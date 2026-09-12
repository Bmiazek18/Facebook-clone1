package com.facebook.clone.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.facebook.clone.model.Story
import com.facebook.clone.model.User
import com.facebook.clone.theme.FacebookBlue

@Composable
fun StoriesSection(
    currentUser: User,
    stories: List<Story>,
    onAddStoryClick: () -> Unit = {},
    onStoryClick: (Story) -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "Add Story" Card
            item {
                CreateStoryCard(
                    currentUser = currentUser,
                    onClick = onAddStoryClick
                )
            }

            // Friends' Story Cards
            items(stories.filterNot { it.isMyStory }, key = { it.id }) { story ->
                StoryItemCard(
                    story = story,
                    onClick = { onStoryClick(story) }
                )
            }
        }
    }
}

@Composable
fun CreateStoryCard(
    currentUser: User,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .width(105.dp)
            .height(180.dp)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top 70% Avatar Image
                AsyncImage(
                    model = currentUser.avatarUrl,
                    contentDescription = currentUser.fullName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                )

                // Bottom 30% Label area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = "Utwórz relację",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // Blue Plus Circle Icon in the center intersection
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 35.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .background(FacebookBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Dodaj",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun StoryItemCard(
    story: Story,
    onClick: () -> Unit
) {
    val borderBrush = if (!story.isSeen) {
        Brush.linearGradient(listOf(Color(0xFF1877F2), Color(0xFF00C6FF)))
    } else {
        Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.5f), Color.Gray.copy(alpha = 0.5f)))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(105.dp)
            .height(180.dp)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full background Story Media
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim gradient for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Top Left: Author Avatar with Blue Gradient Ring
            AsyncImage(
                model = story.author.avatarUrl,
                contentDescription = story.author.fullName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(2.dp, borderBrush, CircleShape)
            )

            // Bottom Left: Author Name
            Text(
                text = story.author.fullName,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }
    }
}
