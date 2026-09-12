package com.facebook.clone.data

import com.facebook.clone.model.*

object MockDataProvider {

    val currentUser = User(
        id = "user_me",
        firstName = "Bartosz",
        lastName = "Miazek",
        avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80",
        isOnline = true
    )

    val stories = listOf(
        Story(
            id = "story_me",
            author = currentUser,
            mediaUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=600&auto=format&fit=crop&q=80",
            isMyStory = true
        ),
        Story(
            id = "story_1",
            author = User("u1", "Anna", "Kowalska", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80", true),
            mediaUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=600&auto=format&fit=crop&q=80",
            isSeen = false
        ),
        Story(
            id = "story_2",
            author = User("u2", "Jan", "Nowak", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150&auto=format&fit=crop&q=80", false),
            mediaUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80",
            isSeen = false
        ),
        Story(
            id = "story_3",
            author = User("u3", "Zofia", "Wiśniewska", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&auto=format&fit=crop&q=80", true),
            mediaUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=600&auto=format&fit=crop&q=80",
            isSeen = true
        ),
        Story(
            id = "story_4",
            author = User("u4", "Michał", "Wójcik", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80", false),
            mediaUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80",
            isSeen = true
        )
    )

    val friendSuggestions = listOf(
        FriendSuggestion(
            id = "fs_1",
            user = User("u5", "Katarzyna", "Zielińska", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80"),
            mutualFriendsCount = 14
        ),
        FriendSuggestion(
            id = "fs_2",
            user = User("u6", "Tomasz", "Kamiński", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&auto=format&fit=crop&q=80"),
            mutualFriendsCount = 3
        ),
        FriendSuggestion(
            id = "fs_3",
            user = User("u7", "Magdalena", "Lewandowska", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&auto=format&fit=crop&q=80"),
            mutualFriendsCount = 8
        )
    )

    val reels = listOf(
        ReelItem(
            id = "reel_1",
            author = User("u8", "Tech Polska", "", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150&auto=format&fit=crop&q=80"),
            videoUrl = "",
            thumbnailUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=500&auto=format&fit=crop&q=80",
            viewsCount = "12.4K",
            caption = "Nowości w AI na 2026 rok 🚀 #tech #ai"
        ),
        ReelItem(
            id = "reel_2",
            author = User("u9", "Podróże Małe i Duże", "", "https://images.unsplash.com/photo-1527631746610-bca00a040d60?w=150&auto=format&fit=crop&q=80"),
            videoUrl = "",
            thumbnailUrl = "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=500&auto=format&fit=crop&q=80",
            viewsCount = "89.1K",
            caption = "Niesamowity zachód słońca w Tatrach 🏔️ #gory #polska"
        ),
        ReelItem(
            id = "reel_3",
            author = User("u10", "Gotuj ze smakiem", "", "https://images.unsplash.com/photo-1556910103-1c02745aae4d?w=150&auto=format&fit=crop&q=80"),
            videoUrl = "",
            thumbnailUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500&auto=format&fit=crop&q=80",
            viewsCount = "45.8K",
            caption = "Najlepsza domowa pizza w 15 minut 🍕 #food #recipe"
        )
    )

    val initialPosts = listOf(
        Post(
            id = "post_1",
            author = User("u1", "Anna", "Kowalska", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80"),
            content = "Piękny weekend w górach! Pogoda dopisała idealnie, szlaki były puste i widoki zapierały dech w piersiach. Kto jeszcze kocha jesień w Tatrach? 🍂⛰️",
            createdAt = "2 godz. temu",
            privacy = "PUBLIC",
            media = listOf(
                PostMedia("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&auto=format&fit=crop&q=80")
            ),
            myReaction = null,
            topReactions = listOf(ReactionType.LOVE, ReactionType.LIKE),
            stats = PostStats(reactionsCount = 42, commentsCount = 8, sharesCount = 3),
            comments = listOf(
                Comment("c1", User("u2", "Jan", "Nowak", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150&auto=format&fit=crop&q=80"), "Cudowne widoki! Który to szlak?", "1 godz. temu", 3, true),
                Comment("c2", User("u3", "Zofia", "Wiśniewska", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&auto=format&fit=crop&q=80"), "Super zdjęcie Aniu! Pozdrawiam serdecznie ❤️", "45 min temu", 1, false)
            )
        ),
        Post(
            id = "post_2",
            author = User("u2", "Jan", "Nowak", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150&auto=format&fit=crop&q=80"),
            content = "Właśnie ukończyłem refaktoryzację architektury w projekcie! GraphQL Federation + ETag Caching działa niesamowicie szybko. Czas odpowiedzi spadł o 80%! 🚀💻",
            createdAt = "4 godz. temu",
            privacy = "FRIENDS",
            media = emptyList(),
            myReaction = ReactionType.LIKE,
            topReactions = listOf(ReactionType.LIKE, ReactionType.WOW),
            stats = PostStats(reactionsCount = 19, commentsCount = 4, sharesCount = 1),
            comments = listOf(
                Comment("c3", currentUser, "Świetna robota Janek! Gratulacje 👏", "3 godz. temu", 2, false)
            )
        ),
        Post(
            id = "post_3",
            author = User("u3", "Zofia", "Wiśniewska", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&auto=format&fit=crop&q=80"),
            content = "Nowy członek rodziny! Poznajcie Lunę 🐶🐾 Jest niesamowicie energiczna i uwielbia spacery po parku.",
            createdAt = "Wczoraj o 18:30",
            privacy = "PUBLIC",
            media = listOf(
                PostMedia("https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=800&auto=format&fit=crop&q=80")
            ),
            myReaction = ReactionType.LOVE,
            topReactions = listOf(ReactionType.LOVE, ReactionType.CARE),
            stats = PostStats(reactionsCount = 128, commentsCount = 24, sharesCount = 6),
            comments = emptyList()
        )
    )
}
