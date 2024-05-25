package com.example.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.nework.dto.User

@Entity
@TypeConverters(BaseTypeConverter::class)
data class UserEntity (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val login: String,
    val name: String,
    val avatar: String?
) {
    fun toDto(): User = User(
        id = id,
        login = login,
        name = name,
        avatar = avatar
    )

    companion object {
        fun fromDto(dto: User): UserEntity = with(dto) {
            UserEntity(
                id = id,
                login = login,
                name = name,
                avatar = avatar
            )
        }
    }
}