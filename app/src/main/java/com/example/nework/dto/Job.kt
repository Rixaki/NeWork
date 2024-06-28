package com.example.nework.dto


import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

val DATE_FORMAT_JOB = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

data class Job(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("position")
    val position: String,
    @SerializedName("start")
    val start: String = DATE_FORMAT_JOB.format(Date()),
    @SerializedName("finish")
    val finish: String? = null,
    @SerializedName("link")
    val link: String? = null,

    val ownedByMe: Boolean = false,
) {
    /*
    fun toEpoch(str: String): Long =
        (DATE_FORMAT.parse(str)!!.time) / 1000L

    companion object {
        //https://stackoverflow.com/questions/47250263/kotlin-convert-timestamp-to-datetime
        fun fromEpochFull(epoch: Long): String {
            try {
                val sdf = DATE_FORMAT
                val netDate = Date(epoch * 1000L)
                return sdf.format(netDate)
            } catch (e: Exception) {
                return e.toString()
            }
        }

        fun fromEpochDate(epoch: Long): String {
            try {
                val sdf = DATE_FORMAT_JOB
                val netDate = Date(epoch * 1000L)
                return sdf.format(netDate)
            } catch (e: Exception) {
                return e.toString()
            }
        }
    }
     */
}