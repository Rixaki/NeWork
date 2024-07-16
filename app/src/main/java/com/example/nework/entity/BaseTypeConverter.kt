package com.example.nework.entity

import androidx.room.TypeConverter
import com.example.nework.dto.Attachment
import com.example.nework.dto.AttachmentType
import com.example.nework.dto.EventType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class BaseTypeConverter {
    @TypeConverter
    fun toDto(attEnt: AttachmentEmbeddable?): Attachment? = if (attEnt != null)
        Attachment(
            url = attEnt.url,
            type = toDto(attEnt.type)
        ) else null

    @TypeConverter
    fun fromDto(att: Attachment?): AttachmentEmbeddable? = if (att != null)
        AttachmentEmbeddable(
            url = att.url,
            type = fromDto(att.type)
        ) else null

    @TypeConverter
    fun toDto(attEntType: AttachmentTypeEntity): AttachmentType =
        AttachmentType.valueOf(attEntType.name)


    @TypeConverter
    fun fromDto(attType: AttachmentType): AttachmentTypeEntity =
        AttachmentTypeEntity.valueOf(attType.name)

    //Coords do NOT need to be converted in the BaseTypeConverter... maybe...

    //JSON CONVERTERS
    @TypeConverter
    fun listLongToJson(list: List<Long>?): String = Gson().toJson(list)

    @TypeConverter
    fun jsonToListLong(json: String?): List<Long> =
        Gson().fromJson(json, object : TypeToken<List<Long>>() {}.type) ?: emptyList()

    //identifiable not need due to non-primitive type in list (maybe?)
    @TypeConverter
    fun mapUsersToJson(map: Map<Long, UserPreviewEntity>): String = Gson().toJson(map)

    @TypeConverter
    fun jsonToMapUser(json: String): Map<Long, UserPreviewEntity> =
        Gson().fromJson(json, object : TypeToken<Map<Long, UserPreviewEntity>>() {}.type)
            ?: emptyMap()

    //EVENT CONVERTERS
    @TypeConverter
    fun toDto(evTypeEnt: EventTypeEntity): EventType =
        EventType.valueOf(evTypeEnt.name)

    @TypeConverter
    fun fromDto(evType: EventType): EventTypeEntity =
        EventTypeEntity.valueOf(evType.name)

}