package com.facebook.UserService.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
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

    public User() {
    }

    public User(UUID id, java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt, String username, String email, String password, String firstName, String lastName, String avatarId, String coverPhotoId, String bio, String location, String hometown, String school, String highSchool, String education, String job, String company, String phone, String website, String gender, String birthDate, String languages, String pronouns, String relationshipStatus, String relationshipSince, String partnerName, String partnerAvatar, String bioDetails, String namePronounciation, String opaqueRecord, String encryptedHistory, Integer failedAttempts, List<String> otherNames, List<String> favoriteQuotes, List<FamilyMember> familyMembers, List<LifeEvent> lifeEvents, List<SocialLink> socialLinks, String note, java.time.LocalDateTime noteCreatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarId = avatarId;
        this.coverPhotoId = coverPhotoId;
        this.bio = bio;
        this.location = location;
        this.hometown = hometown;
        this.school = school;
        this.highSchool = highSchool;
        this.education = education;
        this.job = job;
        this.company = company;
        this.phone = phone;
        this.website = website;
        this.gender = gender;
        this.birthDate = birthDate;
        this.languages = languages;
        this.pronouns = pronouns;
        this.relationshipStatus = relationshipStatus;
        this.relationshipSince = relationshipSince;
        this.partnerName = partnerName;
        this.partnerAvatar = partnerAvatar;
        this.bioDetails = bioDetails;
        this.namePronounciation = namePronounciation;
        this.opaqueRecord = opaqueRecord;
        this.encryptedHistory = encryptedHistory;
        this.failedAttempts = failedAttempts;
        this.otherNames = otherNames;
        this.favoriteQuotes = favoriteQuotes;
        this.familyMembers = familyMembers;
        this.lifeEvents = lifeEvents;
        this.socialLinks = socialLinks;
        this.note = note;
        this.noteCreatedAt = noteCreatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String avatarId;
        private String coverPhotoId;
        private String bio;
        private String location;
        private String hometown;
        private String school;
        private String highSchool;
        private String education;
        private String job;
        private String company;
        private String phone;
        private String website;
        private String gender;
        private String birthDate;
        private String languages;
        private String pronouns;
        private String relationshipStatus;
        private String relationshipSince;
        private String partnerName;
        private String partnerAvatar;
        private String bioDetails;
        private String namePronounciation;
        private String opaqueRecord;
        private String encryptedHistory;
        private Integer failedAttempts;
        private List<String> otherNames;
        private List<String> favoriteQuotes;
        private List<FamilyMember> familyMembers;
        private List<LifeEvent> lifeEvents;
        private List<SocialLink> socialLinks;
        private String note;
        private java.time.LocalDateTime noteCreatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder createdAt(java.time.LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(java.time.LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder avatarId(String avatarId) {
            this.avatarId = avatarId;
            return this;
        }

        public Builder coverPhotoId(String coverPhotoId) {
            this.coverPhotoId = coverPhotoId;
            return this;
        }

        public Builder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder hometown(String hometown) {
            this.hometown = hometown;
            return this;
        }

        public Builder school(String school) {
            this.school = school;
            return this;
        }

        public Builder highSchool(String highSchool) {
            this.highSchool = highSchool;
            return this;
        }

        public Builder education(String education) {
            this.education = education;
            return this;
        }

        public Builder job(String job) {
            this.job = job;
            return this;
        }

        public Builder company(String company) {
            this.company = company;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder birthDate(String birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public Builder languages(String languages) {
            this.languages = languages;
            return this;
        }

        public Builder pronouns(String pronouns) {
            this.pronouns = pronouns;
            return this;
        }

        public Builder relationshipStatus(String relationshipStatus) {
            this.relationshipStatus = relationshipStatus;
            return this;
        }

        public Builder relationshipSince(String relationshipSince) {
            this.relationshipSince = relationshipSince;
            return this;
        }

        public Builder partnerName(String partnerName) {
            this.partnerName = partnerName;
            return this;
        }

        public Builder partnerAvatar(String partnerAvatar) {
            this.partnerAvatar = partnerAvatar;
            return this;
        }

        public Builder bioDetails(String bioDetails) {
            this.bioDetails = bioDetails;
            return this;
        }

        public Builder namePronounciation(String namePronounciation) {
            this.namePronounciation = namePronounciation;
            return this;
        }

        public Builder opaqueRecord(String opaqueRecord) {
            this.opaqueRecord = opaqueRecord;
            return this;
        }

        public Builder encryptedHistory(String encryptedHistory) {
            this.encryptedHistory = encryptedHistory;
            return this;
        }

        public Builder failedAttempts(Integer failedAttempts) {
            this.failedAttempts = failedAttempts;
            return this;
        }

        public Builder otherNames(List<String> otherNames) {
            this.otherNames = otherNames;
            return this;
        }

        public Builder favoriteQuotes(List<String> favoriteQuotes) {
            this.favoriteQuotes = favoriteQuotes;
            return this;
        }

        public Builder familyMembers(List<FamilyMember> familyMembers) {
            this.familyMembers = familyMembers;
            return this;
        }

        public Builder lifeEvents(List<LifeEvent> lifeEvents) {
            this.lifeEvents = lifeEvents;
            return this;
        }

        public Builder socialLinks(List<SocialLink> socialLinks) {
            this.socialLinks = socialLinks;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public Builder noteCreatedAt(java.time.LocalDateTime noteCreatedAt) {
            this.noteCreatedAt = noteCreatedAt;
            return this;
        }

        public User build() {
            return new User(id, createdAt, updatedAt, username, email, password, firstName, lastName, avatarId, coverPhotoId, bio, location, hometown, school, highSchool, education, job, company, phone, website, gender, birthDate, languages, pronouns, relationshipStatus, relationshipSince, partnerName, partnerAvatar, bioDetails, namePronounciation, opaqueRecord, encryptedHistory, failedAttempts, otherNames, favoriteQuotes, familyMembers, lifeEvents, socialLinks, note, noteCreatedAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public String getCoverPhotoId() {
        return coverPhotoId;
    }

    public void setCoverPhotoId(String coverPhotoId) {
        this.coverPhotoId = coverPhotoId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getHighSchool() {
        return highSchool;
    }

    public void setHighSchool(String highSchool) {
        this.highSchool = highSchool;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getPronouns() {
        return pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }

    public String getRelationshipStatus() {
        return relationshipStatus;
    }

    public void setRelationshipStatus(String relationshipStatus) {
        this.relationshipStatus = relationshipStatus;
    }

    public String getRelationshipSince() {
        return relationshipSince;
    }

    public void setRelationshipSince(String relationshipSince) {
        this.relationshipSince = relationshipSince;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPartnerAvatar() {
        return partnerAvatar;
    }

    public void setPartnerAvatar(String partnerAvatar) {
        this.partnerAvatar = partnerAvatar;
    }

    public String getBioDetails() {
        return bioDetails;
    }

    public void setBioDetails(String bioDetails) {
        this.bioDetails = bioDetails;
    }

    public String getNamePronounciation() {
        return namePronounciation;
    }

    public void setNamePronounciation(String namePronounciation) {
        this.namePronounciation = namePronounciation;
    }

    public String getOpaqueRecord() {
        return opaqueRecord;
    }

    public void setOpaqueRecord(String opaqueRecord) {
        this.opaqueRecord = opaqueRecord;
    }

    public String getEncryptedHistory() {
        return encryptedHistory;
    }

    public void setEncryptedHistory(String encryptedHistory) {
        this.encryptedHistory = encryptedHistory;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public List<String> getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(List<String> otherNames) {
        this.otherNames = otherNames;
    }

    public List<String> getFavoriteQuotes() {
        return favoriteQuotes;
    }

    public void setFavoriteQuotes(List<String> favoriteQuotes) {
        this.favoriteQuotes = favoriteQuotes;
    }

    public List<FamilyMember> getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(List<FamilyMember> familyMembers) {
        this.familyMembers = familyMembers;
    }

    public List<LifeEvent> getLifeEvents() {
        return lifeEvents;
    }

    public void setLifeEvents(List<LifeEvent> lifeEvents) {
        this.lifeEvents = lifeEvents;
    }

    public List<SocialLink> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<SocialLink> socialLinks) {
        this.socialLinks = socialLinks;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public java.time.LocalDateTime getNoteCreatedAt() {
        return noteCreatedAt;
    }

    public void setNoteCreatedAt(java.time.LocalDateTime noteCreatedAt) {
        this.noteCreatedAt = noteCreatedAt;
    }

    public String getCity() {
        return location;
    }

    public void setCity(String city) {
        this.location = city;
    }
}