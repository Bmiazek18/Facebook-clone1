package com.facebook.clone.model

data class Story(
    val id: String,
    val author: User,
    val mediaUrl: String,
    val isSeen: Boolean = false,
    val isMyStory: Boolean = false
)
