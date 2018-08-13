package com.forceFilesEditor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Document(collection = "UserDomain")
public class UserDomain implements Serializable {
    @Id
    private String Id;
    private String userName;
    private List<PMDStructure> pmdStructures;
    private Boolean isAdmin;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<PMDStructure> getPmdStructures() {
        return pmdStructures;
    }

    public void setPmdStructures(List<PMDStructure> pmdStructures) {
        this.pmdStructures = pmdStructures;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }
}
