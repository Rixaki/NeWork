package com.example.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WallByUserRemoteKeyEntity(
    @PrimaryKey val type: KeyType,
    val id: Int,
) {
    enum class KeyType {
        AFTER, //upper post
        BEFORE //downer post
    }
}
