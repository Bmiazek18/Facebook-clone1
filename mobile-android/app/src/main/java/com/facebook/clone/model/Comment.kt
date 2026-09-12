package com.facebook.clone.model

data class Comment(
    val id: String,
    val author: User,
    val content: String,
    val createdAt: String,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false
)
