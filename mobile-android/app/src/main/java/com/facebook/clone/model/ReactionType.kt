package com.facebook.clone.model

import androidx.compose.ui.graphics.Color
import com.facebook.clone.theme.*

enum class ReactionType(
    val emoji: String,
    val label: String,
    val color: Color
) {
    LIKE("👍", "Lubię to!", ReactionLike),
    LOVE("❤️", "Super", ReactionLove),
    CARE("🥰", "Trzymaj się", ReactionCare),
    HAHA("😆", "Haha", ReactionHaha),
    WOW("😮", "Wow", ReactionWow),
    SAD("😢", "Przykro mi", ReactionSad),
    ANGRY("😡", "Wrr", ReactionAngry);

    companion object {
        fun fromString(value: String?): ReactionType? {
            return when (value?.lowercase()) {
                "like" -> LIKE
                "love" -> LOVE
                "care" -> CARE
                "haha" -> HAHA
                "wow" -> WOW
                "sad" -> SAD
                "angry" -> ANGRY
                else -> null
            }
        }
    }
}
