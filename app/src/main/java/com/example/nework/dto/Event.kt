package com.example.nework.dto

import com.google.gson.annotations.SerializedName

data class Event (
    @SerializedName("id")
    override val id: Int,
    @SerializedName("authorId")
    val authorId: Int,
    @SerializedName("author")
    val author: String,
    @SerializedName("authorJob")
    val authorJob: String?,
    @SerializedName("authorAvatar")
    val authorAvatar: String?,
    @SerializedName("content")
    val content: String,
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("published")
    val published: String,
    @SerializedName("coords")
    val coords: Coords?,
    @SerializedName("type")
    val type: EventType = EventType.OFFLINE,
    @SerializedName("likeOwnerIds")
    val likeOwnerIds: List<Int> = emptyList(),
    @SerializedName("likedByMe")
    val likedByMe: Boolean,
    @SerializedName("speakerIds")
    val speakerIds: List<Int> = emptyList(),
    @SerializedName("participantsIds")
    val participantsIds: List<Int> = emptyList(),
    @SerializedName("participatedByMe")
    val participatedByMe: Boolean,
    @SerializedName("attachment")
    val attachment: Attachment?,
    @SerializedName("link")
    val videoLink: String?,
    //TODO: WHAT USERS? SPEAKERS? PARTS? ALL?
    @SerializedName("users")
    val users: List<UserPreview> = emptyList(),

    val ownedByMe: Boolean = false,
    val isLikeLoading: Boolean = false,
    val likes: Int = likeOwnerIds.size,

    val isTakingPartLoading: Boolean = false,
    val participantsCount: Int = participantsIds.size
) : FeedItem {
    fun toEpoch(): Long =
        (DATE_FORMAT.parse(this.published)!!.time) / 1000L
}

enum class EventType {
    ONLINE, OFFLINE
}