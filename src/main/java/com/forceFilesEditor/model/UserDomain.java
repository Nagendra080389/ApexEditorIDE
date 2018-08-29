package com.forceFilesEditor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document(collection = "UserDomain")
public class UserDomain implements Serializable {
    @Id
    private String Id;
    private String orgId;
    private String userName;
    private String userId;
    private Map<String, List<PMDStructure>> pmdStructures;
    private Boolean isAdmin;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Map<String, List<PMDStructure>> getPmdStructures() {
        return pmdStructures;
    }

    public void setPmdStructures(Map<String, List<PMDStructure>> pmdStructures) {
        this.pmdStructures = pmdStructures;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }
}
