package com.forceFilesEditor.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.forceFilesEditor.algo.MetadataLoginUtil;
import com.forceFilesEditor.exception.DeploymentException;
import com.forceFilesEditor.model.ApexClassWrapper;
import com.sforce.soap.tooling.Method;
import com.sforce.soap.tooling.SymbolTable;
import org.apache.coyote.http2.ConnectionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

/**
 * Created by Nagendra on 18-06-2017.
 */
@RestController
public class PMDController {


    private Map<String, List<String>> stringListHashMap = new HashMap<>();
    private volatile Map<String, SymbolTable> symbolTableMap = new HashMap<>();

    @Value("${partnerURL}")
    volatile String partnerURL;

    @Value("${toolingURL}")
    volatile String toolingURL;

    @PostConstruct
    public String getAllApexClasses() throws IOException {

        String partnerURL = this.partnerURL;
        String toolingURL = this.toolingURL;
        try {
            generateSymbolTable(partnerURL, toolingURL);
            List<ApexClassWrapper> allApexClasses = MetadataLoginUtil.getAllApexClasses(partnerURL, toolingURL);
            List<String> allClassesInString = new ArrayList<>();

            for (ApexClassWrapper allApexClass : allApexClasses) {
                allClassesInString.add(allApexClass.getName());
            }

            stringListHashMap.put("suggestions",allClassesInString);

            Gson gson = new GsonBuilder().create();
            //apexClassesJSON = gson.toJson(stringListHashMap);
            return gson.toJson(allApexClasses);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/getSuggestion", method = RequestMethod.POST)
    public String getSuggestion(String query) throws IOException {
        Gson gson = new GsonBuilder().create();
        List<String> strings = stringListHashMap.get("suggestions");
        Map<String, List<String>> newMapReturn = new HashMap<>();
        List<String> newListToBeAdded = new ArrayList<>();
        Iterator itr = strings.iterator();
        while (itr.hasNext())
        {
            String x = (String)itr.next();
            if (x.toLowerCase().contains(query.toLowerCase()))
                newListToBeAdded.add(x);
        }

        newMapReturn.put("suggestions", newListToBeAdded);
        return gson.toJson(newMapReturn);
    }

    @RequestMapping(value = "/getMethodSuggestion", method = RequestMethod.POST)
    public String getMethodSuggestion(String query) throws IOException {
        Gson gson = new GsonBuilder().create();
        SymbolTable symbolTable = symbolTableMap.get(query);
        Map<String, List<String>> newMapReturn = new HashMap<>();
        List<String> newListToBeAdded = new ArrayList<>();

        if(symbolTable != null) {
            for (Method method : symbolTable.getMethods()) {
                String name = method.getName();
                newListToBeAdded.add(name);
            }
        }

        newMapReturn.put("hints", newListToBeAdded);
        return gson.toJson(newMapReturn);
    }


    public void generateSymbolTable(String partnerURL, String toolingURL) throws IOException, ConnectionException, com.sforce.ws.ConnectionException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    symbolTableMap = MetadataLoginUtil.generateSymbolTable(partnerURL, toolingURL);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ConnectionException e) {
                    e.printStackTrace();
                } catch (com.sforce.ws.ConnectionException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    @RequestMapping(value = "/getApexBody", method = RequestMethod.GET)
    public String getApexBody(@RequestParam String apexClassName) throws IOException {

        String partnerURL = this.partnerURL;
        String toolingURL = this.toolingURL;
        try {
            ApexClassWrapper main = MetadataLoginUtil.getApexBody(apexClassName, partnerURL, toolingURL);
            Gson gson = new GsonBuilder().create();
            return gson.toJson(main);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/modifyApexBody", method = RequestMethod.POST)
    public String modifyApexBody(@RequestBody ApexClassWrapper apexClassWrapper) throws Exception {

        String partnerURL = this.partnerURL;
        String toolingURL = this.toolingURL;
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        try {
            if(apexClassWrapper == null) return null;
            ProcessBuilder processBuilder = new ProcessBuilder(new ClassPathResource("deleteFiles.bat").getFile().getAbsolutePath());
            Process process = processBuilder.start();
            process.waitFor();
            ApexClassWrapper modifiedClass = MetadataLoginUtil.modifyApexBody(apexClassWrapper, partnerURL, toolingURL);
            if(modifiedClass.isCompilationError()){
                String errorMessage = gson.toJson(modifiedClass.getLineNumberError());
                throw new DeploymentException(errorMessage);
            }
            return gson.toJson(modifiedClass);

        } catch (DeploymentException e) {
            throw e;
        }

    }


}
