package com.forceFilesEditor.controller;

import com.forceFilesEditor.algo.MetadataLoginUtil;
import com.forceFilesEditor.dao.RuleSetsDomainMongoRepository;
import com.forceFilesEditor.dao.UserDomainMongoRepository;
import com.forceFilesEditor.exception.DeploymentException;
import com.forceFilesEditor.model.ApexClassWrapper;
import com.forceFilesEditor.model.RuleSetsDomain;
import com.forceFilesEditor.model.User;
import com.forceFilesEditor.model.UserDomain;
import com.forceFilesEditor.ruleSets.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sforce.soap.tooling.SymbolTable;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.coyote.http2.ConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by Nagendra on 18-06-2017.
 */
@RestController
public class PMDController {

    @Autowired
    private ConvertXmlToObjects convertXmlToObjects;

    @Autowired
    private RuleSetsDomainMongoRepository ruleSetsDomainMongoRepository;

    @Autowired
    private UserDomainMongoRepository userDomainMongoRepository;

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
        Gson gson = new GsonBuilder().create();
        try {

            List<ApexClassWrapper> allApexClasses = MetadataLoginUtil.getAllApexClasses(partnerURL, toolingURL,
                    cookies, response);
            List<String> allClassesInString = new ArrayList<>();

            for (ApexClassWrapper allApexClass : allApexClasses) {
                allClassesInString.add(allApexClass.getName());
            }

            stringListHashMap.put("suggestions", allClassesInString);

            return gson.toJson(allApexClasses);

        } catch (Exception e) {
            return gson.toJson(e.getMessage());
        }

    }

    @RequestMapping(value = "/getAllApexClassesNames", method = RequestMethod.GET)
    public String getAllApexClassesNames(HttpServletResponse response, HttpServletRequest request) throws IOException {

        String partnerURL = this.partnerURL;
        String toolingURL = this.toolingURL;
        Cookie[] cookies = request.getCookies();
        Gson gson = new GsonBuilder().create();
        try {
            List<ApexClassWrapper> allApexClasses = MetadataLoginUtil.getAllApexClasses(partnerURL, toolingURL,
                    cookies, response);
            return gson.toJson(allApexClasses);

        } catch (Exception e) {
            return gson.toJson(e.getMessage());
        }

    }

    public String getReturnSymbolTable(String partnerURL, String toolingURL, Cookie[] cookies, OutputStream
            outputStream, Gson gson)
            throws IOException, ConnectionException, com.sforce.ws.ConnectionException {
        MetadataLoginUtil metadataLoginUtil = new MetadataLoginUtil();
        List<String> strings = new ArrayList<>();
        try {
            strings = metadataLoginUtil.returnSymbolTable(outputStream, gson);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return "";
    }

    @RequestMapping(value = "/getApexBody", method = RequestMethod.GET)
    public String getApexBody(@RequestParam String apexClassName, HttpServletResponse response, HttpServletRequest
            request) throws IOException {

        String partnerURL = this.partnerURL;
        String toolingURL = this.toolingURL;
        Cookie[] cookies = request.getCookies();
        try {
            if (!apexClassName.equals("New Apex Class....")) {
                ApexClassWrapper main = MetadataLoginUtil.getApexBody(apexClassName, partnerURL, toolingURL, cookies);
                Gson gson = new GsonBuilder().create();
                return gson.toJson(main);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/modifyApexBody", method = RequestMethod.POST)
    public String modifyApexBody(@RequestBody ApexClassWrapper apexClassWrapper, HttpServletResponse response,
                                 HttpServletRequest request) throws Exception {

        String partnerURL = this.partnerURL;
        String toolingURL = this.toolingURL;
        Cookie[] cookies = request.getCookies();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        try {
            if (apexClassWrapper == null) return null;
            MetadataLoginUtil metadataLoginUtil = new MetadataLoginUtil();
            ApexClassWrapper modifiedClass = metadataLoginUtil.modifyApexBody(apexClassWrapper, partnerURL,
                    toolingURL, cookies, false, ruleSetsDomainMongoRepository);
            if (modifiedClass.isCompilationError()) {
                return gson.toJson(modifiedClass);
            }
            return gson.toJson(modifiedClass);

        } catch (DeploymentException e) {
            throw e;
        }

    }

    @RequestMapping(value = "/saveModifiedApexBody", method = RequestMethod.POST)
    public String saveModifiedApexBody(@RequestBody ApexClassWrapper apexClassWrapper, HttpServletResponse response,
                                       HttpServletRequest request) throws Exception {

        String partnerURL = this.partnerURL;
        String toolingURL = this.toolingURL;
        Cookie[] cookies = request.getCookies();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        try {
            if (apexClassWrapper == null) return null;
            MetadataLoginUtil metadataLoginUtil = new MetadataLoginUtil();
            ApexClassWrapper apexClassWrapper1 = metadataLoginUtil.modifyApexBody(apexClassWrapper, partnerURL,
                    toolingURL, cookies, true, ruleSetsDomainMongoRepository);
            if (apexClassWrapper1.isTimeStampNotMatching()) {
                return gson.toJson(apexClassWrapper1);
            }

        } catch (DeploymentException e) {
            return gson.toJson(e.getStackTrace());
        }
        apexClassWrapper.setTimeStampNotMatching(false);
        return gson.toJson(apexClassWrapper);

    }

    @RequestMapping(value = "/createFile", method = RequestMethod.POST)
    public String createFile(@RequestBody String apexClassName, HttpServletResponse response, HttpServletRequest
            request) throws Exception {
        String partnerURL = this.partnerURL;
        String toolingURL = this.toolingURL;
        Cookie[] cookies = request.getCookies();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        try {
            ApexClassWrapper modifiedClass = MetadataLoginUtil.createFiles("", apexClassName, partnerURL, toolingURL,
                    cookies, ruleSetsDomainMongoRepository);
            return gson.toJson(modifiedClass);
        } catch (DeploymentException e) {
            return gson.toJson(e.getStackTrace());
        }

    }

    @RequestMapping(value = "/auth", method = RequestMethod.GET, params = {"code", "state"})
    public void auth(@RequestParam String code, @RequestParam String state, ServletResponse response, ServletRequest
            request) throws Exception {

        String environment = null;
        if (state.equals("b")) {
            environment = "https://login.salesforce.com/services/oauth2/token";
        } else if (state.contains("CustomDomain")) {
            environment = "https://" + state.split(",")[0] + ".my.salesforce.com/services/oauth2/token";
        } else {
            environment = "https://test.salesforce.com/services/oauth2/token";
        }

        System.out.println("environment -> " + environment);
        HttpClient httpClient = new HttpClient();

        PostMethod post = new PostMethod(environment);
        post.addParameter("code", code);
        post.addParameter("grant_type", "authorization_code");
        post.addParameter("redirect_uri", "https://apexeditortooldev.herokuapp.com/auth");
        post.addParameter("client_id", "3MVG9d8..z.hDcPLDlm9QqJ3hRfsVqxDxkNuH__5ke7onhRrPniHqH_KRi53jOM_9V_TOQPVsRmEL2McIFJtb");
        post.addParameter("client_secret", "8493474984808415138");

        httpClient.executeMethod(post);
        String responseBody = post.getResponseBodyAsString();

        String accessToken = null;
        String issuedAt = null;
        String signature = null;
        String id_token = null;
        String instance_url = null;
        String useridURL = null;
        String username = null;
        String display_name = null;
        String email = null;
        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();


        try {

            accessToken = jsonObject.get("access_token").getAsString();
            issuedAt = jsonObject.get("issued_at").getAsString();
            signature = jsonObject.get("signature").getAsString();
            id_token = jsonObject.get("id_token").getAsString();
            instance_url = jsonObject.get("instance_url").getAsString();
            useridURL = jsonObject.get("id").getAsString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
        }

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        Cookie session1 = new Cookie("ACCESS_TOKEN", accessToken);
        Cookie session2 = new Cookie("INSTANCE_URL", instance_url);
        Cookie session3 = new Cookie("ID_TOKEN", id_token);
        Cookie session4 = new Cookie("USERIDURL", useridURL);
        session1.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        session2.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        session3.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        session4.setMaxAge(-1); //cookie not persistent, destroyed on browser exit
        httpResponse.addCookie(session1);
        httpResponse.addCookie(session2);
        httpResponse.addCookie(session3);
        httpResponse.addCookie(session4);
        httpResponse.sendRedirect("/html/apexEditor.html");

    }

    @RequestMapping(value = "/auth", method = RequestMethod.GET, params = {"error", "error_description", "state"})
    public void authErrorHandle(@RequestParam String error, @RequestParam String error_description, @RequestParam
            String state,HttpServletResponse response, HttpServletRequest request) throws Exception {

        Cookie errorCookie = new Cookie("ERROROAUTH", URLEncoder.encode(error, "UTF-8"));
        Cookie errorDescCookie = new Cookie("ERROROAUTHDESC", URLEncoder.encode(error_description, "UTF-8"));
        response.addCookie(errorCookie);
        response.addCookie(errorDescCookie);
        response.sendRedirect("/index.html");

    }

    @RequestMapping(value = "/getCurrentUser", method = RequestMethod.GET)
    public String getCurrentUser(HttpServletResponse response, HttpServletRequest request) throws Exception {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Cookie[] cookies = request.getCookies();
        String accessToken = null;
        String useridURL = null;
        String username = null;
        String display_name = null;
        String email = null;
        String orgId = null;
        String customDomain = null;
        String userId = null;
        User user = new User();
        System.out.println("cookies -> "+cookies);
        if (cookies == null) {
            user.setError("No cookies found");
            return gson.toJson(user);
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("ACCESS_TOKEN")) {
                accessToken = cookie.getValue();
            }
            if (cookie.getName().equals("USERIDURL")) {
                useridURL = cookie.getValue();
            }
        }
        HttpClient httpClient = new HttpClient();

        GetMethod getMethod = new GetMethod(useridURL);
        getMethod.addRequestHeader("Authorization", "Bearer " + accessToken);
        httpClient.executeMethod(getMethod);
        String responseUserName = getMethod.getResponseBodyAsString();
        JsonParser parser = new JsonParser();

        JsonObject jsonObject = null;

        try {
            jsonObject = parser.parse(responseUserName).getAsJsonObject();
            username = jsonObject.get("username").getAsString();
            display_name = jsonObject.get("display_name").getAsString();
            email = jsonObject.get("email").getAsString();
            orgId = jsonObject.get("organization_id").getAsString();
            userId = jsonObject.get("user_id").getAsString();
            if (!jsonObject.get("urls").isJsonNull()) {
                customDomain = jsonObject.get("urls").getAsJsonObject().get("custom_domain").getAsString();
            }
        } catch (Exception e) {
            user.setError(e.getMessage());
            return gson.toJson(user);
        } finally {
            getMethod.releaseConnection();
        }


        user.setDisplay_name(display_name);
        user.setEmail(email);
        user.setUsername(username);
        user.setOrgId(orgId);
        user.setDomainName(customDomain);
        user.setUserId(userId);

        UserDomain userDomain = new UserDomain();
        userDomain.setAdmin(false);
        userDomain.setPmdStructures(new ArrayList<>());
        userDomain.setUserName(display_name);
        userDomain.setOrgId(orgId);
        userDomain.setUserId(userId);

        UserDomain byUserIdAndOrgId = userDomainMongoRepository.findByUserIdAndOrgId(userId, orgId);

        if(byUserIdAndOrgId == null) {
            userDomainMongoRepository.save(userDomain);
        }

        return gson.toJson(user);
    }

    @RequestMapping("/generateCustomSymbolTable")
    public StreamingResponseBody generateCustomSymbolTable(HttpServletResponse response, HttpServletRequest request) {
        response.addHeader("Content-Type", MediaType.APPLICATION_JSON);
        Gson gson = new GsonBuilder().create();
        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                try {
                    PMDController.this.generateCustomSymbolTable(response, request, outputStream, gson);
                } finally {
                    outputStream.write(gson.toJson("LastByte").getBytes());
                    IOUtils.closeQuietly(outputStream);
                }
            }
        };
    }

    @RequestMapping("/generateSystemSymbolTable")
    public StreamingResponseBody generateSystemSymbolTable(HttpServletResponse response, HttpServletRequest request) {
        response.addHeader("Content-Type", MediaType.APPLICATION_JSON);
        Gson gson = new GsonBuilder().create();
        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                try {
                    PMDController.this.generateSystemSymbolTable(response, request, outputStream, gson);
                } finally {
                    outputStream.write(gson.toJson("LastByte").getBytes());
                    IOUtils.closeQuietly(outputStream);
                }
            }
        };
    }

    private String generateSystemSymbolTable(HttpServletResponse response, HttpServletRequest request, OutputStream
            outputStream, Gson gson) {
        String partnerURL = this.partnerURL;
        String toolingURL = this.toolingURL;
        Cookie[] cookies = request.getCookies();
        try {
            getReturnSymbolTable(partnerURL, toolingURL, cookies, outputStream, gson);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (com.sforce.ws.ConnectionException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String generateCustomSymbolTable(HttpServletResponse response, HttpServletRequest request, OutputStream
            outputStream, Gson gson) {
        String partnerURL = this.partnerURL;
        String toolingURL = this.toolingURL;
        Cookie[] cookies = request.getCookies();
        try {
            MetadataLoginUtil.generateSymbolTable(partnerURL, toolingURL, cookies, outputStream, gson, response);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (com.sforce.ws.ConnectionException e) {
            e.printStackTrace();
        }
        return "";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public void logout(HttpServletResponse response, HttpServletRequest request) throws Exception {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        response.sendRedirect("/index.html");

    }

    @RequestMapping(value = "/getRuleEngine", method = RequestMethod.POST)
    public RuleSetWrapperExposed getRuleEngine(@RequestBody User userFromUI) throws Exception {
        UserDomain byUserIdAndOrgId = userDomainMongoRepository.findByUserIdAndOrgId(userFromUI.getUserId(),
                userFromUI.getOrgId());
        if(!byUserIdAndOrgId.getAdmin()){
            return null;
        }
        RuleSetWrapperExposed ruleSetWrapperExposed = new RuleSetWrapperExposed();
        RuleSetsDomain byorgId = ruleSetsDomainMongoRepository.findByOrgId(userFromUI.getOrgId());
        List<RuleSetWrapper> ruleSetWrappers = new ArrayList<>();
        Set<String> listOfPriorities = new HashSet<>();
        RuleSetWrapper ruleSetWrapper = null;
        RulesetType dbRuleSets = null;
        if (byorgId == null) {
            dbRuleSets = convertXmlToObjects.convertToObjects(null);
        } else {
            ruleSetWrappers = byorgId.getRuleSetWrappers();
            dbRuleSets = convertXmlToObjects.convertToObjects(byorgId.getRuleSetXML());
            dbRuleSets.setRule(new ArrayList<>());
        }
        if (dbRuleSets != null && dbRuleSets.getRule() != null && !dbRuleSets.getRule().isEmpty()) {
            Collections.sort(dbRuleSets.getRule());

            for (RuleType ruleType : dbRuleSets.getRule()) {
                ruleSetWrapper = new RuleSetWrapper();
                ruleType.setRef(ruleType.getRef());
                listOfPriorities.add(ruleType.getPriority());
                ruleSetWrapper.setRuleType(ruleType);
                ruleSetWrapper.setActive(true);
                ruleSetWrappers.add(ruleSetWrapper);

            }
        }
        ruleSetWrapperExposed.setListOfPriorities(listOfPriorities);
        ruleSetWrapperExposed.setRuleSetWrapper(ruleSetWrappers);
        ruleSetWrapperExposed.setRulesetType(dbRuleSets);

        return ruleSetWrapperExposed;
    }

    @RequestMapping(value = "/modifyRuleEngine", method = RequestMethod.POST)
    public void modifyRuleEngine(@RequestBody RuleSetWrapperExposed modifiedRuleSets) throws Exception {
        List<RuleType> modifiedRuleTypes = new ArrayList<>();
        List<RuleSetWrapper> ruleSetWrapper = modifiedRuleSets.getRuleSetWrapper();
        for (RuleSetWrapper wrapper : ruleSetWrapper) {
            if (wrapper.getActive()) {
                modifiedRuleTypes.add(wrapper.getRuleType());
            }
        }
        RulesetType rulesetType = modifiedRuleSets.getRulesetType();
        rulesetType.setRule(modifiedRuleTypes);
        String xmlString = convertXmlToObjects.convertFromObjects(modifiedRuleSets.getRulesetType());
        RuleSetsDomain byorgId = ruleSetsDomainMongoRepository.findByOrgId(modifiedRuleSets.getOrgId());
        RuleSetsDomain ruleSetsDomain = byorgId == null ? new RuleSetsDomain() : byorgId;
        ruleSetsDomain.setOrgId(modifiedRuleSets.getOrgId());
        ruleSetsDomain.setRuleSetXML(xmlString);
        ruleSetsDomain.setRuleSetWrappers(modifiedRuleSets.getRuleSetWrapper());
        ruleSetsDomainMongoRepository.save(ruleSetsDomain);

    }


}
