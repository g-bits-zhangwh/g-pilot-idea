package com.smallcloud.refactai.struct

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SMCStreamingPeace(
    val choices: List<StreamingChoice>,
    val created: Double,
    val model: String,
    @SerializedName("snippet_telemetry_id") val snippetTelemetryId: Int? = null,
    val cached: Boolean = false,
    @Expose
    var requestId: String = "",
    @SerializedName("multi_line") val multiLine: Boolean,
    @SerializedName("complete_include_char_num") val completeIncludeCharNum: Int,
    @SerializedName("complete_data_collect") val completeDataCollect: Boolean,
    @SerializedName("request_prompt") val requestPrompt: String,
)


data class StreamingChoice(
    val index: Int,
    @SerializedName("code_completion") val delta: String,
    @SerializedName("finish_reason") val finishReason: String?,
)

data class HeadMidTail(
    var head: Int,
    var mid: String,
    val tail: Int
)
