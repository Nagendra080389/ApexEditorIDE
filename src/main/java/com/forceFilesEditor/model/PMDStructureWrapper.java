package com.forceFilesEditor.model;

import java.util.List;

/**
 * Created by Nagendra on 12-08-2017.
 */
public class PMDStructureWrapper implements Comparable<PMDStructureWrapper>{

    private List<PMDStructure> pmdStructures;
    private boolean soqlInForLoop;
    private Integer totalHighErrors = 0;
    private Integer totalMediumErrors = 0;
    private Integer totalLowErrors = 0;

    public List<PMDStructure> getPmdStructures() {
        return pmdStructures;
    }

    public void setPmdStructures(List<PMDStructure> pmdStructures) {
        this.pmdStructures = pmdStructures;
    }

    public boolean isSoqlInForLoop() {
        return soqlInForLoop;
    }

    public void setSoqlInForLoop(boolean soqlInForLoop) {
        this.soqlInForLoop = soqlInForLoop;
    }

    public Integer getTotalHighErrors() {
        return totalHighErrors;
    }

    public void setTotalHighErrors(Integer totalHighErrors) {
        this.totalHighErrors = totalHighErrors;
    }

    public Integer getTotalMediumErrors() {
        return totalMediumErrors;
    }

    public void setTotalMediumErrors(Integer totalMediumErrors) {
        this.totalMediumErrors = totalMediumErrors;
    }

    public Integer getTotalLowErrors() {
        return totalLowErrors;
    }

    public void setTotalLowErrors(Integer totalLowErrors) {
        this.totalLowErrors = totalLowErrors;
    }

    @Override
    public int compareTo(PMDStructureWrapper pmdStructureWrapper) {
        return Integer.compare(this.totalHighErrors, pmdStructureWrapper.totalHighErrors);
    }
}
