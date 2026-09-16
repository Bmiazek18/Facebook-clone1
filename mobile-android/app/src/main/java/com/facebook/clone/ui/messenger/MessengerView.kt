package com.facebook.clone.ui.messenger

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.facebook.clone.theme.FacebookBlue
import kotlinx.coroutines.launch

// ==========================================
// 1. DATA MODELS
// ==========================================

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromMe: Boolean,
    val timestamp: String
)

data class ChatConversation(
    val id: String,
    val participantName: String,
    val participantAvatar: String,
    val isOnline: Boolean,
    val lastActive: String = "Aktywny(a) teraz",
    val lastMessage: String,
    val lastMessageTime: String,
    val isUnread: Boolean = false,
    val messages: List<ChatMessage> = emptyList()
)

data class StoryContact(
    val name: String,
    val avatarUrl: String,
    val isOnline: Boolean,
    val note: String? = null
)

// ==========================================
// 2. PALETA KOLORÓW MESSENGER DARK THEME
// ==========================================

private val MessengerBg = Color(0xFF000000)
private val MessengerSearchBg = Color(0xFF1E1E1E)
private val MessengerTextPrimary = Color(0xFFFFFFFF)
private val MessengerTextSecondary = Color(0xFF8A8D91)
private val MessengerOnlineGreen = Color(0xFF31A24C)
private val MessengerUnreadBlue = Color(0xFF1877F2)

private val MetaAiGradient = Brush.sweepGradient(
    colors = listOf(
        Color(0xFF00C6FF),
        Color(0xFF0072FF),
        Color(0xFFA033FF),
        Color(0xFFFF3366),
        Color(0xFF00C6FF)
    )
)

private val SentBubbleGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF0084FF),
        Color(0xFF00C6FF),
        Color(0xFFA033FF)
    )
)

// ==========================================
// 3. MOCK DATA
// ==========================================

private fun getInitialConversations(): List<ChatConversation> = listOf(
    ChatConversation(
        id = "1",
        participantName = "Carbonara \uD83D\uDE0E",
        participantAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150",
        isOnline = true,
        lastActive = "Aktywna teraz",
        lastMessage = "Wysłałaś(eś) zdjęcie.",
        lastMessageTime = "21:19",
        isUnread = false,
        messages = listOf(
            ChatMessage("m1", "Cześć! Masz ten przepis?", false, "21:10"),
            ChatMessage("m2", "Tak, właśnie robię kolację 🍝", true, "21:15"),
            ChatMessage("m3", "Wysłałaś(eś) zdjęcie.", true, "21:19")
        )
    ),
    ChatConversation(
        id = "2",
        participantName = "Łuków24 (Wiadomości)",
        participantAvatar = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=150",
        isOnline = false,
        lastActive = "Aktywny 40 min temu",
        lastMessage = "Użytkownik Łuków24 wysłał załącznik.",
        lastMessageTime = "20:23",
        isUnread = true,
        messages = listOf(
            ChatMessage("m4", "Wiadomości regionalne na dziś:", false, "20:20"),
            ChatMessage("m5", "Użytkownik Łuków24 wysłał załącznik.", false, "20:23")
        )
    ),
    ChatConversation(
        id = "3",
        participantName = "Faworyt (11-30)",
        participantAvatar = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150",
        isOnline = true,
        lastActive = "Aktywny teraz",
        lastMessage = "bo tyle samo na miejscu zarobię",
        lastMessageTime = "19:15",
        isUnread = true,
        messages = listOf(
            ChatMessage("m6", "Siema, jak tam zlecenia?", true, "19:00"),
            ChatMessage("m7", "bo tyle samo na miejscu zarobię", false, "19:15")
        )
    ),
    ChatConversation(
        id = "4",
        participantName = "Krystian Wojda",
        participantAvatar = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150",
        isOnline = false,
        lastActive = "Aktywny 2 godz. temu",
        lastMessage = "Legancko \uD83D\uDE4F\uD83D\uDC4C",
        lastMessageTime = "18:28",
        isUnread = true,
        messages = listOf(
            ChatMessage("m8", "Wdrożyłem ten nowy moduł!", true, "18:25"),
            ChatMessage("m9", "Legancko \uD83D\uDE4F\uD83D\uDC4C", false, "18:28")
        )
    ),
    ChatConversation(
        id = "5",
        participantName = "Towarzystwo Fijałetów",
        participantAvatar = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150",
        isOnline = true,
        lastActive = "Aktywny teraz",
        lastMessage = "Łysy wegetarianin: A to ciekawe w sumie",
        lastMessageTime = "17:52",
        isUnread = false,
        messages = listOf(
            ChatMessage("m10", "Łysy wegetarianin: A to ciekawe w sumie", false, "17:52")
        )
    ),
    ChatConversation(
        id = "6",
        participantName = "Dawid Czerniewicz",
        participantAvatar = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150",
        isOnline = true,
        lastActive = "Aktywny teraz",
        lastMessage = "Zapłacił bym ci za dojazd i sędziowanie...",
        lastMessageTime = "10:59",
        isUnread = false,
        messages = listOf(
            ChatMessage("m11", "Zapłacił bym ci za dojazd i sędziowanie...", false, "10:59")
        )
    )
)

