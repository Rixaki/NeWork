package com.example.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.nework.dto.Coords
import com.example.nework.dto.Event
import com.example.nework.dto.EventType

@Entity
@TypeConverters(BaseTypeConverter::class)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String?,
    val authorAvatar: String?,
    val content: String,
    val datetime: String,
    val published: String,
    @Embedded val coords: Coords?,
    val type: EventTypeEntity = EventTypeEntity.OFFLINE,
    val likeOwnerIds: List<Long> = emptyList(),
    val likedByMe: Boolean,
    val speakerIds: List<Long> = emptyList(),
    val participantsIds: List<Long> = emptyList(),
    val participatedByMe: Boolean,
    @Embedded(prefix = "att_") val attachment: AttachmentEmbeddable?,
    val link: String?,
    val users: Map<Long, UserPreviewEntity> = emptyMap(),

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
        videoLink = link,
        users = users.mapValues { it.component2().toDto() },

        ownedByMe = ownedByMe,
        isLikeLoading = isLikeLoading,
        likes = likes,

        isTakingPartLoading = isTakingPartLoading,
    )

    private fun toDto(entity: EventTypeEntity): EventType = EventType.valueOf(entity.name)

    companion object {
        fun fromDto(dto: Event) = with(dto) {
            EventEntity(
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
                link = videoLink,
                users = users.mapValues { UserPreviewEntity.fromDto(it.component2()) },

                ownedByMe = ownedByMe,
                isLikeLoading = isLikeLoading,
                likes = likes,

                isTakingPartLoading = isTakingPartLoading,
            )
        }

        private fun fromDto(dto: EventType): EventTypeEntity =
            EventTypeEntity.valueOf(dto.name)
    }
}

enum class EventTypeEntity {
    ONLINE, OFFLINE
}