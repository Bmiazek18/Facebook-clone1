package com.facebook.GroupsService.rule;

import com.facebook.GroupsService.entity.RuleCriteria;
import org.springframework.stereotype.Component;

@Component
public class ProfilePictureEvaluator implements RuleEvaluator {

    @Override
    public boolean supports(RuleCriteria criteria) {
        return criteria.getRequireProfilePicture() != null && criteria.getRequireProfilePicture();
    }

    @Override
    public boolean evaluate(RuleCriteria criteria, RuleContext context) {
        if (context.getUserDetails() == null) {
            return false;
        }
        
        String avatarId = context.getUserDetails().getAvatarId();
        // Triggers (returns true) if a profile picture is required but the user does not have one (empty/null avatarId)
        return avatarId == null || avatarId.trim().isEmpty();
    }
}
