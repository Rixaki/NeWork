package com.example.nework.dto


import com.google.gson.annotations.SerializedName

data class Token(
    @SerializedName("id")
    val id: Int,
    @SerializedName("token")
    val token: String
)