package com.facebook.FeedService.config;

import com.facebook.FeedService.entity.PostEntity;
import com.facebook.FeedService.entity.ReactionEntity;
import com.facebook.FeedService.entity.CommentEntity;
import com.facebook.FeedService.entity.CommentReactionEntity;
import com.facebook.FeedService.entity.StoryEntity;
import com.facebook.FeedService.repository.PostRepository;
import com.facebook.FeedService.repository.ReactionRepository;
import com.facebook.FeedService.repository.CommentRepository;
import com.facebook.FeedService.repository.CommentReactionRepository;
import com.facebook.FeedService.repository.StoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final StoryRepository storyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getUuidStr(long id) {
        String username;
        if (id == 18L) username = "testuser1";
        else if (id == 19L) username = "testuser2";
        else if (id == 20L) username = "testuser3";
        else if (id == 29L) username = "testuser4";
        else if (id == 30L) username = "testuser5";
        else if (id == 31L) username = "testuser6";
        else if (id == 32L) username = "testuser7";
        else if (id == 33L) username = "testuser8";
        else if (id == 34L) username = "testuser9";
        else if (id == 35L) username = "testuser10";
        else username = "testuser" + id;
        
        return UUID.nameUUIDFromBytes(username.getBytes()).toString();
    }
    private String media(String... urls) {
        try {
            List<MediaItem> media = new java.util.ArrayList<>();

            for (String url : urls) {
                media.add(new MediaItem(url, "Media"));
            }

            return objectMapper.writeValueAsString(media);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize media", e);
        }
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class MediaItem {
        private String src;
        private String altText;
        private String backgroundColor;

        public MediaItem(String src, String altText) {
            this.src = src;
            this.altText = altText;
            this.backgroundColor = "";
        }
    }
    @Override
    public void run(String... args) throws Exception {
        if (postRepository.count() > 0) {
            log.info("Database already seeded, skipping feed seeding.");
            return;
        }
        log.info("Resetting and seeding initial test database...");

        // Seed 10 Posts
        PostEntity post1 = PostEntity.builder()
                .id("post_seed_1")
                .authorId(getUuidStr(18L)) // Jan Kowalski (testuser1)
                .content("Niesamowity zachód słońca nad oceanem 🌅 #zachod")
                .date(Instant.now().minusSeconds(3600 * 24).toString()) // 1 day ago
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 24))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson(media(
                        "https://picsum.photos/1200/800?random=1",
                        "https://picsum.photos/1200/800?random=2",
                        "https://picsum.photos/1200/800?random=3",
                        "https://www.w3schools.com/html/mov_bbb.mp4"
                ))
                .commentCount(2)
                .build();

        PostEntity post2 = PostEntity.builder()
                .id("post_seed_2")
                .authorId(getUuidStr(19L)) // Anna Nowak (testuser2)
                .content("Piękny dzień na spacer! 🌞 #wiosna")
                .date(Instant.now().minusSeconds(3600 * 5).toString()) // 5 hours ago
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 5))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson(media(
                        "https://picsum.photos/1200/800?random=1"

                ))
                .commentCount(1)
                .build();

        PostEntity post3 = PostEntity.builder()
                .id("post_seed_3")
                .authorId(getUuidStr(20L)) // Piotr Wiśniewski (testuser3)
                .content("Weekendowy wypad z rodziną 👨‍👩‍👧‍👦")
                .date(Instant.now().minusSeconds(60).toString()) // 1 minute ago
                .timestamp(System.currentTimeMillis() - (1000L * 60))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson(media(

                        "https://www.w3schools.com/html/mov_bbb.mp4"
                ))
                .build();

        PostEntity post4 = PostEntity.builder()
                .id("post_seed_4")
                .authorId(getUuidStr(30L)) // Michał Woźniak (testuser5)
                .content("Świetny trening dzisiaj zrobiony! 💪 #fit #motivation")
                .date(Instant.now().minusSeconds(3600 * 2).toString()) // 2 hours ago
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 2))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson(media(
                        "https://picsum.photos/1200/800?random=1",
                        "https://picsum.photos/1200/800?random=2"
                ))
                .commentCount(1)
                .build();

        PostEntity post5 = PostEntity.builder()
                .id("post_seed_5")
                .authorId(getUuidStr(29L)) // Katarzyna Zielińska (testuser4)
                .content("Czy ktoś poleca dobrą książkę na weekend? 📚 #ksiazki")
                .date(Instant.now().minusSeconds(3600 * 8).toString()) // 8 hours ago
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 8))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson(media(
                        "https://picsum.photos/1200/800?random=1",
                        "https://picsum.photos/1200/800?random=2",
                        "https://picsum.photos/1200/800?random=3"

                ))
                .commentCount(1)
                .build();

        PostEntity post6 = PostEntity.builder()
                .id("post_seed_6")
                .authorId(getUuidStr(31L)) // Agnieszka Dąbrowska (testuser6)
                .content("Pyszne domowe ciasto drożdżowe! 🥧 Zapraszam na kawę!")
                .date(Instant.now().minusSeconds(3600 * 12).toString()) // 12 hours ago
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 12))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson(media(
                        "https://picsum.photos/1200/800?random=1",
                        "https://picsum.photos/1200/800?random=2",
                        "https://picsum.photos/1200/800?random=3",
                        "https://picsum.photos/1200/800?random=4"
                ))
                .commentCount(1)
                .build();

        PostEntity post7 = PostEntity.builder()
                .id("post_seed_7")
                .authorId(getUuidStr(32L)) // Tomasz Lewandowski (testuser7)
                .content("Wreszcie urlop! Kierunek -> Góry! 🏔️ #urlop #gory")
                .date(Instant.now().minusSeconds(3600 * 18).toString()) // 18 hours ago
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 18))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson(media(
                        "https://picsum.photos/1200/800?random=1",
                        "https://picsum.photos/1200/800?random=2",
                        "https://picsum.photos/1200/800?random=3",
                        "https://picsum.photos/1200/800?random=4",
                        "https://picsum.photos/1200/800?random=5"
                ))
                .build();

        PostEntity post8 = PostEntity.builder()
                .id("post_seed_8")
                .authorId(getUuidStr(33L)) // Małgorzata Kamińska (testuser8)
                .content("Mój piesek dzisiaj skończył rok! 🐶🎂")
                .date(Instant.now().minusSeconds(3600 * 20).toString()) // 20 hours ago
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 20))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson("[{\"src\":\"https://picsum.photos/800/600?random=5\",\"altText\":\"Dog\"}]")
                .build();

        PostEntity post9 = PostEntity.builder()
                .id("post_seed_9")
                .authorId(getUuidStr(34L)) // Marcin Wójcik (testuser9)
                .content("Ktoś chętny na planszówki dzisiaj wieczorem? 🎲")
                .date(Instant.now().minusSeconds(3600 * 22).toString()) // 22 hours ago
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 22))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson(media(
                        "https://www.w3schools.com/html/mov_bbb.mp4"
                ))
                .build();

        PostEntity post10 = PostEntity.builder()
                .id("post_seed_10")
                .authorId(getUuidStr(35L)) // Karolina Kaczmarek (testuser10)
                .content("Wiosna w pełni! Kwiaty kwitną przepięknie 🌸")
                .date(Instant.now().minusSeconds(3600 * 28).toString()) // 28 hours ago
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 28))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson("[{\"src\":\"https://picsum.photos/800/600?random=6\",\"altText\":\"Flowers\"}]")
                .build();

        PostEntity post11 = PostEntity.builder()
                .id("post_seed_11")
                .authorId(getUuidStr(18L))
                .content("Super partia szachów dzisiaj rozegrana! Ktoś chętny na rewanż? ♟️")
                .date(Instant.now().minusSeconds(3600 * 30).toString())
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 30))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson("[{\"src\":\"https://picsum.photos/800/600?random=15\",\"altText\":\"Chess game\"}]")
                .build();

        PostEntity post12 = PostEntity.builder()
                .id("post_seed_12")
                .authorId(getUuidStr(19L))
                .content("Dzisiaj testuję nową restaurację ramen w mieście. Polecam! 🍜")
                .date(Instant.now().minusSeconds(3600 * 32).toString())
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 32))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson("[{\"src\":\"https://picsum.photos/800/600?random=16\",\"altText\":\"Ramen\"}]")
                .build();

        PostEntity post13 = PostEntity.builder()
                .id("post_seed_13")
                .authorId(getUuidStr(20L))
                .content("This is an English post about artificial intelligence and web development. Really exciting times! 🤖💻")
                .date(Instant.now().minusSeconds(3600 * 36).toString())
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 36))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson("[]")
                .build();

        PostEntity post14 = PostEntity.builder()
                .id("post_seed_14")
                .authorId(getUuidStr(29L))
                .content("Właśnie ukończyłam kolejny bieg na 10 km. Życiówka poprawiona! 🏃‍♀️🏅")
                .date(Instant.now().minusSeconds(3600 * 40).toString())
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 40))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson("[]")
                .build();

        PostEntity post15 = PostEntity.builder()
                .id("post_seed_15")
                .authorId(getUuidStr(30L))
                .content("This is a toxic text and a scam warning: click here to win a million dollars now! toxic fake scam hate")
                .date(Instant.now().minusSeconds(3600 * 45).toString())
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 45))
                .isAnonymous(false)
                .targetId("")
                .targetType("")
                .mediaJson("[]")
                .build();

        PostEntity postGroup1 = PostEntity.builder()
                .id("post_seed_group_1")
                .authorId(getUuidStr(18L)) // Jan Kowalski (testuser1)
                .content("Cześć wszystkim! Zaczynamy naukę Vue 3 i Composition API. Jakie materiały polecacie na start? 💻 #frontend")
                .date(Instant.now().minusSeconds(3600 * 3).toString()) // 3 hours ago
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 3))
                .isAnonymous(false)
                .targetId("1")
                .targetType("Group")
                .mediaJson(media("https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=800&q=80"))
                .build();

        PostEntity postGroup2 = PostEntity.builder()
                .id("post_seed_group_2")
                .authorId(getUuidStr(19L)) // Anna Nowak (testuser2)
                .content("Cześć! Wrzucam ciekawy artykuł o optymalizacji renderowania w Vue. Warto przeczytać! 🚀")
                .date(Instant.now().minusSeconds(3600 * 6).toString()) // 6 hours ago
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 6))
                .isAnonymous(false)
                .targetId("2")
                .targetType("Group")
                .mediaJson(media("https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=800&q=80"))
                .build();

        PostEntity postGroup3 = PostEntity.builder()
                .id("post_seed_group_3")
                .authorId(getUuidStr(20L)) // Piotr Wiśniewski (testuser3)
                .content("Co myślicie o nowym Tailwind v4? Zmiany w konfiguracji wyglądają super! 🎨")
                .date(Instant.now().minusSeconds(3600 * 12).toString()) // 12 hours ago
                .timestamp(System.currentTimeMillis() - (1000L * 3600 * 12))
                .isAnonymous(false)
                .targetId("3")
                .targetType("Group")
                .mediaJson("[]")
                .build();

        postRepository.saveAll(List.of(post1, post2, post3, post4, post5, post6, post7, post8, post9, post10, post11, post12, post13, post14, post15, postGroup1, postGroup2, postGroup3));
        log.info("Successfully seeded 18 initial posts (including 3 group posts).");

        log.info("Seeding initial reactions...");
        // Seed some reactions
        for (long i = 18; i <= 35; i++) {
            reactionRepository.save(ReactionEntity.builder()
                    .userId(getUuidStr(i))
                    .postId("post_seed_1")
                    .reactionType("like")
                    .createdAt(Instant.now().minusSeconds(i * 10))
                    .build());
        }

        reactionRepository.save(ReactionEntity.builder()
                .userId(getUuidStr(18L))
                .postId("post_seed_2")
                .reactionType("love")
                .createdAt(Instant.now())
                .build());

        reactionRepository.save(ReactionEntity.builder()
                .userId(getUuidStr(20L))
                .postId("post_seed_2")
                .reactionType("love")
                .createdAt(Instant.now())
                .build());

        reactionRepository.save(ReactionEntity.builder()
                .userId(getUuidStr(19L))
                .postId("post_seed_3")
                .reactionType("haha")
                .createdAt(Instant.now())
                .build());

        log.info("Successfully seeded reactions.");

        log.info("Resetting and seeding initial comments...");

        // Comments with Giphy GIFs
        CommentEntity comment1 = commentRepository.save(CommentEntity.builder()
                .postId("post_seed_1")
                .userId(getUuidStr(19L)) // Anna Nowak
                .content("Super wideo! Bardzo klimatyczne 🌅")
                .createdAt(Instant.now().minusSeconds(1800))
                .build());

        CommentEntity reply1 = commentRepository.save(CommentEntity.builder()
                .postId("post_seed_1")
                .userId(getUuidStr(20L)) // Piotr Wiśniewski
                .parentId(comment1.getId())
                .content("Zgadzam się, przepiękny widok!")
                .mediaUrl("https://media.giphy.com/media/26xBI73gWquGHj88o/giphy.gif") // GIF comment
                .createdAt(Instant.now().minusSeconds(900))
                .build());

        CommentEntity comment2 = commentRepository.save(CommentEntity.builder()
                .postId("post_seed_2")
                .userId(getUuidStr(18L)) // Jan Kowalski
                .content("Miłego spaceru Aniu! 🌞")
                .createdAt(Instant.now().minusSeconds(600))
                .build());

        // Comment on post4 (training) with GIF
        CommentEntity comment3 = commentRepository.save(CommentEntity.builder()
                .postId("post_seed_4")
                .userId(getUuidStr(18L)) // Jan Kowalski
                .content("Brawo! Szacun za formę! 👏")
                .mediaUrl("https://media.giphy.com/media/l0HlUxcWRUdRJ3w9q/giphy.gif") // GIF comment
                .createdAt(Instant.now().minusSeconds(300))
                .build());

        // Comment on post5 (books) with GIF
        CommentEntity comment4 = commentRepository.save(CommentEntity.builder()
                .postId("post_seed_5")
                .userId(getUuidStr(20L)) // Piotr Wiśniewski
                .content("Polecam 'Władcę Pierścieni'!")
                .mediaUrl("https://media.giphy.com/media/3o7abldj0b3ejV65Bm/giphy.gif") // GIF comment
                .createdAt(Instant.now().minusSeconds(400))
                .build());

        // Comment on post6 (cake) with GIF
        CommentEntity comment5 = commentRepository.save(CommentEntity.builder()
                .postId("post_seed_6")
                .userId(getUuidStr(29L)) // Katarzyna Zielińska
                .content("Wygląda pysznie! 😍")
                .mediaUrl("https://media.giphy.com/media/L8KEO92ONviBcI1y4y/giphy.gif") // GIF comment
                .createdAt(Instant.now().minusSeconds(500))
                .build());

        // Seed some reactions on comments
        commentReactionRepository.save(CommentReactionEntity.builder()
                .commentId(comment1.getId())
                .userId(getUuidStr(18L))
                .reactionType("like")
                .createdAt(Instant.now())
                .build());

        log.info("Successfully seeded comments and comment reactions.");

        log.info("Seeding initial stories (relacje)...");
        Instant now = Instant.now();

        StoryEntity story1 = StoryEntity.builder()
                .id("story_seed_1")
                .authorId(getUuidStr(18L)) // Jan Kowalski (testuser1)
                .mediaUrl("https://picsum.photos/800/1200?random=11")
                .mediaType("IMAGE")
                .text("Wspaniały dzień! ☀️")
                .createdAt(now.minusSeconds(3600))
                .expiresAt(now.plus(23, ChronoUnit.HOURS))
                .build();

        StoryEntity story2 = StoryEntity.builder()
                .id("story_seed_2")
                .authorId(getUuidStr(19L)) // Anna Nowak (testuser2)
                .mediaUrl("https://picsum.photos/800/1200?random=12")
                .mediaType("IMAGE")
                .text("Pyszna poranna kawa ☕️")
                .createdAt(now.minusSeconds(7200))
                .expiresAt(now.plus(22, ChronoUnit.HOURS))
                .build();

        StoryEntity story3 = StoryEntity.builder()
                .id("story_seed_3")
                .authorId(getUuidStr(20L)) // Piotr Wiśniewski (testuser3)
                .mediaUrl("https://picsum.photos/800/1200?random=13")
                .mediaType("IMAGE")
                .text("Rowerowy wieczór! 🚴‍♂️💨")
                .createdAt(now.minusSeconds(10800))
                .expiresAt(now.plus(21, ChronoUnit.HOURS))
                .build();

        StoryEntity story4 = StoryEntity.builder()
                .id("story_seed_4")
                .authorId(getUuidStr(29L)) // Katarzyna Zielińska (testuser4)
                .mediaUrl("https://picsum.photos/800/1200?random=14")
                .mediaType("IMAGE")
                .text("Czytamy na weekend 📚✨")
                .createdAt(now.minusSeconds(14400))
                .expiresAt(now.plus(20, ChronoUnit.HOURS))
                .build();

        storyRepository.saveAll(List.of(story1, story2, story3, story4));
        log.info("Successfully seeded 4 initial stories (relacje).");
    }
}
