package com.facebook.FeedService.repository;

import com.facebook.FeedService.entity.UserMediaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMediaRepository extends JpaRepository<UserMediaEntity, Long> {

    Page<UserMediaEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<UserMediaEntity> findByUserIdAndMediaTypeOrderByCreatedAtDesc(String userId, String mediaType, Pageable pageable);

    Page<UserMediaEntity> findByUserIdAndAlbumNameOrderByCreatedAtDesc(String userId, String albumName, Pageable pageable);

    long countByUserId(String userId);

    long countByUserIdAndMediaType(String userId, String mediaType);

    long countByUserIdAndAlbumName(String userId, String albumName);

    @Query("SELECT DISTINCT m.albumName FROM UserMediaEntity m WHERE m.userId = :userId AND m.albumName IS NOT NULL")
    List<String> findDistinctAlbumNamesByUserId(@Param("userId") String userId);

    @Query("SELECT m FROM UserMediaEntity m WHERE m.userId = :userId AND m.albumName = :albumName ORDER BY m.createdAt DESC LIMIT 1")
    UserMediaEntity findFirstByUserIdAndAlbumNameOrderByCreatedAtDesc(@Param("userId") String userId, @Param("albumName") String albumName);

    void deleteByPostId(String postId);
}
