package com.smallcloud.refactai.modes.completion

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import com.smallcloud.refactai.io.ConnectionStatus
import com.smallcloud.refactai.io.streamedInferenceFetch
import com.smallcloud.refactai.modes.EditorTextState
import com.smallcloud.refactai.modes.Mode
import com.smallcloud.refactai.modes.completion.prompt.PromptInfo
import com.smallcloud.refactai.modes.completion.prompt.RequestCreator
import com.smallcloud.refactai.modes.completion.renderer.AsyncCompletionLayout
import com.smallcloud.refactai.modes.completion.structs.Completion
import com.smallcloud.refactai.modes.completion.structs.DocumentEventExtra
import com.smallcloud.refactai.privacy.Privacy
import com.smallcloud.refactai.statistic.UsageStatistic
import com.smallcloud.refactai.struct.SMCRequest
import com.smallcloud.refactai.utils.getExtension
import java.io.InterruptedIOException
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import com.smallcloud.refactai.io.InferenceGlobalContext.Companion.instance as InferenceGlobalContext
import com.smallcloud.refactai.lsp.LSPProcessHolder.Companion.getInstance as getLSPProcessHolder
import com.smallcloud.refactai.privacy.PrivacyService.Companion.instance as PrivacyService
import com.smallcloud.refactai.statistic.UsageStats.Companion.instance as UsageStats
import com.google.gson.Gson
import com.smallcloud.refactai.Resources
import com.smallcloud.refactai.modes.completion.structs.AcceptData
import java.net.URI
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


