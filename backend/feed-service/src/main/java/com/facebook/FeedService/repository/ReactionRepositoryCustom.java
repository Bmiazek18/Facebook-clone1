package com.facebook.FeedService.repository;

import com.facebook.FeedService.entity.ReactionEntity;
import java.util.List;

public interface ReactionRepositoryCustom {
    void batchUpsert(List<ReactionEntity> reactions);
}
