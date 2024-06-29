package com.example.nework.dto

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Long,
    @SerializedName("login")
    val login: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("avatar")
    val avatar: String? = "404"
)

data class UserPreview(
    @SerializedName("avatar")
    val avatar: String? = "404",
    @SerializedName("name")
    val name: String = ""
)

data class SelectableUser(
    val id: Long,
    val user: UserPreview,
    val isPicked: Boolean
)
