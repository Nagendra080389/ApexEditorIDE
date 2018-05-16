
package com.forceFilesEditor.model;

public abstract class AbstractCompletionProposalDisplayable {
    public abstract String getReplacementString();

    public abstract String getDisplayString();

    public int cursorPosition() {
        return getReplacementString().length();
    }
}
