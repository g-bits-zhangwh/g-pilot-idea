package com.smallcloud.refactai.modes.completion.structs


data class Completion(
    val originalText: String,
    val prompt: String,
    var completion: String = "",
    val multiline: Boolean,
    val offset: Int,
    val createdTs: Double = -1.0,
    val isFromCache: Boolean = false,
    var snippetTelemetryId: Int? = null,
    val completeMultiLine: Boolean,
    val completeIncludeCharNum: Int,
    val completeDataCollect: Boolean,
    var finalUserShowText: String? = null,
    var finalCompletionOffset: Int? = null,
) {
    fun updateCompletion(text: String) {
        completion += text
    }
}
