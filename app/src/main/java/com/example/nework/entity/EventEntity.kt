package com.example.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.TypeConverters
import com.example.nework.dto.Coords
import com.example.nework.dto.Event
import com.example.nework.dto.EventType
import com.example.nework.dto.UserPreview

@Entity
@TypeConverters(BaseTypeConverter::class, EventTypeConverter::class)
data class EventEntity (
    val id: Int,
    val authorId: Int,
    val author: String,
    val authorJob: String?,
    val authorAvatar: String?,
    val content: String,
    val datetime: String,
    val published: String,
    @Embedded val coords: Coords?,
    val type: EventTypeEntity = EventTypeEntity.OFFLINE,
    val likeOwnerIds: List<Int> = emptyList(),
    val likedByMe: Boolean,
    val speakerIds: List<Int> = emptyList(),
    val participantsIds: List<Int> = emptyList(),
    val participatedByMe: Boolean,
    val attachment: AttachmentEmbeddable?,
    val link: String?,
    val users: List<UserPreview> = emptyList(),

    val ownedByMe: Boolean = false,
    val isLikeLoading: Boolean = false,
    val likes: Int = likeOwnerIds.size,

    val isTakingPartLoading: Boolean = false,
    val participantsCount: Int = participantsIds.size
) {
    fun toDto(): Event = Event(
        id = id,
        authorId = authorId,
        author = author,
        authorJob = authorJob,
        authorAvatar = authorAvatar,
        content = content,
        datetime = datetime,
        published = published,
        coords = coords,
        type = toDto(type),
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        speakerIds = speakerIds,
        participantsIds = participantsIds,
        participatedByMe = participatedByMe,
        attachment = attachment?.toDto(),
        link = link,
        users = users,

        ownedByMe = ownedByMe,
        isLikeLoading = isLikeLoading,
        likes = likes,

        isTakingPartLoading = isTakingPartLoading,
        participantsCount = participantsCount
    )

    private fun toDto(entity: EventTypeEntity): EventType = EventType.valueOf(entity.name)

    companion object {
        fun fromDto(dto: Event) = with(dto) {
            EventEntity (
                id = id,
                authorId = authorId,
                author = author,
                authorJob = authorJob,
                authorAvatar = authorAvatar,
                content = content,
                datetime = datetime,
                published = published,
                coords = coords,
                type = fromDto(type),
                likeOwnerIds = likeOwnerIds,
                likedByMe = likedByMe,
                speakerIds = speakerIds,
                participantsIds = participantsIds,
                participatedByMe = participatedByMe,
                attachment = attachment?.let {
                    AttachmentEmbeddable.fromDto(it)
                },
                link = link,
                users = users,

                ownedByMe = ownedByMe,
                isLikeLoading = isLikeLoading,
                likes = likes,

                isTakingPartLoading = isTakingPartLoading,
                participantsCount = participantsCount
            )
        }

        private fun fromDto(dto: EventType): EventTypeEntity =
            EventTypeEntity.valueOf(dto.name)
    }
}

enum class EventTypeEntity {
    ONLINE, OFFLINE
}