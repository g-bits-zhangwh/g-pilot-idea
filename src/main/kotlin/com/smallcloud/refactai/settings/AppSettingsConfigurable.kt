package com.smallcloud.refactai.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.smallcloud.refactai.PluginState
import com.smallcloud.refactai.account.AccountManagerChangedNotifier
import org.jetbrains.annotations.Nls
import javax.swing.JComponent
import com.smallcloud.refactai.account.AccountManager.Companion.instance as AccountManager
import com.smallcloud.refactai.io.InferenceGlobalContext.Companion.instance as InferenceGlobalContext
import com.intellij.openapi.ui.Messages

/**
 * Provides controller functionality for application settings.
 */
class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

    init {
        ApplicationManager.getApplication().messageBus.connect(PluginState.instance)
            .subscribe(AccountManagerChangedNotifier.TOPIC, object : AccountManagerChangedNotifier {
                override fun apiKeyChanged(newApiKey: String?) {
                    mySettingsComponent?.myTokenText?.let { it.text = newApiKey }
                    mySettingsComponent?.astIsEnabled = InferenceGlobalContext.astIsEnabled
                    mySettingsComponent?.vecdbIsEnabled = InferenceGlobalContext.vecdbIsEnabled
                    mySettingsComponent?.splitter?.revalidate()
                }
            })
    }

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP
    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String {
        return "Settings"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return mySettingsComponent!!.preferredFocusedComponent
    }

    override fun createComponent(): JComponent {
        mySettingsComponent = AppSettingsComponent()
        return mySettingsComponent!!.splitter
    }

    override fun isModified(): Boolean {
//         var modified =
//             (mySettingsComponent!!.tokenText.isNotEmpty() && (AccountManager.apiKey == null ||
//                 mySettingsComponent!!.tokenText.trim() != AccountManager.apiKey))
//         modified = modified || (mySettingsComponent!!.tokenText.isEmpty() && AccountManager.apiKey != null)

//        modified =
//            modified || (mySettingsComponent!!.contrastUrlText.isNotEmpty() &&
//                mySettingsComponent!!.contrastUrlText != InferenceGlobalContext.inferenceUri)
//        modified =
//            modified || (mySettingsComponent!!.contrastUrlText.isEmpty() && !InferenceGlobalContext.isCloud)

//         modified = modified || mySettingsComponent!!.useDeveloperMode != InferenceGlobalContext.developerModeEnabled
//
//         modified = modified || mySettingsComponent!!.xDebugLSPPort != InferenceGlobalContext.xDebugLSPPort
//
//         modified = modified || mySettingsComponent!!.stagingVersion != InferenceGlobalContext.stagingVersion
//
//         modified = modified || mySettingsComponent!!.astIsEnabled != InferenceGlobalContext.astIsEnabled
//         modified = modified || mySettingsComponent!!.astFileLimit != InferenceGlobalContext.astFileLimit
//         modified = modified || mySettingsComponent!!.astLightMode != InferenceGlobalContext.astLightMode
//         modified = modified || mySettingsComponent!!.vecdbIsEnabled != InferenceGlobalContext.vecdbIsEnabled
//         modified = modified || mySettingsComponent!!.vecdbFileLimit != InferenceGlobalContext.vecdbFileLimit
//
//         modified =
//             modified || mySettingsComponent!!.inferenceModel?.trim()?.ifEmpty { null } != InferenceGlobalContext.model
        var modified = mySettingsComponent!!.enableAutoSuggest != InferenceGlobalContext.enableAutoSuggest
        modified = modified || mySettingsComponent!!.agreeCodeCollect != InferenceGlobalContext.agreeCodeCollect
        modified = modified || mySettingsComponent!!.autoJumpToEnd != InferenceGlobalContext.autoJumpToEnd
        modified = modified || mySettingsComponent!!.completeDisplayThreshold != InferenceGlobalContext.completeDisplayThreshold
//         modified = modified || mySettingsComponent!!.completionMaxTokens!= InferenceGlobalContext.completionMaxTokens
//         modified = modified || mySettingsComponent!!.telemetrySnippetsEnabled != InferenceGlobalContext.telemetrySnippetsEnabled
//         modified = modified || mySettingsComponent!!.pauseCompletion != !InferenceGlobalContext.useAutoCompletion
        return modified
    }

    override fun apply() {
        // 获取 completeDisplayThreshold
        val threshold = mySettingsComponent!!.completeDisplayThreshold

        // 检查 threshold 是否为空或非正数
        if (!threshold.matches(Regex("([1-9]\\d*|0)(\\.\\d+)?$")) && threshold != "") {
            mySettingsComponent!!.completeDisplayThreshold = InferenceGlobalContext.completeDisplayThreshold
            // 显示提示框
            Messages.showErrorDialog("complete Display Threshold补全结果显示阈值设置错误，请设置为空或正数字.", "参数设置错误")
            return
        }
//         AccountManager.apiKey = mySettingsComponent!!.tokenText.trim().ifEmpty { null }
//        InferenceGlobalContext.inferenceUri = mySettingsComponent!!.contrastUrlText.ifEmpty { null }
//        mySettingsComponent!!.contrastUrlText = InferenceGlobalContext.inferenceUri ?: ""
//         InferenceGlobalContext.developerModeEnabled = mySettingsComponent!!.useDeveloperMode
//         InferenceGlobalContext.stagingVersion = mySettingsComponent!!.stagingVersion
//         InferenceGlobalContext.xDebugLSPPort = mySettingsComponent!!.xDebugLSPPort
//         InferenceGlobalContext.astIsEnabled = mySettingsComponent!!.astIsEnabled
//         InferenceGlobalContext.astFileLimit = mySettingsComponent!!.astFileLimit
//         InferenceGlobalContext.astLightMode = mySettingsComponent!!.astLightMode
//         InferenceGlobalContext.vecdbIsEnabled = mySettingsComponent!!.vecdbIsEnabled
//         InferenceGlobalContext.vecdbFileLimit = mySettingsComponent!!.vecdbFileLimit
        InferenceGlobalContext.enableAutoSuggest = mySettingsComponent!!.enableAutoSuggest
        InferenceGlobalContext.agreeCodeCollect = mySettingsComponent!!.agreeCodeCollect
        InferenceGlobalContext.autoJumpToEnd = mySettingsComponent!!.autoJumpToEnd
        InferenceGlobalContext.completeDisplayThreshold = mySettingsComponent!!.completeDisplayThreshold
//         InferenceGlobalContext.completionMaxTokens = mySettingsComponent!!.completionMaxTokens
//         InferenceGlobalContext.telemetrySnippetsEnabled = mySettingsComponent!!.telemetrySnippetsEnabled
//         InferenceGlobalContext.useAutoCompletion = !mySettingsComponent!!.pauseCompletion
//         InferenceGlobalContext.model = mySettingsComponent!!.inferenceModel?.trim()?.ifEmpty { null }
    }

    override fun reset() {
//         mySettingsComponent!!.tokenText = AccountManager.apiKey ?: ""
//        mySettingsComponent!!.contrastUrlText = InferenceGlobalContext.inferenceUri ?: ""
//         mySettingsComponent!!.useDeveloperMode = InferenceGlobalContext.developerModeEnabled
//         mySettingsComponent!!.stagingVersion = InferenceGlobalContext.stagingVersion
//         mySettingsComponent!!.xDebugLSPPort = InferenceGlobalContext.xDebugLSPPort
//         mySettingsComponent!!.astIsEnabled = InferenceGlobalContext.astIsEnabled
//         mySettingsComponent!!.astFileLimit = InferenceGlobalContext.astFileLimit
//         mySettingsComponent!!.astLightMode = InferenceGlobalContext.astLightMode
//         mySettingsComponent!!.vecdbIsEnabled = InferenceGlobalContext.vecdbIsEnabled
//         mySettingsComponent!!.vecdbFileLimit = InferenceGlobalContext.vecdbFileLimit
//         mySettingsComponent!!.inferenceModel = InferenceGlobalContext.model
        mySettingsComponent!!.enableAutoSuggest = InferenceGlobalContext.enableAutoSuggest
        mySettingsComponent!!.agreeCodeCollect = InferenceGlobalContext.agreeCodeCollect
        mySettingsComponent!!.autoJumpToEnd = InferenceGlobalContext.autoJumpToEnd
        mySettingsComponent!!.completeDisplayThreshold = InferenceGlobalContext.completeDisplayThreshold
//         mySettingsComponent!!.completionMaxTokens = InferenceGlobalContext.completionMaxTokens
//         mySettingsComponent!!.telemetrySnippetsEnabled = InferenceGlobalContext.telemetrySnippetsEnabled
//         mySettingsComponent!!.pauseCompletion = !InferenceGlobalContext.useAutoCompletion
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }


}
