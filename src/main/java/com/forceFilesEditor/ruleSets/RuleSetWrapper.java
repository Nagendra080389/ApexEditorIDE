package com.forceFilesEditor.ruleSets;

import java.io.Serializable;

public class RuleSetWrapper implements Serializable {
    private RuleType ruleType;
    private Boolean active;

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
