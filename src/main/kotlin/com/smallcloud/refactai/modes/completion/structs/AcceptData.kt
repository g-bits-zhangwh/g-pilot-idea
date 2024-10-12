package com.smallcloud.refactai.modes.completion.structs
import com.google.gson.annotations.SerializedName

data class AcceptData(
    @SerializedName("request_prompt") val requestPrompt: String,
    val completions: String,
    @SerializedName("completions_length") val completionsLength: Int,
    @SerializedName("user_name") val userName: String,
    @SerializedName("multi_line") val multiLine: Boolean,
    val ide: String,
)
