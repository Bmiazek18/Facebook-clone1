package com.facebook.GroupsService.rule;

import com.facebook.GroupsService.entity.RuleCriteria;

public interface RuleEvaluator {
    /**
     * Returns true if this evaluator can handle the specified criteria configuration.
     */
    boolean supports(RuleCriteria criteria);

    /**
     * Evaluates the condition. Returns true if the condition matches (is triggered).
     */
    boolean evaluate(RuleCriteria criteria, RuleContext context);
}
