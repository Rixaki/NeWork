package com.example.nework.dto


import com.google.gson.annotations.SerializedName
import java.util.Date

data class Job(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("position")
    val position: String,
    @SerializedName("start")
    val start: String = DATE_FORMAT.format(Date()),
    @SerializedName("finish")
    val finish: String? = null,
    @SerializedName("link")
    val link: String? = null,

    val ownedByMe: Boolean = false,
)