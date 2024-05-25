package com.example.nework.model

data class ResponceState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val lastErrorAction: String = "Api errors were not detected."
)

/*
data class UserListInPostState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val userListIds: List<Int> = emptyList(),
    val userList: List<User> = emptyList(),
    val lastErrorAction: String = "Api errors were not detected."
)

data class UserListInEventState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val partListIds: List<Int> = emptyList(),
    val partList: List<User> = emptyList(),
    val speakListIds: List<Int> = emptyList(),
    val speakList: List<User> = emptyList(),
    val lastErrorAction: String = "Api errors were not detected."
)
 */


