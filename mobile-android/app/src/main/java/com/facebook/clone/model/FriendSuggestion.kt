package com.facebook.clone.model

data class FriendSuggestion(
    val id: String,
    val user: User,
    val mutualFriendsCount: Int = 0
)
