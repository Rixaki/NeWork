package com.example.nework.dto

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

sealed interface FeedItem {
    val id: Long
}

val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

data class Post(
    @SerializedName("id")
    override val id: Long,
    @SerializedName("authorId")
    val authorId: Long,
    @SerializedName("author")
    val author: String,
    @SerializedName("authorJob")
    val authorJob: String?,
    @SerializedName("authorAvatar")
    val authorAvatar: String? = "404",
    @SerializedName("content")
    val content: String,
    @SerializedName("published")
    val published: String = DATE_FORMAT.format(Date()),
    @SerializedName("coords")
    val coords: Coords?,
    @SerializedName("link")
    val videoLink: String?,
    @SerializedName("mentionIds")
    val mentionIds: List<Long> = emptyList(),
    @SerializedName("mentionedMe")
    val mentionedMe: Boolean,
    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Long> = emptyList(),
    @SerializedName("likedByMe")
    val likedByMe: Boolean,
    //ONLY 1 ATTACHMENT IN API SCHEMA
    @SerializedName("attachment")
    val attachment: Attachment? = null,
    @SerializedName("users")
    val users: Map<Long, UserPreview> = emptyMap(),

    val ownedByMe: Boolean = false,
    val isLikeLoading: Boolean = false,
    val likes: Int = likeOwnerIds.size
) : FeedItem {
    fun toEpoch(): Long =
        (DATE_FORMAT.parse(this.published)!!.time) / 1000L

    /*
    companion object {
        //https://stackoverflow.com/questions/47250263/kotlin-convert-timestamp-to-datetime
        fun fromEpoch(epoch: Long): String {
            try {
                val sdf = DATE_FORMAT
                val netDate = Date(epoch * 1000L)
                return sdf.format(netDate)
            } catch (e: Exception) {
                return e.toString()
            }
        }
    }
     */
}

data class Attachment(
    //response schema: string "IMAGE", ...
    @SerializedName("type")
    val type: AttachmentType,
    @SerializedName("url")
    val url: String,
)

enum class AttachmentType {
    IMAGE, VIDEO, AUDIO
}

data class Coords(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("long")
    val long: Double
)

data class Ad(
    override val id: Long,
    val image: String
) : FeedItem

data class TimeHeader(
    override val id: Long,
    val type: TimeType,
    val title: String = "SOME TIME AGO"
) : FeedItem

enum class TimeType {
    TODAY,
    YESTERDAY,
    LAST_WEEK,
}