package com.forceFilesEditor.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.forceFilesEditor.algo.MetadataLoginUtil;
import com.forceFilesEditor.exception.DeploymentException;
import com.forceFilesEditor.model.ApexClassWrapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sforce.soap.tooling.Method;
import com.sforce.soap.tooling.Symbol;
import com.sforce.soap.tooling.SymbolTable;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.coyote.http2.ConnectionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @RequestMapping(value = "/getAllApexClasses", method = RequestMethod.POST)
    public String getAllApexClasses(HttpServletResponse response, HttpServletRequest request) throws IOException {

        String partnerURL = this.partnerURL;
        String toolingURL = this.toolingURL;
        Cookie[] cookies = request.getCookies();
        try {
            generateSymbolTable(partnerURL, toolingURL,cookies);
            List<ApexClassWrapper> allApexClasses = MetadataLoginUtil.getAllApexClasses(partnerURL, toolingURL,cookies);
            List<String> allClassesInString = new ArrayList<>();

            for (ApexClassWrapper allApexClass : allApexClasses) {
                allClassesInString.add(allApexClass.getName());
            }

            stringListHashMap.put("suggestions",allClassesInString);

            Gson gson = new GsonBuilder().create();
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

    @RequestMapping(value = "/apex/getMethodSuggestion", method = RequestMethod.GET)
    public String getMethodSuggestion() throws IOException {
        Gson gson = new GsonBuilder().create();
        Map<String, Map<String, List<String>>> newMapReturn = new HashMap<>();
        for (SymbolTable symbolTable : symbolTableMap.values()) {
            List<String> newListToBeAdded = new ArrayList<>();
            Map<String, List<String>> newMapToBeAdded = new HashMap<>();

            if(symbolTable != null) {
                for (Method method : symbolTable.getMethods()) {
                    String name = method.getName();
                    if(newMapToBeAdded.containsKey("methods")){
                        List<String> strings = newMapToBeAdded.get("methods");
                        strings.add(name);
                    }else {
                        List<String> strings = new ArrayList<>();
                        strings.add(name);
                        newMapToBeAdded.put("methods",strings);
                    }
                }

                for (Symbol constructors : symbolTable.getConstructors()) {
                    String name = constructors.getName();
                    if(newMapToBeAdded.containsKey("constructors")){
                        List<String> strings = newMapToBeAdded.get("constructors");
                        strings.add(name);
                    }else {
                        List<String> strings = new ArrayList<>();
                        strings.add(name);
                        newMapToBeAdded.put("constructors",strings);
                    }
                }

                for (Symbol properties : symbolTable.getProperties()) {
                    String name = properties.getName();
                    if(newMapToBeAdded.containsKey("properties")){
                        List<String> strings = newMapToBeAdded.get("properties");
                        strings.add(name);
                    }else {
                        List<String> strings = new ArrayList<>();
                        strings.add(name);
                        newMapToBeAdded.put("properties",strings);
                    }
                }
            }

            if (symbolTable != null) {
                newMapReturn.put(symbolTable.getName(), newMapToBeAdded);
            }
        }

        return gson.toJson(newMapReturn);
    }


    public void generateSymbolTable(String partnerURL, String toolingURL, Cookie[] cookies) throws IOException, ConnectionException, com.sforce.ws.ConnectionException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    symbolTableMap = MetadataLoginUtil.generateSymbolTable(partnerURL, toolingURL,cookies);
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
    public String getApexBody(@RequestParam String apexClassName, HttpServletResponse response, HttpServletRequest request) throws IOException {

        String partnerURL = this.partnerURL;
        String toolingURL = this.toolingURL;
        Cookie[] cookies = request.getCookies();
        try {
            ApexClassWrapper main = MetadataLoginUtil.getApexBody(apexClassName, partnerURL, toolingURL,cookies);
            Gson gson = new GsonBuilder().create();
            return gson.toJson(main);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/modifyApexBody", method = RequestMethod.POST)
    public String modifyApexBody(@RequestBody ApexClassWrapper apexClassWrapper,HttpServletResponse response, HttpServletRequest request) throws Exception {

        String partnerURL = this.partnerURL;
        String toolingURL = this.toolingURL;
        Cookie[] cookies = request.getCookies();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        try {
            if(apexClassWrapper == null) return null;
            MetadataLoginUtil metadataLoginUtil = new MetadataLoginUtil();
            ApexClassWrapper modifiedClass = metadataLoginUtil.modifyApexBody(apexClassWrapper, partnerURL, toolingURL,cookies);
            if(modifiedClass.isCompilationError()){
                String errorMessage = gson.toJson(modifiedClass.getLineNumberError());
                return gson.toJson(modifiedClass);
            }
            return gson.toJson(modifiedClass);

        } catch (DeploymentException e) {
            throw e;
        }

    }

    @RequestMapping(value = "/createFile", method = RequestMethod.GET)
    public String createFile() throws Exception {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        try {
            ApexClassWrapper modifiedClass = MetadataLoginUtil.createFiles("",null);

            return gson.toJson(modifiedClass);

        } catch (DeploymentException e) {
            throw e;
        }

    }

    @RequestMapping(value = "/auth", method = RequestMethod.GET)
    public void auth(@RequestParam String code, @RequestParam String state, ServletResponse response, ServletRequest request) throws Exception {

        String environment = null;
        if(state.equals("b")) {
            environment = "https://login.salesforce.com/services/oauth2/token";
        }else {
            environment = "https://test.salesforce.com/services/oauth2/token";
        }
        HttpClient httpClient = new HttpClient();

        PostMethod post = new PostMethod(environment);
        post.addParameter("code",code);
        post.addParameter("grant_type","authorization_code");
        post.addParameter("redirect_uri","https://2e0bf63e.ngrok.io/auth");
        post.addParameter("client_id","3MVG9d8..z.hDcPLDlm9QqJ3hRZOLrqvRAQajMY8Oxx9oDmHejwyUiK6qG4r4pGjvw6x2ts_8ps125hIMn9Pz");
        post.addParameter("client_secret","7957205307299792687");

        httpClient.executeMethod(post);
        String responseBody = post.getResponseBodyAsString();



        String accessToken = null;
        String issuedAt = null;
        String signature = null;
        String id_token = null;
        String instance_url = null;
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = null;

        jsonObject = parser.parse(responseBody).getAsJsonObject();


        try {

            accessToken = jsonObject.get("access_token").getAsString();
            issuedAt = jsonObject.get("issued_at").getAsString();
            signature = jsonObject.get("signature").getAsString();
            id_token = jsonObject.get("id_token").getAsString();
            instance_url = jsonObject.get("instance_url").getAsString();

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            post.releaseConnection();
        }

        HttpServletResponse httpResponse = (HttpServletResponse)response;
        Cookie session1 = new Cookie("ACCESS_TOKEN", accessToken);
        Cookie session2 = new Cookie("INSTANCE_URL", instance_url);
        Cookie session3 = new Cookie("ID_TOKEN", id_token);
        session1.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        httpResponse.addCookie(session1);
        httpResponse.addCookie(session2);
        httpResponse.addCookie(session3);
        httpResponse.sendRedirect("/html/apexEditor.html");

    }
}
