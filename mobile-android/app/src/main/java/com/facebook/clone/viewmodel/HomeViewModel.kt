package com.facebook.clone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.clone.data.MockDataProvider
import com.facebook.clone.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val currentUser: User = MockDataProvider.currentUser,
    val stories: List<Story> = emptyList(),
    val posts: List<Post> = emptyList(),
    val friendSuggestions: List<FriendSuggestion> = emptyList(),
    val reels: List<ReelItem> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isFetchingMore: Boolean = false,
    val hasMore: Boolean = true,
    val selectedPostForComments: Post? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadInitialFeed()
    }

    fun loadInitialFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Simulate network latency
            delay(400)
            _uiState.update {
                it.copy(
                    currentUser = MockDataProvider.currentUser,
                    stories = MockDataProvider.stories,
                    posts = MockDataProvider.initialPosts,
                    friendSuggestions = MockDataProvider.friendSuggestions,
                    reels = MockDataProvider.reels,
                    isLoading = false
                )
            }
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            delay(600)
            _uiState.update {
                it.copy(
                    posts = MockDataProvider.initialPosts,
                    isRefreshing = false,
                    hasMore = true
                )
            }
        }
    }

    fun loadMorePosts() {
        if (_uiState.value.isFetchingMore || !_uiState.value.hasMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingMore = true) }
            delay(800)

            val nextId = (_uiState.value.posts.size + 1).toString()
            val extraPost = Post(
                id = "post_$nextId",
                author = User("u_extra", "Piotr", "Kaczmarek", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80"),
                content = "Kolejny ciekawy dzień i nowe wyzwania. Pamiętajcie, żeby robić przerwy w pracy przed monitorem! ☕",
                createdAt = "Przed chwilą",
                privacy = "PUBLIC",
                stats = PostStats(reactionsCount = 7, commentsCount = 1, sharesCount = 0)
            )

            _uiState.update {
                it.copy(
                    posts = it.posts + extraPost,
                    isFetchingMore = false,
                    hasMore = it.posts.size < 6
                )
            }
        }
    }

    fun toggleReaction(postId: String, reaction: ReactionType) {
        _uiState.update { state ->
            val updatedPosts = state.posts.map { post ->
                if (post.id == postId) {
                    val isRemoving = post.myReaction == reaction
                    val newReaction = if (isRemoving) null else reaction
                    val delta = when {
                        isRemoving -> -1
                        post.myReaction == null -> 1
                        else -> 0
                    }
                    val newCount = (post.stats.reactionsCount + delta).coerceAtLeast(0)

                    post.copy(
                        myReaction = newReaction,
                        stats = post.stats.copy(reactionsCount = newCount)
                    )
                } else post
            }
            state.copy(posts = updatedPosts)
        }
    }

    fun addNewPost(content: String, mediaUrl: String? = null) {
        if (content.isBlank() && mediaUrl == null) return

        val newPost = Post(
            id = "post_${System.currentTimeMillis()}",
            author = _uiState.value.currentUser,
            content = content,
            createdAt = "Przed chwilą",
            privacy = "PUBLIC",
            media = if (mediaUrl != null) listOf(PostMedia(mediaUrl)) else emptyList(),
            stats = PostStats(reactionsCount = 0, commentsCount = 0, sharesCount = 0)
        )

        _uiState.update { it.copy(posts = listOf(newPost) + it.posts) }
    }

    fun openComments(post: Post) {
        _uiState.update { it.copy(selectedPostForComments = post) }
    }

    fun closeComments() {
        _uiState.update { it.copy(selectedPostForComments = null) }
    }

    fun addComment(postId: String, commentText: String) {
        if (commentText.isBlank()) return

        val newComment = Comment(
            id = "c_${System.currentTimeMillis()}",
            author = _uiState.value.currentUser,
            content = commentText,
            createdAt = "Przed chwilą",
            likesCount = 0
        )

        _uiState.update { state ->
            val updatedPosts = state.posts.map { post ->
                if (post.id == postId) {
                    post.copy(
                        comments = post.comments + newComment,
                        stats = post.stats.copy(commentsCount = post.stats.commentsCount + 1)
                    )
                } else post
            }

            val updatedSelected = if (state.selectedPostForComments?.id == postId) {
                state.selectedPostForComments.copy(
                    comments = state.selectedPostForComments.comments + newComment,
                    stats = state.selectedPostForComments.stats.copy(
                        commentsCount = state.selectedPostForComments.stats.commentsCount + 1
                    )
                )
            } else state.selectedPostForComments

            state.copy(posts = updatedPosts, selectedPostForComments = updatedSelected)
        }
    }

    fun removeFriendSuggestion(suggestionId: String) {
        _uiState.update { state ->
            state.copy(friendSuggestions = state.friendSuggestions.filterNot { it.id == suggestionId })
        }
    }
}
