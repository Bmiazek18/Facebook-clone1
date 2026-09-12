# Facebook Clone – Mobile Android App (Kotlin & Jetpack Compose)

Natywna aplikacja mobilna Facebook Clone przepisana na system Android przy użyciu **Kotlina** i **Jetpack Compose**.

---

## 🏗️ Architektura i Stack Technologiczny

- **Język:** Kotlin 1.9+
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architektura:** MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
- **Stan UI:** Kotlin Coroutines `StateFlow` + `viewModelScope`
- **Ładowanie obrazów:** Coil Compose (z asynchronicznym cache'owaniem)
- **Interakcje:**
  - Pull-to-refresh (`PullToRefreshContainer`)
  - Infinite scroll pagination (`LazyListState` & `derivedStateOf`)
  - Obsługa długiego przytrzymania dla popupu reakcji (`combinedClickable`)
  - Interaktywny Bottom Sheet dla komentarzy (`ModalBottomSheet`)
  - Shimmer effect placeholder dla ładowania postów (`PostSkeleton`)

---

## 📁 Struktura Projektu

```
mobile-android/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/facebook/clone/
│       │   ├── MainActivity.kt               # Główna aktywność z Edge-to-Edge
│       │   ├── FacebookApplication.kt        # Klasa aplikacji Android
│       │   ├── theme/                        # Kolory FB, Typografia, Dark/Light Theme
│       │   │   ├── Color.kt
│       │   │   ├── Theme.kt
│       │   │   └── Type.kt
│       │   ├── model/                        # Modele danych spójne z GraphQL
│       │   │   ├── User.kt
│       │   │   ├── Post.kt
│       │   │   ├── Story.kt
│       │   │   ├── Comment.kt
│       │   │   ├── ReactionType.kt
│       │   │   ├── FriendSuggestion.kt
│       │   │   └── ReelItem.kt
│       │   ├── data/                         # Mock data provider & repo
│       │   │   └── MockDataProvider.kt
│       │   ├── viewmodel/                    # Logika biznesowa i stan feedu
│       │   │   └── HomeViewModel.kt
│       │   └── ui/home/                      # Widok główny i komponenty
│       │       ├── HomeScreen.kt
│       │       └── components/
│       │           ├── HomeTopBar.kt         # Logo FB, Szukaj, Powiadomienia, Messenger
│       │           ├── CreatePostBox.kt      # "Co słychać?", Zdjęcie, Live, Nastrój
│       │           ├── StoriesSection.kt     # Pasek relacji (Add Story + Friends Stories)
│       │           ├── PostItemCard.kt       # Karta posta, media, statystyki, akcje
│       │           ├── ReactionPickerPopup.kt# Wyskakujące reakcje (Lubię to, Super, Haha itd.)
│       │           ├── PeopleYouMayKnowSection.kt # Sugestie znajomych
│       │           ├── ReelsGallerySection.kt# Karuzela rolek wideo
│       │           ├── CommentsBottomSheet.kt# Modal komentarzy z polem dodawania
│       │           └── PostSkeleton.kt       # Animowany placeholder ładowania
│       └── res/                              # Zasoby XML, stringi, motywy
├── settings.gradle.kts
└── build.gradle.kts
```

---

## 🚀 Jak uruchomić w Android Studio

1. Otwórz **Android Studio**.
2. Wybierz **Open** i wskaż katalog `mobile-android/`.
3. Poczekaj na automatyczną synchronizację Gradle (`Sync Project with Gradle Files`).
4. Wybierz emulator (np. Pixel 8 z Androidem 14 / API 34) lub podłącz telefon przez USB.
5. Kliknij zielony przycisk **Run ('app')**.