private fun getMockStories(): List<StoryContact> = listOf(
    StoryContact("Julek", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150", true, "Kawa time ☕"),
    StoryContact("Maciej", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150", true, null),
    StoryContact("Sebastian", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=150", true, "W trasie 🚗"),
    StoryContact("Klaudia", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150", false, null)
)

// ==========================================
// 4. MAIN MESSENGER VIEW CONTAINER
// ==========================================

@Composable
fun MessengerView(
    onNavigateBack: () -> Unit = {}
) {
    var conversations by remember { mutableStateOf(getInitialConversations()) }
    var selectedConversationId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    val activeConversation = remember(selectedConversationId, conversations) {
        conversations.find { it.id == selectedConversationId }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MessengerBg)
    ) {
        // Inbox List
        Scaffold(
            containerColor = MessengerBg,
            topBar = {
                MessengerHeader(onNavigateBack = onNavigateBack)
            },
            bottomBar = {
                MessengerBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelect = { selectedTab = it }
                )
            },
            floatingActionButton = {
                // Wielobarwny przycisk Meta AI
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF242526))
                        .clickable { /* Otwórz Meta AI */ },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MetaAiGradient)
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 1. Wyszukiwarka Meta AI
                item(key = "search_bar") {
                    MessengerSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                }

                // 2. Pasek relacji i notatek
                item(key = "stories_carousel") {
                    StoriesAndActiveRow()
                }

                item(key = "separator") {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 3. Lista konwersacji
                val filtered = conversations.filter {
                    it.participantName.contains(searchQuery, ignoreCase = true) ||
                    it.lastMessage.contains(searchQuery, ignoreCase = true)
                }

                items(filtered, key = { it.id }) { conv ->
                    MessengerConversationRow(
                        conversation = conv,
                        onClick = {
                            conversations = conversations.map {
                                if (it.id == conv.id) it.copy(isUnread = false) else it
                            }
                            selectedConversationId = conv.id
                        }
                    )
                }
            }
        }

        // Aktywne okno rozmowy (Chat Detail)
        AnimatedVisibility(
            visible = activeConversation != null,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            activeConversation?.let { conversation ->
                BackHandler {
                    selectedConversationId = null
                }
                MessengerChatDetail(
                    conversation = conversation,
                    onBackClick = { selectedConversationId = null },
                    onSendMessage = { newText ->
                        val newMessage = ChatMessage(
                            id = "msg_${System.currentTimeMillis()}",
                            text = newText,
                            isFromMe = true,
                            timestamp = "Teraz"
                        )
                        conversations = conversations.map { c ->
                            if (c.id == conversation.id) {
                                c.copy(
                                    lastMessage = "Ty: $newText",
                                    lastMessageTime = "Teraz",
                                    messages = c.messages + newMessage
                                )
                            } else c
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// 5. NAGŁÓWEK (messenger + powrót + aparat + edycja)
// ==========================================

@Composable
private fun MessengerHeader(
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Powrót do Facebooka",
                    tint = MessengerTextPrimary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "messenger",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = MessengerTextPrimary,
                letterSpacing = (-0.5).sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderRoundButton(icon = Icons.Default.CameraAlt, contentDescription = "Aparat")
            HeaderRoundButton(icon = Icons.Default.Edit, contentDescription = "Nowa wiadomość")
        }
    }
}

@Composable
private fun HeaderRoundButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF242526))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MessengerTextPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ==========================================
// 6. PASEK WYSZUKIWANIA Z META AI
// ==========================================

@Composable
private fun MessengerSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MessengerSearchBg)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Wielokolorowy pierścień Meta AI
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MetaAiGradient)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "Zapytaj Meta AI lub szukaj",
                    color = MessengerTextSecondary,
                    fontSize = 15.sp
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = MessengerTextPrimary,
                    fontSize = 15.sp
                ),
                cursorBrush = SolidColor(MessengerUnreadBlue),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ==========================================
// 7. KARUZELA RELACJI I AKTYWNYCH ZNAJOMYCH
// ==========================================

