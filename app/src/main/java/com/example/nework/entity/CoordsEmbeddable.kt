package com.example.nework.entity

import androidx.room.Entity
import com.example.nework.dto.Coords

@Entity
data class CoordsEmbeddable (
    val lat: Double,
    val long: Double
) {
    fun toDto(): Coords = Coords(
        lat = lat,
        long = long
    )

    companion object {
        fun fromDto(dto: Coords): CoordsEmbeddable = with(dto) {
            CoordsEmbeddable(
                lat = lat,
                long = long
            )
        }
    }
}