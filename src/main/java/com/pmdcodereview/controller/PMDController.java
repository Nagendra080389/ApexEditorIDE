package com.pmdcodereview.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pmdcodereview.algo.MetadataLoginUtil;
import com.pmdcodereview.exception.DeploymentException;
import com.pmdcodereview.model.ApexClassWrapper;
import com.sforce.soap.metadata.MetadataConnection;
import org.springframework.web.bind.annotation.*;

import java.io.*;

/**
 * Created by Nagendra on 18-06-2017.
 */
@RestController
public class PMDController {


    @RequestMapping(value = "/getApexBody", method = RequestMethod.GET)
    public String getApexBody() throws IOException {

        try {
            ApexClassWrapper main = MetadataLoginUtil.getApexBody();
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
