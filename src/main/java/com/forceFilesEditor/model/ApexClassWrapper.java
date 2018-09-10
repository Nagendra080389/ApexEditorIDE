package com.forceFilesEditor.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sforce.soap.tooling.SymbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApexClassWrapper {

    public String name;

    public String body;

    public String id;

    private boolean timeStampNotMatching;

    private Map<Integer, List<String>> lineNumberError = new HashMap<>();

    private ApexClassWrapper modifiedApexClassWrapper;

    private String originalBodyFromOrg;

    private Integer lineNumber;

    private String groupName;

    private String lastModifiedBy;

    private String lastModifiedDate;

    private String currentUser;

    private boolean isCompilationError;

    private SymbolTable symbolTable;

    private boolean dataNotMatching;

    @JsonDeserialize(as=ArrayList.class, contentAs=PMDStructure.class)
    private List<PMDStructure> pmdStructures;

    private String orgId;

    public String getOriginalBodyFromOrg() {
        return originalBodyFromOrg;
    }

    public void setOriginalBodyFromOrg(String originalBodyFromOrg) {
        this.originalBodyFromOrg = originalBodyFromOrg;
    }

    public boolean isDataNotMatching() {
        return dataNotMatching;
    }

    public void setDataNotMatching(boolean dataNotMatching) {
        this.dataNotMatching = dataNotMatching;
    }

    public ApexClassWrapper getModifiedApexClassWrapper() {
        return modifiedApexClassWrapper;
    }

    public void setModifiedApexClassWrapper(ApexClassWrapper modifiedApexClassWrapper) {
        this.modifiedApexClassWrapper = modifiedApexClassWrapper;
    }

    public boolean isTimeStampNotMatching() {
        return timeStampNotMatching;
    }

    public void setTimeStampNotMatching(boolean timeStampNotMatching) {
        this.timeStampNotMatching = timeStampNotMatching;
    }

    public List<PMDStructure> getPmdStructures() {
        return pmdStructures;
    }

    public void setPmdStructures(List<PMDStructure> pmdStructures) {
        this.pmdStructures = pmdStructures;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public boolean isCompilationError() {
        return isCompilationError;
    }

    public void setCompilationError(boolean compilationError) {
        isCompilationError = compilationError;
    }

    public Map<Integer, List<String>> getLineNumberError() {
        return lineNumberError;
    }

    public void setLineNumberError(Map<Integer, List<String>> lineNumberError) {
        this.lineNumberError = lineNumberError;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
