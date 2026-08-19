package com.facebook.SearchService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeiliEvent {
    private String id;
    private String userId;
    private String name;
    private String title;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private String type;
    private String privacy;
    private String description;
    private String location;
    private String locationName;
    private String address;
    private Boolean showGuestList;
    private String date;
    private String frequency;
    private List<String> images;
}
