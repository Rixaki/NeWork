package com.example.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nework.dto.Job
import com.google.gson.annotations.SerializedName

@Entity
data class JobEntity (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val position: String,
    val start: String,
    val finish: String,
    val link: String?,

    val ownedByMe: Boolean = false
) {
    fun toDto(): Job = Job (
        id = id,
        name = name,
        position = position,
        start = start,
        finish = finish,
        link = link,
        ownedByMe = ownedByMe
    )

    companion object {
        fun fromDto(dto: Job): JobEntity = with(dto) {
            JobEntity(
                id = id,
                name = name,
                position = position,
                start = start,
                finish = finish,
                link = link,
                ownedByMe = ownedByMe
            )
        }
    }
}