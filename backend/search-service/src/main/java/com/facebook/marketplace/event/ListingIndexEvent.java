package com.facebook.marketplace.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingIndexEvent implements Serializable {
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String condition;
    private Double latitude;
    private Double longitude;
}
