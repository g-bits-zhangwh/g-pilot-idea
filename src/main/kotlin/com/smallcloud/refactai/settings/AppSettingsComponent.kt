package com.smallcloud.refactai.settings

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.options.newEditor.SettingsDialog
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.JBSplitter
import com.intellij.ui.TitledSeparator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.SystemProperties
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import com.smallcloud.refactai.RefactAIBundle
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.io.path.Path


class AppSettingsComponent {
    val splitter: JBSplitter = JBSplitter(true, 0.3f)
    private val mainPanel: JPanel
    private var experimentalPanel: JPanel = JPanel()
    val myTokenText = JBTextField().apply {
        addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {}
            override fun keyReleased(e: KeyEvent?) {}
            override fun keyPressed(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_MINUS && e.isControlDown && e.isAltDown) {
                    experimentalPanel.isVisible = true
                    myXDebugLSPPortLabel.isVisible = true
                    myXDebugLSPPort.isVisible = true
                }
            }

        })
    }
    private val myContrastUrlText = JBTextField()
    private val myModelText = JBTextField()
    private val myAstFileLimitText = JBTextField()
    private val myAstLightMode = JCheckBox(RefactAIBundle.message("advancedSettings.useASTLightMode"))
    private val myVecdbFileLimitText = JBTextField()
    private val enableAutoSuggestCheckBox = JCheckBox(RefactAIBundle.message("advancedSettings.enableAutoSuggest"))
    private val agreeCodeCollectCheckBox = JCheckBox(RefactAIBundle.message("advancedSettings.agreeCodeCollect"))
    private val autoJumpToEndCheckBox = JCheckBox(RefactAIBundle.message("advancedSettings.autoJumpToEnd"))
    private val completeDisplayThresholdText = JBTextField()
    private val telemetrySnippetCheckBox = JCheckBox(RefactAIBundle.message("advancedSettings.telemetryCodeSnippets"))
    private val pauseCompletionCheckBox = JCheckBox(RefactAIBundle.message("advancedSettings.pauseCompletion"))
    private val completionMaxTokenText = JBTextField()
    val astCheckbox = JCheckBox(RefactAIBundle.message("advancedSettings.useMultipleFilesCompletion")).apply {
        isVisible = true
    }
    private val developerModeCheckBox = JCheckBox(RefactAIBundle.message("advancedSettings.developerMode")).apply {
        isVisible = false
    }
    private val myXDebugLSPPort = JBTextField().apply {
        isVisible = false
    }
    private val myXDebugLSPPortLabel = JBLabel("xDebug LSP port:").apply {
        isVisible = false
    }
    private val myStagingVersionText = JBTextField().apply {
        isVisible = false
    }
    private val myStagingVersionLabel = JBLabel("Staging version:").apply {
        isVisible = false
    }

    val vecdbCheckbox = JCheckBox(RefactAIBundle.message("advancedSettings.useVecDB")).apply {
        isVisible = false
    }

    val openCustomizationButton = JButton("Open Customization").apply {
        val project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(preferredFocusedComponent))
            ?: ProjectManager.getInstance().openProjects.firstOrNull()
            ?: ProjectManager.getInstance().defaultProject

        isVisible = project != ProjectManager.getInstance().defaultProject
        addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent?) {
                val file = Path(SystemProperties.getUserHome(), ".cache", "refact", "customization.yaml")
                VirtualFileManager.getInstance().findFileByNioPath(file)?.let { vf ->
                    val fileDescriptor = OpenFileDescriptor(project, vf)
                    SettingsDialog.findInstance(preferredFocusedComponent)?.doCancelAction()
                    ApplicationManager.getApplication().invokeLater {
                        FileEditorManager.getInstance(project).openTextEditor(fileDescriptor, true)
                    }
                }
            }

        })
    }


    init {
        mainPanel = FormBuilder.createFormBuilder().run {
//            addLabeledComponent(JBLabel("${RefactAIBundle.message("advancedSettings.inferenceURL")}: "),
//                myContrastUrlText, (UIUtil.DEFAULT_VGAP * 1.5).toInt(), false)
//            addComponentToRightColumn(
//                JBLabel(
//                    RefactAIBundle.message("advancedSettings.inferenceURLDescription"),
//                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
//                ), 0
//            )
//            addLabeledComponent(JBLabel("${RefactAIBundle.message("advancedSettings.secretApiKey")}: "),
//                myTokenText, 1, false)

            addComponent(enableAutoSuggestCheckBox, UIUtil.LARGE_VGAP)
            addComponent(
                JBLabel(
                    "是否启用自动补全功能",
                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
                ).apply {
                    setCopyable(true)
                }, 0
            )

            addComponent(agreeCodeCollectCheckBox, UIUtil.LARGE_VGAP)
            addComponent(
                JBLabel(
                    "是否同意进行代码数据收集（收集的匿名数据）",
                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
                ).apply {
                    setCopyable(true)
                }, 0
            )

            addComponent(autoJumpToEndCheckBox, UIUtil.LARGE_VGAP)
            addComponent(
                JBLabel(
                    "每次采纳补全后是否自动跳转到行尾",
                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
                ).apply {
                    setCopyable(true)
                }, 0
            )

            addLabeledComponent(JBLabel("${RefactAIBundle.message("advancedSettings.completeDisplayThreshold")}: "),
               completeDisplayThresholdText, (UIUtil.DEFAULT_VGAP * 1.5).toInt(), false)
            addComponent(
                JBLabel(
                    "代码补全推荐显示阈值（默认值是3。范围是0到正无穷。数值越则小判断越严格，显示的代码补全推荐越少，显示的代",
                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
                ).apply {
                    setCopyable(true)
                }, 0
            )
            addComponent(
                JBLabel(
                    "码补全推荐质量越高。设置为空则代表正无穷，代表不进行阈值判断，所有补全推荐都会显示出来）",
                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
                ).apply {
                    setCopyable(true)
                }, 0
            )

//            addLabeledComponent(JBLabel(RefactAIBundle.message("advancedSettings.completionMaxTokens")),
//                completionMaxTokenText, (UIUtil.DEFAULT_VGAP * 1.5).toInt(), false)
//            addComponent(
//                JBLabel(
//                    RefactAIBundle.message("advancedSettings.completionMaxTokensDesc"),
//                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
//                ), 0
//            )

//            addLabeledComponent(JBLabel("${RefactAIBundle.message("advancedSettings.codeCompletionModel")}: "),
//                myModelText, (UIUtil.DEFAULT_VGAP * 1.5).toInt(), false)
//            addComponent(
//                JBLabel(
//                    RefactAIBundle.message("advancedSettings.codeCompletionModelDesc"),
//                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
//                ), 0
//            )

//            addComponent(telemetrySnippetCheckBox, UIUtil.LARGE_VGAP)
//            addComponent(
//                JBLabel(
//                    RefactAIBundle.message("advancedSettings.telemetryCodeSnippetsDesc"),
//                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
//                ).apply {
//                    setCopyable(true)
//                }, 0
//            )
//            addComponent(pauseCompletionCheckBox, UIUtil.LARGE_VGAP)
//            addComponent(
//                JBLabel(
//                    RefactAIBundle.message("advancedSettings.pauseCompletionDesc"),
//                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
//                ).apply {
//                    setCopyable(true)
//                }, 0
//            )
//            addComponent(astCheckbox, UIUtil.LARGE_VGAP)
//            addComponent(
//                JBLabel(
//                    RefactAIBundle.message("advancedSettings.useMultipleFilesCompletionDescription"),
//                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
//                ).apply {
//                    setCopyable(true)
//                }, 0
//            )
//            addLabeledComponent(JBLabel("${RefactAIBundle.message("advancedSettings.astFileLimit")}: "), myAstFileLimitText,
//                (UIUtil.DEFAULT_VGAP * 1.5).toInt(), false)
//            addComponent(
//                JBLabel(
//                    RefactAIBundle.message("advancedSettings.astFileLimitDescription"),
//                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
//                ), 0
//            )
//            addComponent(myAstLightMode, UIUtil.LARGE_VGAP)
//            addComponent(
//                JBLabel(
//                    RefactAIBundle.message("advancedSettings.useASTLightModeDescription"),
//                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
//                ).apply {
//                    setCopyable(true)
//                }, 0
//            )

//            addComponent(vecdbCheckbox, UIUtil.LARGE_VGAP)
//            addComponent(
//                JBLabel(
//                    RefactAIBundle.message("advancedSettings.useVecDBDescription"),
//                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
//                ).apply {
//                    setCopyable(true)
//                }, 0
//            )
//            addLabeledComponent(JBLabel("${RefactAIBundle.message("advancedSettings.vecDBFileLimit")}: "), myVecdbFileLimitText)
//            addComponent(
//                JBLabel(
//                    RefactAIBundle.message("advancedSettings.vecDBFileLimitDescription"),
//                    UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER
//                ).apply {
//                    setCopyable(true)
//                }, 0
//            )

//            addLabeledComponent(JBLabel("Customization").apply {
//                isVisible = openCustomizationButton.isVisible
//            }, openCustomizationButton, (UIUtil.DEFAULT_VGAP * 1.5).toInt(), false)

            addComponentFillVertically(JPanel(), 0)
        }.panel

        experimentalPanel = FormBuilder.createFormBuilder().run {
            addComponent(TitledSeparator(RefactAIBundle.message("advancedSettings.experimentalFeatures")))

//            addComponent(developerModeCheckBox, UIUtil.LARGE_VGAP)
            addLabeledComponent(myXDebugLSPPortLabel, myXDebugLSPPort, UIUtil.LARGE_VGAP)
//            addLabeledComponent(myStagingVersionLabel, myStagingVersionText, UIUtil.LARGE_VGAP)
            addComponentFillVertically(JPanel(), 0)
        }.panel
        experimentalPanel.isVisible = false

        splitter.firstComponent = mainPanel
        splitter.secondComponent = experimentalPanel
        splitter.isShowDividerControls = true
    }

    val preferredFocusedComponent: JComponent
        get() = myTokenText

    var tokenText: String
        get() = myTokenText.text
        set(newText) {
            myTokenText.text = newText
        }

    var contrastUrlText: String
        get() = myContrastUrlText.text
        set(newText) {
            myContrastUrlText.text = newText
        }

    var useDeveloperMode: Boolean
        get() = developerModeCheckBox.isSelected
        set(newVal) {
            developerModeCheckBox.isSelected = newVal
        }

    var astIsEnabled: Boolean
        get() = astCheckbox.isSelected
        set(newVal) {
            astCheckbox.isSelected = newVal
        }
    var astFileLimit: Int
        get() = myAstFileLimitText.text.toIntOrNull() ?: 15000
        set(newVal) {
            myAstFileLimitText.text = newVal.toString()
        }
    var astLightMode: Boolean
        get() = myAstLightMode.isSelected
        set(newVal) {
            myAstLightMode.isSelected = newVal
        }
    var vecdbIsEnabled: Boolean
        get() = vecdbCheckbox.isSelected
        set(newVal) {
            vecdbCheckbox.isSelected = newVal
        }
    var vecdbFileLimit: Int
        get() = myVecdbFileLimitText.text.toIntOrNull() ?: 15000
        set(newVal) {
            myVecdbFileLimitText.text = newVal.toString()
        }
    var inferenceModel: String?
        get() = myModelText.text
        set(newVal) {
            myModelText.text = newVal
        }

    var enableAutoSuggest: Boolean
        get() = enableAutoSuggestCheckBox.isSelected
        set(newVal) {
            enableAutoSuggestCheckBox.isSelected = newVal
        }

    var agreeCodeCollect: Boolean
        get() = agreeCodeCollectCheckBox.isSelected
        set(newVal) {
            agreeCodeCollectCheckBox.isSelected = newVal
        }

    var autoJumpToEnd: Boolean
        get() = autoJumpToEndCheckBox.isSelected
        set(newVal) {
            autoJumpToEndCheckBox.isSelected = newVal
        }

    var completeDisplayThreshold: String
        get() = completeDisplayThresholdText.text
        set(newVal) {
            completeDisplayThresholdText.text = newVal
        }

    var completionMaxTokens: Int
        get() = completionMaxTokenText.text.toIntOrNull() ?: 0
        set(newVal) {
            completionMaxTokenText.text = newVal.toString()
        }

    var telemetrySnippetsEnabled: Boolean
        get() = telemetrySnippetCheckBox.isSelected
        set(newVal) {
            telemetrySnippetCheckBox.isSelected = newVal
        }

    var pauseCompletion: Boolean
        get() = pauseCompletionCheckBox.isSelected
        set(newVal) {
            pauseCompletionCheckBox.isSelected = newVal
        }

    var xDebugLSPPort: Int?
        get() =
            try {
                myXDebugLSPPort.text.toInt()
            } catch (e: Exception) {
                null
            }

        set(newVal) {
            myXDebugLSPPort.text = newVal?.toString() ?: ""
        }

    var stagingVersion: String
        get() = myStagingVersionText.text
        set(newVal) {
            myStagingVersionText.text = newVal
        }
}