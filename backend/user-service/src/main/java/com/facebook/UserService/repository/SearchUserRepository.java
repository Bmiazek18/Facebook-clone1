package com.facebook.UserService.repository;

import com.facebook.UserService.model.SearchUser;
import com.facebook.UserService.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SearchUserRepository extends JpaRepository<SearchUser, Long> {
    Optional<SearchUser> findBySearchedUser(User searchedUser);
    java.util.List<SearchUser> findAllByOrderByLastSearchedAtDesc();
    java.util.Optional<SearchUser> findBySearchedUserId(java.util.UUID searchedUserId);
    java.util.List<SearchUser> findBySearchingUserIdOrderByLastSearchedAtDesc(java.util.UUID searchingUserId);
    java.util.Optional<SearchUser> findBySearchingUserIdAndSearchedUserId(java.util.UUID searchingUserId, java.util.UUID searchedUserId);

    @org.springframework.data.jpa.repository.Query(value = 
        "SELECT COUNT(*) FROM posts WHERE author_id = :authorId AND timestamp > :lastSearchedTimestamp", 
        nativeQuery = true)
    long countNewPostsSince(String authorId, Long lastSearchedTimestamp);
}
