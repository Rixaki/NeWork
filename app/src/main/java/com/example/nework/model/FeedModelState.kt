package com.example.nework.model

data class FeedModelState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val error: Boolean = false,
    val lastErrorAction: String = "Api errors were not detected."
) // client-api status