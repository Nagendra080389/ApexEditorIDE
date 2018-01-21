package com.pmdcodereview.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pmdcodereview.algo.MetadataLoginUtil;
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
    public String modifyApexBody(@RequestBody ApexClassWrapper apexClassWrapper) throws IOException {

        try {
            if(apexClassWrapper == null) return null;
            ApexClassWrapper main = MetadataLoginUtil.modifyApexBody(apexClassWrapper);
            Gson gson = new GsonBuilder().create();
            return gson.toJson(main);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
