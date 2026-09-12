package com.facebook.clone.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facebook.clone.ui.home.components.*
import com.facebook.clone.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToMessenger: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Pagination trigger when scrolling near the end
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= totalItemsCount - 2 && !uiState.isFetchingMore && uiState.hasMore
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMorePosts()
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                onSearchClick = onNavigateToSearch,
                onMessengerClick = onNavigateToMessenger,
                onNotificationsClick = onNavigateToNotifications
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshFeed() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.posts.isEmpty()) {
                // Skeleton loading state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) {
                        PostSkeleton()
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Create Post Box ("Co słychać?")
                    item(key = "create_post_box") {
                        CreatePostBox(
                            currentUser = uiState.currentUser,
                            onBoxClick = {
                                viewModel.addNewPost("Właśnie testuję nową wersję mobilną w Jetpack Compose! Działa super płynnie 🚀")
                            }
                        )
                    }

                    // 2. Stories Section (Add Story + Friends Stories)
                    item(key = "stories_section") {
                        StoriesSection(
                            currentUser = uiState.currentUser,
                            stories = uiState.stories,
                            onAddStoryClick = { /* Dodaj relację */ },
                            onStoryClick = { story -> /* Odtwórz relację */ }
                        )
                    }

                    // 3. Main Feed Posts with injected widgets
                    itemsIndexed(
                        items = uiState.posts,
                        key = { _, post -> post.id }
                    ) { index, post ->
                        PostItemCard(
                            post = post,
                            onReactionClick = {
                                viewModel.toggleReaction(
                                    post.id,
                                    com.facebook.clone.model.ReactionType.LIKE
                                )
                            },
                            onReactionSelect = { reaction ->
                                viewModel.toggleReaction(post.id, reaction)
                            },
                            onCommentClick = {
                                viewModel.openComments(post)
                            }
                        )

                        // Inject "People You May Know" widget after 2nd post
                        if (index == 1 && uiState.friendSuggestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            PeopleYouMayKnowSection(
                                suggestions = uiState.friendSuggestions,
                                onAddFriendClick = { suggestion -> viewModel.removeFriendSuggestion(suggestion.id) },
                                onRemoveClick = { suggestion -> viewModel.removeFriendSuggestion(suggestion.id) }
                            )
                        }

                        // Inject "Reels Gallery" carousel after 5th post
                        if (index == 4 && uiState.reels.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ReelsGallerySection(
                                reels = uiState.reels,
                                onReelClick = { reel -> /* Otwórz odtwarzacz rolek */ }
                            )
                        }
                    }

                    // 4. Loading indicator at the bottom (Pagination)
                    if (uiState.isFetchingMore) {
                        item(key = "fetching_more_indicator") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }
                    }

                    // 5. End of Feed message
                    if (!uiState.hasMore && !uiState.isFetchingMore && uiState.posts.isNotEmpty()) {
                        item(key = "end_of_feed") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "To już wszystkie posty z Twojej tablicy",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Comments Bottom Sheet Modal
            uiState.selectedPostForComments?.let { selectedPost ->
                CommentsBottomSheet(
                    post = selectedPost,
                    currentUser = uiState.currentUser,
                    onDismiss = { viewModel.closeComments() },
                    onAddComment = { text ->
                        viewModel.addComment(selectedPost.id, text)
                    }
                )
            }
        }
    }
}
