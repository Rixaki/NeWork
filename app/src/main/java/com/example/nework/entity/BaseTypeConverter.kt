package com.example.nework.entity

import androidx.room.TypeConverter
import com.example.nework.dto.Attachment
import com.example.nework.dto.AttachmentType
import com.example.nework.dto.EventType
import com.google.gson.Gson
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
    //fun listToJson(list: List<Int>?): Identifiable<String> = IdentifiableImpl(Gson().toJson(list))
    fun listToJson(list: List<Int>?): String = Gson().toJson(list)

    @TypeConverter
    //fun jsonToList(json: Identifiable<String>): List<Int> =
    //    Gson().fromJson(IdentifiableImpl(json), object : TypeToken<List<Int>>(){}.type) ?: emptyList()
    fun jsonToList(json: String?): List<Int> =
        Gson().fromJson(json, object : TypeToken<List<Int>>(){}.type) ?: emptyList()

    //identifiable not need due to non-primitive type in list (maybe?)
    @TypeConverter
    fun listToJson(list: List<UserPreviewEntity>?): String = Gson().toJson(list)

    @TypeConverter
    fun jsonToList(json: String): List<UserPreviewEntity> =
        Gson().fromJson(json, object : TypeToken<List<UserPreviewEntity>>(){}.type) ?: emptyList()

    //EVENT CONVERTERS
    @TypeConverter
    fun toDto(evTypeEnt: EventTypeEntity): EventType =
        EventType.valueOf(evTypeEnt.name)

    @TypeConverter
    fun fromDto(evType: EventType): EventTypeEntity =
        EventTypeEntity.valueOf(evType.name)

}

//https://stackoverflow.com/questions/29268526/how-to-overcome-same-jvm-signature-error-when-implementing-a-java-interface
//from kotlin to java
interface Identifiable<ID : Serializable?> {
    val id: ID
    fun getId() : ID = id
}


/*
//from java to kotlin
class IdentifiableImpl(@JvmField var id: String) : Identifiable<String>
{
    override fun getId(): String {TODO("not implemented")}
}
 */
class IdentifiableImpl(override val id: String) : Identifiable<String>
{
    override fun getId(): String = id
}