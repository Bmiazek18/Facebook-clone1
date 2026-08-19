package com.facebook.UserService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = java.time.LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = java.time.LocalDateTime.now();
        }
        // Ustawiamy wartość domyślną dla nowych użytkowników
        if (this.failedAttempts == null) {
            this.failedAttempts = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "avatar_id")
    private String avatarId;

    @Column(name = "cover_photo_id")
    private String coverPhotoId;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String location;

    private String hometown;

    private String school;

    @Column(name = "high_school")
    private String highSchool;

    private String education;

    private String job;

    private String company;

    private String phone;

    private String website;

    private String gender;

    @Column(name = "birth_date")
    private String birthDate;

    private String languages;

    private String pronouns;

    @Column(name = "relationship_status")
    private String relationshipStatus;

    @Column(name = "relationship_since")
    private String relationshipSince;

    @Column(name = "partner_name")
    private String partnerName;

    @Column(name = "partner_avatar")
    private String partnerAvatar;

    @Column(name = "bio_details", columnDefinition = "TEXT")
    private String bioDetails;

    @Column(name = "name_pronounciation")
    private String namePronounciation;

    // ==========================================
    // OPAQUE VAULT (E2EE PIN Backup)
    // ==========================================

    @Column(name = "opaque_record", columnDefinition = "TEXT")
    private String opaqueRecord;

    @Column(name = "encrypted_history", columnDefinition = "TEXT")
    private String encryptedHistory;

    @Column(name = "failed_attempts")
    private Integer failedAttempts;

    // ==========================================

    @ElementCollection
    @CollectionTable(name = "user_other_names", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "other_name")
    private List<String> otherNames;

    @ElementCollection
    @CollectionTable(name = "user_favorite_quotes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "quote", columnDefinition = "TEXT")
    private List<String> favoriteQuotes;

    @ElementCollection
    @CollectionTable(name = "user_family_members", joinColumns = @JoinColumn(name = "user_id"))
    private List<FamilyMember> familyMembers;

    @ElementCollection
    @CollectionTable(name = "user_life_events", joinColumns = @JoinColumn(name = "user_id"))
    private List<LifeEvent> lifeEvents;

    @ElementCollection
    @CollectionTable(name = "user_social_links", joinColumns = @JoinColumn(name = "user_id"))
    private List<SocialLink> socialLinks;

    @Column(name = "note")
    private String note;

    @Column(name = "note_created_at")
    private java.time.LocalDateTime noteCreatedAt;
}