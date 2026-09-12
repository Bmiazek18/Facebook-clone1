package com.facebook.clone.model

data class ReelItem(
    val id: String,
    val author: User,
    val videoUrl: String,
    val thumbnailUrl: String,
    val viewsCount: String,
    val caption: String
)
