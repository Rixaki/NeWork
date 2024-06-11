package com.example.nework.entity

import androidx.room.Entity
import com.example.nework.dto.UserPreview

@Entity
data class UserPreviewEntity(
    val avatar: String,
    val name: String?
) {
    fun toDto(): UserPreview = UserPreview(
        avatar = avatar,
        name = name
    )

    companion object {
        fun fromDto(dto: UserPreview): UserPreviewEntity =
            with(dto) {
                UserPreviewEntity(
                    avatar = avatar,
                    name = name
                )
            }
    }
}