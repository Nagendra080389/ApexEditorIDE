package com.forceFilesEditor.model;

import com.sforce.soap.tooling.SymbolTable;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApexClassWrapper {

    public String name;

    public String body;

    private Date salesForceSystemModStamp;

    public String id;

    private Map<Integer, List<String>> lineNumberError = new HashMap<>();

    private Integer lineNumber;

    private boolean isCompilationError;

    private SymbolTable symbolTable;

    public Date getSalesForceSystemModStamp() {
        return salesForceSystemModStamp;
    }

    public void setSalesForceSystemModStamp(Date salesForceSystemModStamp) {
        this.salesForceSystemModStamp = salesForceSystemModStamp;
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
}
