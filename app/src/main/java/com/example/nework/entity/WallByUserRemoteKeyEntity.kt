package com.example.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nework.dto.Attachment
import com.example.nework.dto.AttachmentType

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
