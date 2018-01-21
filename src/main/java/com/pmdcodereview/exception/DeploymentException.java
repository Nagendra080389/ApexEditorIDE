package com.pmdcodereview.exception;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class DeploymentException extends Exception {

    public DeploymentException(String message) {
        super(message);
    }

    /*@Override
    public String getMessage() {
        String message = super.getMessage();
        String s = StringUtils.substringBetween(message, "{", "}");
        String[] keyValuePairs = s.split(",");
        Map<String, String> map = new HashMap<>();

        for (String pair : keyValuePairs){                      //iterate over the pairs
            String[] entry = pair.split("=");                   //split the pairs to get key and value
            map.put(entry[0].trim(), entry[1].trim());          //add them to the hashmap and trim whitespaces
        }
        return super.getMessage();
    }*/
}