private val specialSymbolsRegex = "^[:\\s\\t\\n\\r(){},.\"'\\];]*\$".toRegex()
class CompletionMode(
    override var needToRender: Boolean = true
) : Mode, CaretListener {
    private val scope: String = "completion"
    private val app = ApplicationManager.getApplication()
    private val scheduler = AppExecutorUtil.createBoundedScheduledExecutorService("SMCCompletionScheduler", 1)
    private var processTask: Future<*>? = null
    private var lastRequestId: String = ""
    private var requestTask: Future<*>? = null
    private var completionLayout: AsyncCompletionLayout? = null
    private val logger = Logger.getInstance("StreamedCompletionMode")
    private var completionInProgress: Boolean = false
    private var temperatureCounter: Int = 0


    override fun beforeDocumentChangeNonBulk(event: DocumentEventExtra) {
        event.editor.caretModel.removeCaretListener(this)
        event.editor.caretModel.addCaretListener(this)
        cancelOrClose()
    }

    override fun onTextChange(event: DocumentEventExtra) {
        if (completionInProgress) return
        if (!InferenceGlobalContext.enableAutoSuggest) return
        val fileName = getActiveFile(event.editor.document, event.editor.project) ?: return
        if (PrivacyService.getPrivacy(FileDocumentManager.getInstance().getFile(event.editor.document))
            == Privacy.DISABLED && !InferenceGlobalContext.isSelfHosted) return
        var maybeState: EditorTextState? = null
        val debounceMs: Long
        val editor = event.editor
        val project = editor.project!!
        val logicalPos = event.editor.caretModel.logicalPosition
        var text = editor.document.text
        var offset = -1
        var fileType = FileDocumentManager.getInstance().getFile(event.editor.document)?.extension
        if (fileType != null )
            fileType = ".$fileType"
        ApplicationManager.getApplication().runReadAction {
            offset = editor.caretModel.offset
        }

        val currentLine = text.substring(editor.document.getLineStartOffset(logicalPos.line),
                editor.document.getLineEndOffset(logicalPos.line))
        val rightOfCursor = text.substring(offset,
            editor.document.getLineEndOffset(logicalPos.line))

//        if (!rightOfCursor.matches(specialSymbolsRegex)) return

//        val isMultiline = currentLine.all { it == ' ' || it == '\t' }
        val isMultiline = false
        var pos = 0
        if (isMultiline) {
            val startOffset = editor.document.getLineStartOffset(logicalPos.line)
            val endOffset = editor.document.getLineEndOffset(logicalPos.line)
            text = text.removeRange(startOffset, endOffset)
        } else {
            pos = offset - editor.document.getLineStartOffset(logicalPos.line)
        }

        if (!event.force) {
            val docEvent = event.event ?: return
            if (docEvent.offset + docEvent.newLength > editor.document.text.length) return
            if (docEvent.newLength + docEvent.oldLength <= 0) return
            maybeState = EditorTextState(
                editor,
                editor.document.modificationStamp,
                docEvent.offset + docEvent.newLength + event.offsetCorrection
            )

            if (shouldIgnoreChange(docEvent, editor, maybeState.offset)) {
                return
            }

            debounceMs = CompletionTracker.calcDebounceTime(editor)
            CompletionTracker.updateLastCompletionRequestTime(editor)
            logger.debug("Debounce time: $debounceMs")
        } else {
            app.invokeAndWait {
                maybeState = EditorTextState(
                    editor,
                    editor.document.modificationStamp,
                    editor.caretModel.offset
                )
            }
            debounceMs = 0
        }

        val state = maybeState ?: return
        if (!state.isValid()) return

        val promptInfo: List<PromptInfo> = listOf()
        val stat = UsageStatistic(scope, extension = getExtension(fileName))
        val baseUrl = getLSPProcessHolder(project).url
        val request = RequestCreator.create(
            fileName, text, logicalPos.line, pos,
            stat, "Infill", "infill", promptInfo,
            baseUrl = baseUrl,
            stream = false, model = InferenceGlobalContext.model,
            multiline = true,
            useAst = InferenceGlobalContext.astIsEnabled,
            showMultiline = event.showMultiline,
            forceDisplay = event.force,
            fileType = fileType,
        ) ?: return

        processTask = scheduler.schedule({
            process(request, state, event.force, )
        }, debounceMs, TimeUnit.MILLISECONDS)
    }

    private fun renderCompletion(
            editor: Editor,
            state: EditorTextState,
            completionData: Completion,
            animation: Boolean
    ) {
        val modificationStamp = editor.document.modificationStamp
        val offset = editor.caretModel.offset

        val invalidStamp = state.modificationStamp != modificationStamp
        val invalidOffset = state.offset != offset
        if (invalidStamp || invalidOffset) {
            logger.info("Completion is dropped: invalidStamp || invalidOffset")
            logger.info(
                "state_offset: ${state.offset}," +
                        " state_modificationStamp: ${state.modificationStamp}"
            )
            logger.info(
                "editor_offset: $offset, editor_modificationStamp: $modificationStamp"
            )
            return
        }
        if (processTask == null) {
            logger.info("Completion is dropped: there is no active processTask is left")
            return
        }
        logger.info(
            "Completion rendering: offset: ${state.offset}," +
                    " modificationStamp: ${state.modificationStamp}"
        )
        logger.info("Completion data: ${completionData.completion}")
        try {
            if (completionLayout == null) {
                completionLayout = AsyncCompletionLayout(editor)
            }
            completionLayout?.also {
                it.update(completionData, state, needToRender, animation)
            }
        } catch (ex: Exception) {
            logger.warn("Exception while rendering completion", ex)
            logger.debug("Exception while rendering completion cancelOrClose request")
            cancelOrClose()
        }
    }

    override fun hide() {
        if (!isInActiveState()) return
        scheduler.submit {
            try {
                processTask?.get()
            } catch (_: CancellationException) {
            } finally {
                completionLayout?.hide()
            }
        }
    }

    override fun show() {
        if (isInActiveState()) return
        scheduler.submit {
            try {
                processTask?.get()
            } catch (_: CancellationException) {
            } finally {
                completionLayout?.show()
            }
        }
    }

    private fun process(
        request: SMCRequest,
        editorState: EditorTextState,
        force: Boolean,
    ) {
        InferenceGlobalContext.status = ConnectionStatus.PENDING
        completionInProgress = true
        if (force) {
            request.body.parameters.maxNewTokens = 50
            request.body.noCache = true
            temperatureCounter++
        } else {
            temperatureCounter = 0
        }
        if (InferenceGlobalContext.completionMaxTokens > 1) {
            request.body.parameters.maxNewTokens = InferenceGlobalContext.completionMaxTokens
        }

        if (temperatureCounter > 1) {
            request.body.parameters.temperature = 0.6F
        }
        if (requestTask != null && requestTask!!.isDone && requestTask!!.isCancelled ) {
            requestTask!!.cancel(true)
        }
        lastRequestId = request.id
        streamedInferenceFetch(request, dataReceiveEnded = {
            InferenceGlobalContext.status = ConnectionStatus.CONNECTED
            InferenceGlobalContext.lastErrorMsg = null
        }) { prediction ->
            if (prediction.choices == null || prediction.choices.isEmpty()) {
                throw Exception("No choices found")
            }
            val choice = prediction.choices.first()
            if (lastRequestId != prediction.requestId) {
                completionLayout?.dispose()
                completionLayout = null
                return@streamedInferenceFetch
            }

            val completion: Completion = if (completionLayout?.lastCompletionData == null ||
                completionLayout?.lastCompletionData?.createdTs != prediction.created) {
                val completion = Completion(request.body.inputs.sources.values.toList().first(),
                    prompt = prediction.requestPrompt,
                    offset = editorState.offset,
                    multiline = false,
                    createdTs = prediction.created,
                    isFromCache = prediction.cached,
                    snippetTelemetryId = prediction.snippetTelemetryId,
                    completeMultiLine = prediction.multiLine,
                    completeIncludeCharNum = prediction.completeIncludeCharNum,
                    completeDataCollect = prediction.completeDataCollect,
                )
                completion
            } else {
                completionLayout!!.lastCompletionData!!
            }
            if (completion.snippetTelemetryId == null) {
                completion.snippetTelemetryId = prediction.snippetTelemetryId
            }
            if ((!completionInProgress)
                || (choice.delta.isEmpty() && !choice.finishReason.isNullOrEmpty())) {
                return@streamedInferenceFetch
            }
            completion.updateCompletion(choice.delta)
            app.invokeLater {
                synchronized(this) {
                    renderCompletion(
                        editorState.editor, editorState, completion, false
                    )
                }
            }
        }?.also {
            try {
                requestTask = it.get()
                requestTask!!.get()
                logger.debug("Completion request finished")
                completionInProgress = false
            } catch (e: InterruptedException) {
                handleInterruptedException(requestTask, editorState.editor)
            } catch (e: InterruptedIOException) {
                handleInterruptedException(requestTask, editorState.editor)
            } catch (e: ExecutionException) {
                cancelOrClose()
                catchNetExceptions(e.cause)
            } catch (e: Exception) {
                InferenceGlobalContext.status = ConnectionStatus.ERROR
                InferenceGlobalContext.lastErrorMsg = e.message
                cancelOrClose()
                logger.warn("Exception while completion request processing", e)
            }
        }
    }

    private fun handleInterruptedException(requestFuture: Future<*>?, editor: Editor) {
        InferenceGlobalContext.status = ConnectionStatus.CONNECTED
        requestFuture?.cancel(true)
        cancelOrClose()
        logger.debug("lastReqJob abort")
    }

    private fun catchNetExceptions(e: Throwable?) {
        InferenceGlobalContext.status = ConnectionStatus.ERROR
        InferenceGlobalContext.lastErrorMsg = e?.message
        logger.warn("Exception while completion request processing", e)
    }

    override fun onTabPressed(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val params = completionLayout?.lastCompletionData?.let {
            AcceptData(
                requestPrompt = if (InferenceGlobalContext.agreeCodeCollect && it.completeDataCollect) {
                    it.prompt
                } else {
                    ""
                },
                completions = if (InferenceGlobalContext.agreeCodeCollect && it.completeDataCollect) {
                    it.completion
                } else {
                    ""
                },
                completionsLength=it.completion.length,
                userName = InferenceGlobalContext.username,
                multiLine = it.completeMultiLine,
                ide= InferenceGlobalContext.ide,
            )
        }
        if (params != null) {
            val json = Gson().toJson(params)
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("${InferenceGlobalContext.inferenceUri}accept")
                .post(body)
                .build()

            InferenceGlobalContext.client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                }
            })
        }

        completionLayout?.apply {
            applyPreview(caret ?: editor.caretModel.currentCaret)
            lastCompletionData?.snippetTelemetryId?.let {
                UsageStats?.snippetAccepted(it)
            }
            dispose()
        }
        completionLayout = null
    }

    override fun onEscPressed(editor: Editor, caret: Caret?, dataContext: DataContext) {
        cancelOrClose()
    }

    override fun onCaretChange(event: CaretEvent) {
    }

    override fun caretPositionChanged(event: CaretEvent) {
        cancelOrClose()
    }

    override fun isInActiveState(): Boolean = completionLayout != null && completionLayout!!.rendered && needToRender

    override fun cleanup(editor: Editor) {
        cancelOrClose()
    }

    private fun shouldIgnoreChange(event: DocumentEvent?, editor: Editor, offset: Int): Boolean {
        if (event == null) return false
        val document = event.document

        if (editor.editorKind != EditorKind.MAIN_EDITOR && !app.isUnitTestMode) {
            return true
        }
        if (!EditorModificationUtil.checkModificationAllowed(editor)
            || document.getRangeGuard(offset, offset) != null
        ) {
            document.fireReadOnlyModificationAttempt()
            return true
        }
        return false
    }

    private fun getActiveFile(document: Document, project: Project?): String? {
        val file = FileDocumentManager.getInstance().getFile(document) ?: return null
        return file.path
    }

    private fun cancelOrClose() {
        try {
            processTask?.cancel(true)
            processTask?.get()
        } catch (_: CancellationException) {
        } finally {
            completionInProgress = false
            processTask = null
            completionLayout?.dispose()
            completionLayout = null
        }
    }
}
