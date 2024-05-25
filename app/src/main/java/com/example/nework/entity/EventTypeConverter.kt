package com.example.nework.entity

import androidx.room.TypeConverter
import com.example.nework.dto.EventType

class EventTypeConverter {
    @TypeConverter
    fun toDto(evTypeEnt: EventTypeEntity): EventType =
        EventType.valueOf(evTypeEnt.name)

    @TypeConverter
    fun fromDto(evType: EventType): EventTypeEntity =
        EventTypeEntity.valueOf(evType.name)
}