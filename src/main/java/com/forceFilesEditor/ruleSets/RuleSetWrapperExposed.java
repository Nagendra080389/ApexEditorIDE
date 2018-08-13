package com.forceFilesEditor.ruleSets;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class RuleSetWrapperExposed implements Serializable {

    private List<RuleSetWrapper> ruleSetWrapper;
    private RulesetType rulesetType;
    private Set<String> listOfPriorities;
    private String orgId;

    public List<RuleSetWrapper> getRuleSetWrapper() {
        return ruleSetWrapper;
    }

    public void setRuleSetWrapper(List<RuleSetWrapper> ruleSetWrapper) {
        this.ruleSetWrapper = ruleSetWrapper;
    }

    public RulesetType getRulesetType() {
        return rulesetType;
    }

    public void setRulesetType(RulesetType rulesetType) {
        this.rulesetType = rulesetType;
    }

    public Set<String> getListOfPriorities() {
        return listOfPriorities;
    }

    public void setListOfPriorities(Set<String> listOfPriorities) {
        this.listOfPriorities = listOfPriorities;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
