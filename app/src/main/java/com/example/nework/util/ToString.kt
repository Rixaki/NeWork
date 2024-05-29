package com.example.nework.util

import com.example.nework.dto.DATE_FORMAT
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log
import kotlin.math.pow

fun countToString(x: Int): String {
    if (x == 0) {
        return "0"
    } else {
        when (log(x + 0.0, 10.0).toInt()) {
            0, 1, 2 -> return x.toString()
            3, 4, 5 -> return ("%.2f".format((x + 0.0) * (10.0).pow(-3.0))) + "K"
            6, 7, 8 -> return ("%.2f".format((x + 0.0) * (10.0).pow(-6.0))) + "M"
            else -> return if (x >= 1_000_000) ">=1B" else "count error"
        }
    }
}