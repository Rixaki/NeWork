package com.example.nework.dto

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int,
    @SerializedName("login")
    val login: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("avatar")
    val avatar: String?
)

data class UserPreview(
    @SerializedName("avatar")
    val avatar: String = "404",
    @SerializedName("name")
    val name: String? = ""
)

data class SelectableUser(
    val id: Int,
    val user: UserPreview,
    val isPicked: Boolean
)
