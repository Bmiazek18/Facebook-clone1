package com.facebook.marketplace.controller;

import com.facebook.marketplace.dto.CreateListingRequest;
import com.facebook.marketplace.model.Listing;
import com.facebook.marketplace.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    // Tworzenie nowego ogłoszenia
    @PostMapping
    public ResponseEntity<Listing> createListing(@Valid @RequestBody CreateListingRequest request) {
        Listing createdListing = listingService.createListing(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdListing);
    }

    // Pobieranie szczegółów konkretnego ogłoszenia
    @GetMapping("/{id}")
    public ResponseEntity<Listing> getListing(@PathVariable Long id) {
        Listing listing = listingService.getListingById(id);
        return ResponseEntity.ok(listing);
    }

    // Pobieranie ogłoszeń w danym promieniu (filtrowanie PostGIS i Meilisearch)
    @GetMapping("/nearby")
    public ResponseEntity<List<Listing>> getNearbyListings(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10000") double radius, // Domyślnie 10 km
            @RequestParam(required = false) String query
    ) {
        if (query != null && !query.trim().isEmpty()) {
            return ResponseEntity.ok(listingService.searchListingsMeili(query, lat, lon, radius));
        }
        List<Listing> listings = listingService.getListingsNearby(lat, lon, radius);
        return ResponseEntity.ok(listings);
    }
}