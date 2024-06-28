package com.example.nework.model

import com.example.nework.dto.Coords

data class EventModel(
    val coords: Coords? = null,
    val speakerIds: List<Long> = emptyList(),
    val participantsIds: List<Long> = emptyList()
)


