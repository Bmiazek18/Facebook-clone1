package com.facebook.UserService.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_users")
public class SearchUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "searched_user_id", nullable = false)
    private User searchedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "searching_user_id", nullable = true)
    private User searchingUser;

    @Column(name = "last_searched_at", nullable = false)
    private LocalDateTime lastSearchedAt;

    public SearchUser() {
    }

    public SearchUser(Long id, User searchedUser, User searchingUser, LocalDateTime lastSearchedAt) {
        this.id = id;
        this.searchedUser = searchedUser;
        this.searchingUser = searchingUser;
        this.lastSearchedAt = lastSearchedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User searchedUser;
        private User searchingUser;
        private LocalDateTime lastSearchedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder searchedUser(User searchedUser) {
            this.searchedUser = searchedUser;
            return this;
        }

        public Builder searchingUser(User searchingUser) {
            this.searchingUser = searchingUser;
            return this;
        }

        public Builder lastSearchedAt(LocalDateTime lastSearchedAt) {
            this.lastSearchedAt = lastSearchedAt;
            return this;
        }

        public SearchUser build() {
            return new SearchUser(id, searchedUser, searchingUser, lastSearchedAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSearchedUser() {
        return searchedUser;
    }

    public void setSearchedUser(User searchedUser) {
        this.searchedUser = searchedUser;
    }

    public User getSearchingUser() {
        return searchingUser;
    }

    public void setSearchingUser(User searchingUser) {
        this.searchingUser = searchingUser;
    }

    public LocalDateTime getLastSearchedAt() {
        return lastSearchedAt;
    }

    public void setLastSearchedAt(LocalDateTime lastSearchedAt) {
        this.lastSearchedAt = lastSearchedAt;
    }
}
