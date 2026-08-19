package com.facebook.SearchService.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "events")
@Data
public class Event {

    @Id
    private String id;

    private String userId;
    private String name;
    private String title;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "start_time")
    private String startTime;

    @Column(name = "end_date")
    private String endDate;

    @Column(name = "end_time")
    private String endTime;

    private String type;
    private String privacy;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    @Column(name = "location_name")
    private String locationName;

    private String address;

    @Column(name = "show_guest_list")
    private Boolean showGuestList;

    private String date;
    private String frequency;
}
