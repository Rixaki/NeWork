package com.example.nework.model

data class ResponceState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val lastErrorAction: String = "Api errors were not detected."
)


