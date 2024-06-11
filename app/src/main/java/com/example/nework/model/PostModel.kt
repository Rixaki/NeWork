package com.example.nework.model

import com.example.nework.dto.Coords

data class PostModel(
    val coords: Coords? = null,
    val mentionIds: List<Int> = emptyList()
)