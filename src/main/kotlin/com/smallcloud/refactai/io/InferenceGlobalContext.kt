package com.smallcloud.refactai.io

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.MessageBus
import com.smallcloud.refactai.struct.DeploymentMode
import com.smallcloud.refactai.struct.SMCRequest
import com.smallcloud.refactai.struct.SMCRequestBody
import okhttp3.OkHttpClient
import java.net.URI
import com.smallcloud.refactai.account.AccountManager.Companion.instance as AccountManager
import com.smallcloud.refactai.settings.AppSettingsState.Companion.instance as AppSettingsState

class InferenceGlobalContext : Disposable {
    private val messageBus: MessageBus = ApplicationManager.getApplication().messageBus
    var connection: AsyncConnection = AsyncConnection()
    val client = OkHttpClient()

    fun canRequest(): Boolean {
        return status == ConnectionStatus.CONNECTED
    }

    var status: ConnectionStatus = ConnectionStatus.CONNECTED
        set(newStatus) {
            if (field == newStatus) return
            field = newStatus
            ApplicationManager.getApplication()
                    .messageBus
                    .syncPublisher(ConnectionChangedNotifier.TOPIC)
                    .statusChanged(field)
        }
    var lastErrorMsg: String? = null
        set(newMsg) {
            if (field == newMsg) return
            field = newMsg
            ApplicationManager.getApplication()
                    .messageBus
                    .syncPublisher(ConnectionChangedNotifier.TOPIC)
                    .lastErrorMsgChanged(field)
        }

    var inferenceUri: String?
        get() {
            return AppSettingsState.userInferenceUri
        }
        set(newValue) {
        }

    var multilineInferenceUri: String?
        get() {
            return AppSettingsState.multilineInferenceUri
        }
        set(newValue) {
        }

    var extensionId: String
        get() {
            return AppSettingsState.extensionId
        }
        set(newValue) {
        }

    var ide: String
        get() {
            return AppSettingsState.ide
        }
        set(newValue) {
        }

    var username: String
        get() {
            return if (AppSettingsState.username == null){
                AppSettingsState.initializeUsername()
            } else {
                AppSettingsState.username!!
            }
        }
        set(newValue) {
        }

    // cloudInferenceUri is uri from SMC server; must be change only in login method
    var cloudInferenceUri: URI?
        set(newInferenceUrl) {
            if (newInferenceUrl == AppSettingsState.inferenceUri?.let { URI(it) }) return
            messageBus
                    .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
                    .inferenceUriChanged(newInferenceUrl)
        }
        get() {
            return AppSettingsState.inferenceUri?.let { URI(it) }
        }


    var isNewChatStyle: Boolean = false

    var temperature: Float?
        get() = AppSettingsState.temperature
        set(newTemp) {
            if (newTemp == temperature) return
            messageBus
                    .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
                    .temperatureChanged(newTemp)
        }

    var developerModeEnabled: Boolean
        get() = AppSettingsState.developerModeEnabled
        set(newValue) {
            if (newValue == developerModeEnabled) return
            messageBus
                    .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
                    .developerModeEnabledChanged(newValue)
        }

    var stagingVersion: String
        get() = AppSettingsState.stagingVersion
        set(newStr) {
            if (newStr == AppSettingsState.stagingVersion) return
            AppSettingsState.stagingVersion = newStr
        }

    var lastAutoModel: String? = null
        set(newModel) {
            if (newModel == field) return
            field = newModel
            messageBus
                    .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
                    .lastAutoModelChanged(newModel)
        }

    var model: String?
        get() = AppSettingsState.model
        set(newModel) {
            if (newModel == model) return
            messageBus
                    .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
                    .modelChanged(newModel)
        }

    var useAutoCompletion: Boolean
        get() = AppSettingsState.useAutoCompletion
        set(newValue) {
            if (newValue == useAutoCompletion) return
            messageBus
                    .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
                    .useAutoCompletionModeChanged(newValue)
        }

