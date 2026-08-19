package com.facebook.UserService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
