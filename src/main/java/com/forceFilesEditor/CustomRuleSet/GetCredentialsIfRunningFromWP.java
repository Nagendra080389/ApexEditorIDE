package com.forceFilesEditor.CustomRuleSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GetCredentialsIfRunningFromWP {
    public static final String FILE_NAME = "C:\\NgRock\\ngrok-stable-windows-amd64\\pass.txt";

    public static String getUserName() throws IOException {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        FileReader fileReader = new FileReader(FILE_NAME);
        createMapOfProperties(fileReader, propertiesMap);
        return propertiesMap.get("username");
    }

    public static String getPassword() throws IOException {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        FileReader fileReader = new FileReader(FILE_NAME);
        createMapOfProperties(fileReader, propertiesMap);
        return propertiesMap.get("password");
    }

    public static String getToolingAPI() throws IOException {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        FileReader fileReader = new FileReader(FILE_NAME);
        createMapOfProperties(fileReader, propertiesMap);
        return propertiesMap.get("toolingapi");
    }

    public static String getPartnerAPI() throws IOException {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        FileReader fileReader = new FileReader(FILE_NAME);
        createMapOfProperties(fileReader, propertiesMap);
        return propertiesMap.get("partnerapi");
    }


    private static void createMapOfProperties(FileReader fileReader, Map<String, String> propertiesMap) throws IOException {
        BufferedReader bufferedReader = null;
        String sCurrentLine;

        bufferedReader = new BufferedReader(fileReader);

        while ((sCurrentLine = bufferedReader.readLine()) != null) {
            sCurrentLine = sCurrentLine.replaceAll("\\s+", "");
            String[] split = sCurrentLine.split("=");
            propertiesMap.put(split[0], split[1]);

        }
    }
}
