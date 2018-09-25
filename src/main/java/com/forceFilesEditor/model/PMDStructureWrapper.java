package com.forceFilesEditor.model;

import java.util.List;

/**
 * Created by Nagendra on 12-08-2017.
 */
public class PMDStructureWrapper {

    private List<PMDStructure> pmdStructures;
    private boolean soqlInForLoop;

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
}
