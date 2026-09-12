package com.facebook.clone.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.facebook.clone.model.User
import com.facebook.clone.theme.FeelingYellow
import com.facebook.clone.theme.LiveVideoRed
import com.facebook.clone.theme.PhotoVideoGreen

@Composable
fun CreatePostBox(
    currentUser: User,
    onBoxClick: () -> Unit = {},
    onPhotoClick: () -> Unit = {},
    onLiveClick: () -> Unit = {},
    onFeelingClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Upper row: Avatar + Text prompt button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = currentUser.avatarUrl,
                    contentDescription = currentUser.fullName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onBoxClick)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Co słychać, ${currentUser.firstName}?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(4.dp))

            // Lower row: Action shortcut buttons (Live, Photo, Feeling)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CreatePostActionItem(
                    icon = Icons.Default.Videocam,
                    iconTint = LiveVideoRed,
                    label = "Transmisja",
                    onClick = onLiveClick
                )
                CreatePostActionItem(
                    icon = Icons.Default.PhotoLibrary,
                    iconTint = PhotoVideoGreen,
                    label = "Zdjęcie",
                    onClick = onPhotoClick
                )
                CreatePostActionItem(
                    icon = Icons.Default.EmojiEmotions,
                    iconTint = FeelingYellow,
                    label = "Nastrój",
                    onClick = onFeelingClick
                )
            }
        }
    }
}

@Composable
fun RowScope.CreatePostActionItem(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
