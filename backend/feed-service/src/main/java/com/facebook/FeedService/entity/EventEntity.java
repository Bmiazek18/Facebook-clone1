package com.facebook.FeedService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {
    @Id
    private String id;

    private String userId;
    private String name;
    private String title;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private String type; // online/offline
    private String privacy; // public/private
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_images", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    private String location;
    private String locationName;
    private String address;
    private Boolean showGuestList;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_hosts", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "host_name")
    @Builder.Default
    private List<String> hosts = new ArrayList<>();

    private String date;

    @Builder.Default
    private Integer responses = 0;
    
    @Builder.Default
    private Integer guestsGoing = 0;
    
    @Builder.Default
    private Integer guestsInterested = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_coordinates", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "coord")
    @Builder.Default
    private List<Double> coordinates = new ArrayList<>();

    private String frequency;
}
