package com.example.nework.entity

import androidx.room.TypeConverter
import com.example.nework.dto.Attachment
import com.example.nework.dto.AttachmentType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class BaseTypeConverter {
    @TypeConverter
    fun toDto(attEnt: AttachmentEmbeddable): Attachment =
        Attachment(
            url = attEnt.url,
            type = toDto(attEnt.type)
        )

    @TypeConverter
    fun fromDto(att: Attachment): AttachmentEmbeddable =
        AttachmentEmbeddable(
            url = att.url,
            type = fromDto(att.type)
        )

    @TypeConverter
    fun toDto(attEntType: AttachmentTypeEntity): AttachmentType =
        AttachmentType.valueOf(attEntType.name)


    @TypeConverter
    fun fromDto(attType: AttachmentType): AttachmentTypeEntity =
        AttachmentTypeEntity.valueOf(attType.name)

    //Coords do NOT need to be converted in the BaseTypeConverter... maybe...

    //T: Int, UserPreview
    @TypeConverter
    fun <T> listToJson(list: List<T>?): String? = Gson().toJson(list)

    @TypeConverter
    fun <T> jsonToList(json: String?): List<T> =
        Gson().fromJson(json, object : TypeToken<List<T>>(){}.type) ?: emptyList()
}