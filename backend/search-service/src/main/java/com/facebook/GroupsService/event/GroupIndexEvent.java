package com.facebook.GroupsService.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupIndexEvent implements Serializable {
    private String id;
    private String name;
    private String image;
    private Integer newPostsCount;
    private Boolean delete; // true to remove from Meilisearch
}
