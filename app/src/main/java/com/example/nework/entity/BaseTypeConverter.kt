package com.example.nework.entity

import androidx.room.TypeConverter
import com.example.nework.dto.Attachment
import com.example.nework.dto.AttachmentType
import com.example.nework.dto.EventType
import com.google.gson.Gson
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import com.google.gson.reflect.TypeToken
import java.io.Serializable


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

    //TODO: ERROR - Platform declaration clash:
    // The following declarations have the same JVM signature
    // (listToJson(Ljava/util/List;)Ljava/lang/String;):
    // fun listToJson(list: List<UserPreviewEntity>?): String defined in com.example.nework.entity.BaseTypeConverter
    // fun listToJson(list: List<Int>?): String defined in com.example.nework.entity.BaseTypeConverter
    //JSON CONVERTERS
    @TypeConverter
    fun listIntToJson(list: List<Int>?): String = Gson().toJson(list)

    @TypeConverter
    fun jsonToListInt(json: String?): List<Int> =
        Gson().fromJson(json, object : TypeToken<List<Int>>() {}.type) ?: emptyList()

    //identifiable not need due to non-primitive type in list (maybe?)
    @TypeConverter
    fun listUsersToJson(list: List<UserPreviewEntity>?): String = Gson().toJson(list)

    @TypeConverter
    fun jsonToListUser(json: String): List<UserPreviewEntity> =
        Gson().fromJson(json, object : TypeToken<List<UserPreviewEntity>>() {}.type) ?: emptyList()

    //EVENT CONVERTERS
    @TypeConverter
    fun toDto(evTypeEnt: EventTypeEntity): EventType =
        EventType.valueOf(evTypeEnt.name)

    @TypeConverter
    fun fromDto(evType: EventType): EventTypeEntity =
        EventTypeEntity.valueOf(evType.name)

}