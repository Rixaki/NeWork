package com.example.nework.util

import kotlin.math.log
import kotlin.math.pow

fun countToString(x: Int): String {
    return if (x == 0) {
        "0"
    } else {
        when (log(x + 0.0, 10.0).toInt()) {
            0, 1, 2 -> x.toString()
            3, 4, 5 -> ("%.2f".format((x + 0.0) * (10.0).pow(-3.0))) + "K"
            6, 7, 8 -> ("%.2f".format((x + 0.0) * (10.0).pow(-6.0))) + "M"
            else -> if (x >= 1_000_000) ">=1B" else "count error"
        }
    }
}