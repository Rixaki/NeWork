package com.example.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.nework.dto.Attachment
import com.example.nework.dto.AttachmentType
import com.example.nework.dto.Coords
import com.example.nework.dto.Post

@Entity
@TypeConverters(BaseTypeConverter::class)
data class PostEntity (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val authorId: Int,
    val author: String,
    val authorJob: String?,
    val authorAvatar: String?,
    val content: String,
    val published: String,
    @Embedded val coords: Coords?,
    val link: String?,
    val mentionIds: List<Int> = emptyList(),
    val mentionedMe: Boolean,
    val likeOwnerIds: List<Int> = emptyList(),
    val likedByMe: Boolean,
    @Embedded(prefix= "att_") val attachment: AttachmentEmbeddable? = null,
    val users: List<UserPreviewEntity> = emptyList(),

    val ownedByMe: Boolean = false,
    val isLikeLoading: Boolean = false,
    val likes: Int = likeOwnerIds.size
) {
    fun toDto(): Post = Post (
        id = id,
        authorId = authorId,
        author = author,
        authorJob = authorJob,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        coords = coords,
        videoLink = link,
        mentionIds = mentionIds,
        mentionedMe = mentionedMe,
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        attachment = attachment?.toDto(),
        users = users.map{ it.toDto() },

        ownedByMe = ownedByMe,
        isLikeLoading = isLikeLoading,
        likes = likes
    )

    companion object {
        fun fromDto(dto: Post): PostEntity = with(dto) {
            PostEntity(
                id = id,
                authorId = authorId,
                author = author,
                authorJob = authorJob,
                authorAvatar = authorAvatar,
                content = content,
                published = published,
                coords = coords,
                link = videoLink,
                mentionIds = mentionIds,
                mentionedMe = mentionedMe,
                likeOwnerIds = likeOwnerIds,
                likedByMe = likedByMe,
                users = users.map{ UserPreviewEntity.fromDto(it) },
                attachment = attachment?.let {
                    AttachmentEmbeddable.fromDto(it)
                },

                ownedByMe = ownedByMe,
                isLikeLoading = isLikeLoading,
                likes = likes,
            )
        }
    }
}

data class AttachmentEmbeddable(
    val url: String,
    val type: AttachmentTypeEntity
) {
    fun toDto(): Attachment = Attachment(
        url = url,
        type = AttachmentType.valueOf(type.name)
    )

    companion object {
        fun fromDto(dto: Attachment): AttachmentEmbeddable = with(dto) {
            AttachmentEmbeddable(
                url = url,
                type = AttachmentTypeEntity.valueOf(type.name)
            )
        }
    }
}

enum class AttachmentTypeEntity {
    IMAGE, VIDEO, AUDIO
}