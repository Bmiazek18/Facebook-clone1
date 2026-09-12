package com.facebook.clone.ui.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.facebook.clone.model.Post
import com.facebook.clone.model.ReactionType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItemCard(
    post: Post,
    onReactionClick: () -> Unit,
    onReactionSelect: (ReactionType) -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    var showReactionPicker by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Avatar, Name, Time, Privacy, More Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.author.avatarUrl,
                    contentDescription = post.author.fullName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.author.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = post.createdAt,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Publiczny",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                IconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Więcej",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Post Text Content
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            // Media Preview (Images/Video)
            if (post.media.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = post.media.first().url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                )
            }

            // Reaction & Comments Stats Summary
            if (post.stats.reactionsCount > 0 || post.stats.commentsCount > 0 || post.stats.sharesCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Top Emojis + Count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        post.topReactions.take(2).forEach { reaction ->
                            Text(text = reaction.emoji, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.stats.reactionsCount.toString(),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Right: Comments and Shares
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (post.stats.commentsCount > 0) {
                            Text(
                                text = "${post.stats.commentsCount} komentarzy",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable(onClick = onCommentClick)
                            )
                        }
                        if (post.stats.sharesCount > 0) {
                            Text(
                                text = "${post.stats.sharesCount} udostępnień",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Action Buttons: Like, Comment, Share
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Like button with Long-Press support for reaction picker
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .combinedClickable(
                                onClick = {
                                    if (showReactionPicker) showReactionPicker = false
                                    else onReactionClick()
                                },
                                onLongClick = {
                                    showReactionPicker = true
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (post.myReaction != null) {
                            Text(text = post.myReaction.emoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = post.myReaction.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = post.myReaction.color
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.ThumbUp,
                                contentDescription = "Lubię to",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lubię to!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Comment button
                    PostActionButton(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        label = "Komentarz",
                        onClick = onCommentClick
                    )

                    // Share button
                    PostActionButton(
                        icon = Icons.Outlined.Share,
                        label = "Udostępnij",
                        onClick = onShareClick
                    )
                }

                // Reaction Picker Popup Overlay
                if (showReactionPicker) {
                    ReactionPickerPopup(
                        visible = true,
                        onReactionSelect = {
                            showReactionPicker = false
                            onReactionSelect(it)
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 10.dp, y = (-48).dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.PostActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
