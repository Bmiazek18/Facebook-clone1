package com.facebook.UserService.repository;

import com.facebook.UserService.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PageRepository extends JpaRepository<Page, UUID> {
    List<Page> findByOwnerId(UUID ownerId);
    List<Page> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);
    Optional<Page> findByIdAndOwnerId(UUID id, UUID ownerId);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Page p WHERE LOWER(p.website) LIKE LOWER(CONCAT('%', :domain, '%'))")
    List<Page> findByWebsiteDomain(@org.springframework.data.repository.query.Param("domain") String domain);
}
