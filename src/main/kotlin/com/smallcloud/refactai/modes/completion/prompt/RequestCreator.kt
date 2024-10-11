package com.smallcloud.refactai.modes.completion.prompt

import com.smallcloud.refactai.Resources
import com.smallcloud.refactai.statistic.UsageStatistic
import com.smallcloud.refactai.struct.SMCCursor
import com.smallcloud.refactai.struct.SMCInputs
import com.smallcloud.refactai.struct.SMCRequest
import com.smallcloud.refactai.struct.SMCRequestBody
import com.smallcloud.refactai.struct.GpilotParameters
import java.net.URI
import com.smallcloud.refactai.io.InferenceGlobalContext.Companion.instance as InferenceGlobalContext
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId

object RequestCreator {
    fun create(
        fileName: String, text: String,
        line: Int, column: Int,
        stat: UsageStatistic,
        intent: String, functionName: String,
        promptInfo: List<PromptInfo>,
        baseUrl: URI,
        model: String? = null,
        useAst: Boolean = false,
        stream: Boolean = true,
        multiline: Boolean = false,
        showMultiline: Boolean = false,
        forceDisplay: Boolean = false,
        fileType: String? = null
    ): SMCRequest? {
        val inputs = SMCInputs(
            sources = mutableMapOf(fileName to text),
            cursor = SMCCursor(
                file = fileName,
                line = line,
                character = column,
            ),
            multiline = multiline,
        )
        val url: String = if (showMultiline) {
            InferenceGlobalContext.multilineInferenceUri + "generate"
        } else {
            InferenceGlobalContext.inferenceUri + "generate"
        }

        val requestBody = SMCRequestBody(
            inputs = inputs,
            stream = stream,
            model = model,
            useAst = useAst,
            gpilotParameters=GpilotParameters(
                generateUrl = url,
                ide = InferenceGlobalContext.ide,
                username = InferenceGlobalContext.username,
                showMultiline = showMultiline,
                forceDisplay = forceDisplay,
                extensionVersion = getCurrentPluginVersion(),
                completeDisplayThreshold = InferenceGlobalContext.completeDisplayThreshold,
                fileType = fileType
            )
        )

        return InferenceGlobalContext.makeRequest(
            requestBody,
        )?.also {
            it.stat = stat
            it.uri = baseUrl.resolve(Resources.defaultCodeCompletionUrlSuffix)
        }
    }

    fun getCurrentPluginVersion(): String {
        // 获取当前类的类加载器所属的插件
        val pluginId = PluginManagerCore.getPlugin(PluginId.getId(InferenceGlobalContext.extensionId))

        return if (pluginId != null) {
            pluginId.version
        } else {
            ""
        }
    }
}
