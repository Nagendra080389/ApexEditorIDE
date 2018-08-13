package com.forceFilesEditor.model;

import com.forceFilesEditor.ruleSets.RuleSetWrapper;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "RuleSetsDomain")
public class RuleSetsDomain {
    @Id
    private String id;
    private String orgId;
    private String ruleSetXML;
    private List<RuleSetWrapper> ruleSetWrappers;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getRuleSetXML() {
        return ruleSetXML;
    }

    public void setRuleSetXML(String ruleSetXML) {
        this.ruleSetXML = ruleSetXML;
    }

    public List<RuleSetWrapper> getRuleSetWrappers() {
        return ruleSetWrappers;
    }

    public void setRuleSetWrappers(List<RuleSetWrapper> ruleSetWrappers) {
        this.ruleSetWrappers = ruleSetWrappers;
    }
}
