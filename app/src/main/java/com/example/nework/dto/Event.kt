package com.example.nework.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Event(
    @SerializedName("id")
    override val id: Long,
    @SerializedName("authorId")
    val authorId: Long,
    @SerializedName("author")
    val author: String,
    @SerializedName("authorJob")
    val authorJob: String?,
    @SerializedName("authorAvatar")
    val authorAvatar: String?,
    @SerializedName("content")
    val content: String,
    @SerializedName("datetime")
    val datetime: String = DATE_FORMAT.format(Date()),
    @SerializedName("published")
    val published: String = DATE_FORMAT.format(Date()),
    @SerializedName("coords")
    val coords: Coords?,
    @SerializedName("type")
    val type: EventType = EventType.OFFLINE,
    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Long> = emptyList(),
    @SerializedName("likedByMe")
    val likedByMe: Boolean,
    @SerializedName("speakerIds")
    val speakerIds: List<Long> = emptyList(),
    @SerializedName("participantsIds")
    val participantsIds: List<Long> = emptyList(),
    @SerializedName("participatedByMe")
    val participatedByMe: Boolean,
    @SerializedName("attachment")
    val attachment: Attachment?,
    @SerializedName("link")
    val videoLink: String?,
    //TODO: WHAT USERS? SPEAKERS? PARTS? ALL?
    @SerializedName("users")
    val users: Map<Long, UserPreview> = emptyMap(),

    val ownedByMe: Boolean = false,
    val isLikeLoading: Boolean = false,
    val likes: Int = likeOwnerIds.size,

    val isTakingPartLoading: Boolean = false
) : FeedItem {
    fun toEpoch(): Long =
        (DATE_FORMAT.parse(this.published)!!.time) / 1000L
}

enum class EventType {
    ONLINE, OFFLINE
}