@Composable
private fun StoriesAndActiveRow() {
    val stories = remember { getMockStories() }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        // "Opublikuj notatkę..."
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(68.dp)
            ) {
                Box(contentAlignment = Alignment.TopCenter) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-4).dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF242526))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Opublikuj notatkę...",
                            fontSize = 9.sp,
                            color = MessengerTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3A3B3C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Dodaj notatkę",
                            tint = MessengerTextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Twoja notatka",
                    fontSize = 12.sp,
                    color = MessengerTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Aktywni znajomi
        items(stories) { contact ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(62.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = contact.avatarUrl,
                        contentDescription = contact.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                    if (contact.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(MessengerOnlineGreen)
                                .border(2.dp, MessengerBg, CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contact.name,
                    fontSize = 12.sp,
                    color = MessengerTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ==========================================
// 8. ELEMENT LISTY KONWERSACJI
// ==========================================

@Composable
private fun MessengerConversationRow(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Awatar ze wskaźnikiem online
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = conversation.participantAvatar,
                contentDescription = conversation.participantName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
            if (conversation.isOnline) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(MessengerOnlineGreen)
                        .border(2.5.dp, MessengerBg, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Nazwa i treść wiadomości
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.participantName,
                fontSize = 16.sp,
                fontWeight = if (conversation.isUnread) FontWeight.Bold else FontWeight.Medium,
                color = MessengerTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.lastMessage,
                    fontSize = 14.sp,
                    fontWeight = if (conversation.isUnread) FontWeight.Bold else FontWeight.Normal,
                    color = if (conversation.isUnread) MessengerTextPrimary else MessengerTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = " • ${conversation.lastMessageTime}",
                    fontSize = 13.sp,
                    fontWeight = if (conversation.isUnread) FontWeight.Bold else FontWeight.Normal,
                    color = if (conversation.isUnread) MessengerTextPrimary else MessengerTextSecondary
                )
            }
        }

        // Niebieska kropka nieprzeczytanej wiadomości
        if (conversation.isUnread) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MessengerUnreadBlue)
            )
        }
    }
}

// ==========================================
// 9. OKNO CZATU (CHAT DETAIL)
// ==========================================

@Composable
private fun MessengerChatDetail(
    conversation: ChatConversation,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            listState.animateScrollToItem(conversation.messages.size - 1)
        }
    }

    Scaffold(
        containerColor = MessengerBg,
        topBar = {
            Surface(
                color = Color(0xFF18191A),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Wstecz",
                                tint = FacebookBlue
                            )
                        }

                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = conversation.participantAvatar,
                                contentDescription = conversation.participantName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                            if (conversation.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MessengerOnlineGreen)
                                        .border(1.5.dp, Color(0xFF18191A), CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = conversation.participantName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MessengerTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = conversation.lastActive,
                                fontSize = 11.sp,
                                color = MessengerTextSecondary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Call, contentDescription = "Zadzwoń", tint = FacebookBlue)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Videocam, contentDescription = "Wideo", tint = FacebookBlue)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.Info, contentDescription = "Informacje", tint = FacebookBlue)
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color(0xFF18191A),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Image, contentDescription = "Zdjęcie", tint = FacebookBlue)
                    }
                    IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Mic, contentDescription = "Głos", tint = FacebookBlue)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF242526))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Wpisz wiadomość...",
                                fontSize = 14.sp,
                                color = MessengerTextSecondary
                            )
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            textStyle = TextStyle(
                                color = MessengerTextPrimary,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(MessengerUnreadBlue),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onSendMessage(inputText.trim())
                                inputText = ""
                                scope.launch {
                                    if (conversation.messages.isNotEmpty()) {
                                        listState.animateScrollToItem(conversation.messages.size)
                                    }
                                }
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Wyślij",
                                tint = FacebookBlue
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                onSendMessage("👍")
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Kciuk",
                                tint = FacebookBlue
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = conversation.participantAvatar,
                        contentDescription = conversation.participantName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .shadow(4.dp, CircleShape)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = conversation.participantName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MessengerTextPrimary
                    )
                    Text(
                        text = "Jesteście znajomymi na Facebooku",
                        fontSize = 12.sp,
                        color = MessengerTextSecondary
                    )
                }
            }

            items(conversation.messages, key = { it.id }) { message ->
                val isMe = message.isFromMe
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (isMe) 18.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 18.dp
                                )
                            )
                            .background(
                                if (isMe) SentBubbleGradient else SolidColor(Color(0xFF242526))
                            )
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = message.text,
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 10. DOLNY PASEK ZAKŁADEK (Czaty, Relacje...)
// ==========================================

@Composable
private fun MessengerBottomNavigation(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit
) {
    val items = listOf(
        Triple(Icons.Outlined.ChatBubbleOutline, "Czaty", 0),
        Triple(Icons.Default.VideoCall, "Rozmowy", 0),
        Triple(Icons.Default.People, "Kontakty", 0),
        Triple(Icons.Outlined.Notifications, "Relacje", 3)
    )

    NavigationBar(
        containerColor = Color(0xFF121212),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEachIndexed { index, (icon, label, badgeCount) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelect(index) },
                icon = {
                    BadgedBox(badge = {
                        if (badgeCount > 0) {
                            Badge(
                                containerColor = Color(0xFFE41E3F),
                                contentColor = Color.White
                            ) {
                                Text(text = "$badgeCount", fontSize = 10.sp)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) MessengerUnreadBlue else MessengerTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = if (isSelected) MessengerUnreadBlue else MessengerTextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}