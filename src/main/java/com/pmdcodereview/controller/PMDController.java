package com.pmdcodereview.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pmdcodereview.algo.MetadataLoginUtil;
import com.pmdcodereview.exception.DeploymentException;
import com.pmdcodereview.model.ApexClassWrapper;
import com.sforce.soap.metadata.MetadataConnection;
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

    @PostConstruct
    @RequestMapping(value = "/getAllApexClasses", method = RequestMethod.GET)
    public String getAllApexClasses() throws IOException {

        try {
            List<ApexClassWrapper> allApexClasses = MetadataLoginUtil.getAllApexClasses();
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

    @RequestMapping(value = "/getApexBody", method = RequestMethod.GET)
    public String getApexBody(@RequestParam String apexClassName) throws IOException {

        try {
            ApexClassWrapper main = MetadataLoginUtil.getApexBody(apexClassName);
            Gson gson = new GsonBuilder().create();
            return gson.toJson(main);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/modifyApexBody", method = RequestMethod.POST)
    public String modifyApexBody(@RequestBody ApexClassWrapper apexClassWrapper) throws Exception {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        try {
            if(apexClassWrapper == null) return null;
            ApexClassWrapper modifiedClass = MetadataLoginUtil.modifyApexBody(apexClassWrapper);
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
