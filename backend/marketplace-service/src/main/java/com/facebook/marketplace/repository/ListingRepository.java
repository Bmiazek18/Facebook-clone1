package com.facebook.marketplace.repository;

import com.facebook.marketplace.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    /**
     * Wyszukuje ogłoszenia w danym promieniu od podanego punktu.
     * Rzutowanie na 'geography' pozwala obliczać dystans w metrach uwzględniając krzywiznę Ziemi.
     */
    @Query(value = """
        SELECT * FROM listings 
        WHERE ST_DWithin(
            location::geography, 
            ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, 
            :radiusInMeters
        )
        ORDER BY ST_Distance(
            location::geography, 
            ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
        ) ASC
        """, nativeQuery = true)
    List<Listing> findWithinDistance(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusInMeters") double radiusInMeters
    );
}