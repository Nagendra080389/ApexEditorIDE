package com.forceFilesEditor.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nagendra on 18-06-2017.
 */
public class PMDStructure implements Serializable{

    public String name;

    public String body;

    private Map<Integer, List<String>> lineNumberError = new HashMap<>();

    private Integer beginLine;
    private Integer endLine;
    private Integer numberOfDuplicates;
    private Integer lineNumber;
    private String reviewFeedback;
    private String codeFragment;
    private Integer noOfDuplicatesFiles;
    private String ruleName;
    private String ruleUrl;
    private Integer rulePriority;

    public Integer getRulePriority() {
        return rulePriority;
    }

    public void setRulePriority(Integer rulePriority) {
        this.rulePriority = rulePriority;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleUrl() {
        return ruleUrl;
    }

    public void setRuleUrl(String ruleUrl) {
        this.ruleUrl = ruleUrl;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getReviewFeedback() {
        return reviewFeedback;
    }

    public void setReviewFeedback(String reviewFeedback) {
        this.reviewFeedback = reviewFeedback;
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

    public Map<Integer, List<String>> getLineNumberError() {
        return lineNumberError;
    }

    public void setLineNumberError(Map<Integer, List<String>> lineNumberError) {
        this.lineNumberError = lineNumberError;
    }

    public Integer getBeginLine() {
        return beginLine;
    }

    public void setBeginLine(Integer beginLine) {
        this.beginLine = beginLine;
    }

    public Integer getEndLine() {
        return endLine;
    }

    public void setEndLine(Integer endLine) {
        this.endLine = endLine;
    }

    public Integer getNumberOfDuplicates() {
        return numberOfDuplicates;
    }

    public void setNumberOfDuplicates(Integer numberOfDuplicates) {
        this.numberOfDuplicates = numberOfDuplicates;
    }

    public String getCodeFragment() {
        return codeFragment;
    }

    public void setCodeFragment(String codeFragment) {
        this.codeFragment = codeFragment;
    }

    public Integer getNoOfDuplicatesFiles() {
        return noOfDuplicatesFiles;
    }

    public void setNoOfDuplicatesFiles(Integer noOfDuplicatesFiles) {
        this.noOfDuplicatesFiles = noOfDuplicatesFiles;
    }

}
