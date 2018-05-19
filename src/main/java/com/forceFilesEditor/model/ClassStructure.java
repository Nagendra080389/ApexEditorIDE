package com.forceFilesEditor.model;

import java.io.Serializable;
import java.util.List;

public class ClassStructure implements Serializable {
    private String className;
    private List<String> methodsNames;
    private List<String> propertyNames;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getMethodsNames() {
        return methodsNames;
    }

    public void setMethodsNames(List<String> methodsNames) {
        this.methodsNames = methodsNames;
    }

    public List<String> getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }
}
