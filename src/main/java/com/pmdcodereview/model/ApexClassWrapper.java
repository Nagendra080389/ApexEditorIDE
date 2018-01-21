package com.pmdcodereview.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApexClassWrapper {

    public String name;

    public String body;

    public String id;

    public Map<Integer, List<String>> lineNumberError = new HashMap<>();

    public Integer lineNumber;

    public boolean isCompilationError;

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
}
