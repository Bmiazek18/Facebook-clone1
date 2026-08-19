package com.facebook.SearchService.controller;

import com.facebook.SearchService.model.MeiliEvent;
import com.facebook.SearchService.model.MeiliUser;
import com.facebook.SearchService.model.User;
import com.facebook.SearchService.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<MeiliUser>> searchUsers(@RequestParam("query") String query) {
        List<MeiliUser> results = searchService.searchUsers(query);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/users/index")
    public ResponseEntity<Void> indexUser(@RequestBody User user) {
        searchService.indexUser(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/reindex-all")
    public ResponseEntity<Void> reindexAll() {
        searchService.reindexAll();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/events")
    public ResponseEntity<List<MeiliEvent>> searchEvents(@RequestParam("query") String query) {
        List<MeiliEvent> results = searchService.searchEvents(query);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/events/index")
    public ResponseEntity<Void> indexEvent(@RequestBody MeiliEvent event) {
        searchService.indexEvent(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/reindex-all")
    public ResponseEntity<Void> reindexAllEvents() {
        searchService.reindexAllEvents();
        return ResponseEntity.ok().build();
    }
}