    val deploymentMode: DeploymentMode
        get() {
            if (AppSettingsState.userInferenceUri == null) {
                return DeploymentMode.CLOUD
            }

            return when(AppSettingsState.userInferenceUri!!.lowercase()) {
                "hf" -> DeploymentMode.HF
                "refact" -> DeploymentMode.CLOUD
                else -> DeploymentMode.SELF_HOSTED
            }
        }

    val isCloud: Boolean
        get() {
            return deploymentMode == DeploymentMode.CLOUD
        }
    val isSelfHosted: Boolean
        get() {
            return deploymentMode == DeploymentMode.SELF_HOSTED
        }

    var astIsEnabled: Boolean
        get() = AppSettingsState.astIsEnabled
        set(newValue) {
            if (newValue == astIsEnabled) return
            messageBus
                   .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
                   .astFlagChanged(newValue)
        }

    var astFileLimit: Int
        get() { return AppSettingsState.astFileLimit }
        set(newValue) {
            if (newValue == astFileLimit) return
            messageBus
                  .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
                  .astFileLimitChanged(newValue)
        }

    var astLightMode: Boolean
        get() = AppSettingsState.astLightMode
        set(newValue) {
            if (newValue == astLightMode) return
            messageBus
                 .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
                 .astLightModeChanged(newValue)
        }


    var vecdbIsEnabled: Boolean
        get() = AppSettingsState.vecdbIsEnabled
        set(newValue) {
            if (newValue == vecdbIsEnabled) return
            messageBus
                .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
                .vecdbFlagChanged(newValue)
        }

    var vecdbFileLimit: Int
        get() { return AppSettingsState.vecdbFileLimit }
        set(newValue) {
            if (newValue == vecdbFileLimit) return
            messageBus
                .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
                .vecdbFileLimitChanged(newValue)
        }

    var insecureSSL: Boolean
        get() = AppSettingsState.insecureSSL
        set(newValue) {

        }

    var enableAutoSuggest: Boolean
        get() = AppSettingsState.enableAutoSuggest
        set(newValue) {
            if (newValue == enableAutoSuggest) return
            messageBus
               .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
               .enableAutoSuggestChanged(newValue)
        }

    var agreeCodeCollect: Boolean
        get() = AppSettingsState.agreeCodeCollect
        set(newValue) {
            if (newValue == agreeCodeCollect) return
            messageBus
               .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
               .agreeCodeCollectChanged(newValue)
        }

    var autoJumpToEnd: Boolean
        get() = AppSettingsState.autoJumpToEnd
        set(newValue) {
            if (newValue == autoJumpToEnd) return
            messageBus
               .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
               .autoJumpToEndChanged(newValue)
        }

    var completeDisplayThreshold: String
        get() = AppSettingsState.completeDisplayThreshold
        set(newValue) {
            if (newValue == completeDisplayThreshold) return
            messageBus
               .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
               .completeDisplayThresholdChanged(newValue)
        }

    var completionMaxTokens: Int
        get() { return AppSettingsState.completionMaxTokens }
        set(newValue) {
            if (newValue == completionMaxTokens) return
            messageBus
              .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
              .completionMaxTokensChanged(newValue)
        }

    var telemetrySnippetsEnabled: Boolean
        get() { return AppSettingsState.telemetrySnippetsEnabled }
        set(newValue) {
            if (newValue == telemetrySnippetsEnabled) return
            messageBus
              .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
              .telemetrySnippetsEnabledChanged(newValue)
        }

    var xDebugLSPPort: Int?
        get() { return AppSettingsState.xDebugLSPPort }
        set(newValue) {
            if (newValue == AppSettingsState.xDebugLSPPort) return
            messageBus
                .syncPublisher(InferenceGlobalContextChangedNotifier.TOPIC)
                .xDebugLSPPortChanged(newValue)
        }

    fun makeRequest(requestData: SMCRequestBody): SMCRequest? {
        val apiKey = AccountManager.apiKey

        return SMCRequest(requestData, apiKey ?: "self_hosted")
    }

    override fun dispose() {}

    companion object {
        @JvmStatic
        val instance: InferenceGlobalContext
            get() = ApplicationManager.getApplication().getService(InferenceGlobalContext::class.java)
    }
}
