package com.example.nework.model

import com.example.nework.dto.Coords

data class EventModel(
    val coords: Coords? = null,
    val speakerIds: List<Int> = emptyList(),
    val participantsIds: List<Int> = emptyList()
)


