package com.facebook.clone.model

data class PostMedia(
    val url: String,
    val isVideo: Boolean = false,
    val altText: String = ""
)

data class PostStats(
    val reactionsCount: Int,
    val commentsCount: Int,
    val sharesCount: Int
)

data class Post(
    val id: String,
    val author: User,
    val content: String,
    val createdAt: String,
    val privacy: String = "PUBLIC", // PUBLIC, FRIENDS, ONLY_ME
    val media: List<PostMedia> = emptyList(),
    val myReaction: ReactionType? = null,
    val topReactions: List<ReactionType> = listOf(ReactionType.LIKE, ReactionType.LOVE),
    val stats: PostStats = PostStats(0, 0, 0),
    val comments: List<Comment> = emptyList()
)
