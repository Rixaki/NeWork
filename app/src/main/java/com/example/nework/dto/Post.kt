package com.example.nework.dto

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale

sealed interface FeedItem {
    val id: Int
}

val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)

data class Post(
    @SerializedName("id")
    override val id: Int,
    @SerializedName("authorId")
    val authorId: Int,
    @SerializedName("author")
    val author: String,
    @SerializedName("authorJob")
    val authorJob: String?,
    @SerializedName("authorAvatar")
    val authorAvatar: String? = "404",
    @SerializedName("content")
    val content: String,
    @SerializedName("published")
    val published: String,
    @SerializedName("coords")
    val coords: Coords?,
    @SerializedName("link")
    val link: String?,
    @SerializedName("mentionIds")
    val mentionIds: List<Int> = emptyList(),
    @SerializedName("mentionedMe")
    val mentionedMe: Boolean,
    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Int> = emptyList(),
    @SerializedName("likedByMe")
    val likedByMe: Boolean,
    //ONLY 1 ATTACHMENT IN API SCHEMA
    @SerializedName("attachment")
    val attachment: Attachment? = null,
    @SerializedName("users")
    val users: List<UserPreview> = emptyList(),

    val ownedByMe: Boolean = false,
    val isLikeLoading: Boolean = false,
    val likes: Int = likeOwnerIds.size
) : FeedItem {
    fun toEpoch(): Long =
        (DATE_FORMAT.parse(this.published)!!.time) / 1000L
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
    val lat: Int,
    @SerializedName("long")
    val long: Int
)

data class Ad(
    override val id: Int,
    val image: String
) : FeedItem

data class TimeHeader(
    override val id: Int,
    val type: TimeType,
    val title: String = "SOME TIME AGO"
) : FeedItem

enum class TimeType() {
    TODAY,
    YESTERDAY,
    LAST_WEEK,
}