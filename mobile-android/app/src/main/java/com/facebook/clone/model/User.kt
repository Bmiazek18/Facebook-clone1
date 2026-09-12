package com.facebook.clone.model

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String,
    val isOnline: Boolean = false
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}
