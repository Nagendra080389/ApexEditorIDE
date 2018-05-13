package com.forceFilesEditor.algo;

import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

public class CompareDiff {

    public static boolean checkForEquals(String bodyFromOrg, String backUpBodyFromOrg){
        return bodyFromOrg.equals(backUpBodyFromOrg);
    }
}
