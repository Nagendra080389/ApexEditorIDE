package com.forceFilesEditor.model;

import java.util.List;
import java.util.Map;

public class PMDMainWrapper {

    private Map<String, PMDStructureWrapper> pmdStructureWrapper;
    private List<PMDStructure> pmdDuplicates;

    public Map<String, PMDStructureWrapper> getPmdStructureWrapper() {
        return pmdStructureWrapper;
    }

    public void setPmdStructureWrapper(Map<String, PMDStructureWrapper> pmdStructureWrapper) {
        this.pmdStructureWrapper = pmdStructureWrapper;
    }

    public List<PMDStructure> getPmdDuplicates() {
        return pmdDuplicates;
    }

    public void setPmdDuplicates(List<PMDStructure> pmdDuplicates) {
        this.pmdDuplicates = pmdDuplicates;
    }
}
