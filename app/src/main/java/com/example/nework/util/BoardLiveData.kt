package com.example.nework.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class BoardLiveData(
    val daySource: LiveData<String>,
    val timeSource: LiveData<String>,
) : MediatorLiveData<String>() {
    private var day: String = "__.__.____"
    private var time: String = "__:__"
    private fun combine(day: String, time: String): String = "$day $time"

    init {
        super.addSource(daySource) {
            day = it
            value = combine(day, time)
        }
        super.addSource(timeSource) {
            time = it
            value = combine(day, time)
        }
    }
}