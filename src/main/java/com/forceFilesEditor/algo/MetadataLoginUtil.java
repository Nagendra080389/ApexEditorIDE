package com.forceFilesEditor.algo;

import com.forceFilesEditor.model.*;
import com.forceFilesEditor.pmd.PmdReviewService;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.tooling.*;
import com.sforce.soap.tooling.sobject.*;
import com.sforce.ws.ConnectorConfig;
import net.sourceforge.pmd.*;
import net.sourceforge.pmd.util.ResourceLoader;
import org.apache.commons.io.IOUtils;
import org.apache.coyote.http2.ConnectionException;
import org.springframework.core.io.ClassPathResource;
import wiremock.org.apache.commons.collections4.trie.PatriciaTrie;

import javax.servlet.http.Cookie;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.*;

public class MetadataLoginUtil {
    public static final String FILE_NAME = "C:\\JenkinsPOC\\Jenkins\\ConfigurationFileForIDE.txt";

    static PartnerConnection partnerConnection;
    static MetadataConnection metadataConnection;

    public static ApexClassWrapper getApexBody(String className, String partnerURL, String toolingURL, Cookie[] cookies) throws Exception {

        String instanceUrl = null;
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("ACCESS_TOKEN")){
                accessToken = cookie.getValue();
            }
            if(cookie.getName().equals("INSTANCE_URL")){
                instanceUrl = cookie.getValue();
                instanceUrl = instanceUrl + partnerURL;
            }
        }


        ConnectorConfig config = new ConnectorConfig();
        config.setSessionId(accessToken);
        config.setServiceEndpoint(instanceUrl);

        try {
            try {
                partnerConnection = Connector.newConnection(config);
                metadataConnection = com.sforce.soap.metadata.Connector.newConnection(config);
            } catch (Exception e) {
                throw new com.sforce.ws.ConnectionException("Cannot connect to Org");
            }

            String apexClassBody = "SELECT Id,Body,LastModifiedDate, Name FROM APEXCLASS Where Name = '" + className + "'";
            QueryResult query = partnerConnection.query(apexClassBody);

            Object body = query.getRecords()[0].getField("Body");
            Object name = query.getRecords()[0].getField("Name");
            Object id = query.getRecords()[0].getField("Id");

            ApexClassWrapper apexClassWrapper = new ApexClassWrapper();
            apexClassWrapper.setName(name.toString());
            apexClassWrapper.setBody(body.toString());
            apexClassWrapper.setId(id.toString());
            apexClassWrapper.setOriginalBodyFromOrg(body.toString());

            return apexClassWrapper;

        } catch (com.sforce.ws.ConnectionException e) {
            throw new com.sforce.ws.ConnectionException(e.getMessage());
        }

    }

    public ApexClassWrapper modifyApexBody(ApexClassWrapper apexClassWrapper, String partnerURL, String toolingURL, Cookie[] cookies, boolean save) throws Exception {

        String instanceUrl = null;
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("ACCESS_TOKEN")){
                accessToken = cookie.getValue();
            }
            if(cookie.getName().equals("INSTANCE_URL")){
                instanceUrl = cookie.getValue();
                instanceUrl = instanceUrl + toolingURL;
            }
        }

        ConnectorConfig config = new ConnectorConfig();
        config.setSessionId(accessToken);
        config.setServiceEndpoint(instanceUrl);

        ConnectorConfig toolConfig = new ConnectorConfig();

        toolConfig.setServiceEndpoint(instanceUrl);
        toolConfig.setSessionId(accessToken);


        ToolingConnection toolingConnection = new ToolingConnection(toolConfig);

        BufferedReader bufferedReader = null;
        try {
            // Create a MetaData Container, this is like a bucket for ur modified member
            MetadataContainer container = new MetadataContainer();
            container.setName(String.valueOf(Math.random()));
            SObject[] con = {container};
            com.sforce.soap.tooling.SaveResult[] saveResults = toolingConnection.create(con);
            String containerId = saveResults[0].getId();


            // we create a member if we want to update and direttly ApexClass when we want to create
            ApexClassMember apexClassMember = new ApexClassMember();
            apexClassMember.setBody(apexClassWrapper.getBody());
            apexClassMember.setContentEntityId(apexClassWrapper.getId());
            apexClassMember.setMetadataContainerId(containerId);

            Map<Integer, List<String>> lineNumberError = new HashMap<>();

            con = new SObject[]{apexClassMember};
            com.sforce.soap.tooling.SaveResult[] saveMember = toolingConnection.create(con);

            if(save) {
                String apexClassBody = "SELECT Body FROM APEXCLASS Where Name = '" + apexClassWrapper.getName() + "'";
                if(partnerConnection == null){
                    partnerConnection = Connector.newConnection(config);
                }
                QueryResult query = partnerConnection.query(apexClassBody);
                Object body = query.getRecords()[0].getField("Body");
                if (!body.toString().equals(apexClassWrapper.getOriginalBodyFromOrg())) {
                    apexClassWrapper.setDataNotMatching(true);
                    ApexClassWrapper fromOrg = new ApexClassWrapper();
                    fromOrg.setBody(body.toString());
                    apexClassWrapper.setModifiedApexClassWrapper(fromOrg);
                    return apexClassWrapper;
                }
            }

            System.out.println("after return cookies -> "+apexClassWrapper);

            ContainerAsyncRequest containerAsyncRequest = new ContainerAsyncRequest();
            containerAsyncRequest.setMetadataContainerId(containerId);
            containerAsyncRequest.setIsCheckOnly(!save);

            con = new SObject[]{containerAsyncRequest};
            com.sforce.soap.tooling.SaveResult[] asyncResultMember = toolingConnection.create(con);

            String id = asyncResultMember[0].getId();
            List<PMDStructure> pmdStructures = new ArrayList<>();

            while (true) {
                /*if(save) {
                    String apexClassBody = "SELECT Body,LastModifiedDate FROM APEXCLASS Where Name = '" + apexClassWrapper.getName() + "'";
                    QueryResult query = partnerConnection.query(apexClassBody);
                    Object body = query.getRecords()[0].getField("Body");
                    Date dateFromOrg = DateUtils.parseDateStrictly((String) query.getRecords()[0].getField("LastModifiedDate"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    if (dateFromOrg.getTime() != apexClassWrapper.getSalesForceSystemModStamp().getTime()) {
                        apexClassWrapper.setTimeStampNotMatching(true);
                        ApexClassWrapper fromOrg = new ApexClassWrapper();
                        fromOrg.setBody(body.toString());
                        apexClassWrapper.setModifiedApexClassWrapper(fromOrg);
                        break;
                    }
                }*/
                com.sforce.soap.tooling.QueryResult containerSyncRequestCompile = toolingConnection.query("SELECT Id,State, DeployDetails, ErrorMsg FROM ContainerAsyncRequest where id = '" + id + "'");
                ContainerAsyncRequest sObject1 = (ContainerAsyncRequest) containerSyncRequestCompile.getRecords()[0];
                if ("Queued".equals(sObject1.getState())) {
                    Thread.sleep(5000);
                    continue;
                } else {
                    if ("Failed".equals(sObject1.getState())) {
                        DeployDetails deployDetails = sObject1.getDeployDetails();
                        if (deployDetails != null && deployDetails.getComponentFailures() != null) {
                            if (deployDetails.getComponentFailures().length > 0) {
                                apexClassWrapper.setCompilationError(true);
                                for (DeployMessage deployMessage : deployDetails.getComponentFailures()) {
                                    PMDStructure pmdStructure = new PMDStructure();
                                    pmdStructure.setLineNumber(deployMessage.getLineNumber());
                                    pmdStructure.setReviewFeedback(deployMessage.getProblem());
                                    pmdStructures.add(pmdStructure);
                                    /*if (lineNumberError.containsKey(deployMessage.getLineNumber())) {
                                        List<String> strings = lineNumberError.get(deployMessage.getLineNumber());
                                        strings.add(deployMessage.getProblem());
                                    } else {
                                        List<String> problemList = new ArrayList<>();
                                        problemList.add(deployMessage.getProblem());
                                        lineNumberError.put(deployMessage.getLineNumber(), problemList);
                                    }*/
                                }
                            }
                        }
                    }
                }
                break;
            }

            if(!save) {
                apexClassWrapper.setLineNumberError(lineNumberError);

                if (!apexClassWrapper.isCompilationError()) {
                    PMDConfiguration pmdConfiguration = new PMDConfiguration();
                    pmdConfiguration.setReportFormat("text");
                    ClassLoader classLoader = this.getClass().getClassLoader();
                    InputStream resourceAsStream = classLoader.getResourceAsStream("xml/ruleSet.xml");
                    String ruleSetFilePath = "";
                    if (resourceAsStream != null) {
                        File file = stream2file(resourceAsStream);
                        ruleSetFilePath = file.getPath();

                    }
                    pmdConfiguration.setRuleSets(ruleSetFilePath);
                    pmdConfiguration.setThreads(4);
                    SourceCodeProcessor sourceCodeProcessor = new SourceCodeProcessor(pmdConfiguration);
                    RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(pmdConfiguration, new ResourceLoader());
                    RuleSets ruleSets = RulesetsFactoryUtils.getRuleSetsWithBenchmark(pmdConfiguration.getRuleSets(), ruleSetFactory);

                    PmdReviewService pmdReviewService = new PmdReviewService(sourceCodeProcessor, ruleSets);
                    List<RuleViolation> review = pmdReviewService.review(apexClassWrapper.getBody(), apexClassWrapper.getName() + ".cls");

                    for (RuleViolation ruleViolation : review) {
                        PMDStructure pmdStructure = new PMDStructure();
                        pmdStructure.setReviewFeedback(ruleViolation.getDescription());
                        pmdStructure.setLineNumber(ruleViolation.getBeginLine());
                        pmdStructure.setRuleName(ruleViolation.getRule().getName());
                        pmdStructure.setRuleUrl(ruleViolation.getRule().getExternalInfoUrl());
                        pmdStructure.setRulePriority(ruleViolation.getRule().getPriority().getPriority());
                        pmdStructures.add(pmdStructure);

                        /*if (lineNumberError.containsKey(ruleViolation.getBeginLine())) {
                            List<String> strings = lineNumberError.get(ruleViolation.getBeginLine());
                            strings.add(ruleViolation.getDescription());
                        } else {
                            List<String> problemList = new ArrayList<>();
                            problemList.add(ruleViolation.getDescription());
                            lineNumberError.put(ruleViolation.getBeginLine(), problemList);
                        }*/
                    }
                }
            }
            apexClassWrapper.setPmdStructures(pmdStructures);
            return apexClassWrapper;

        } catch (com.sforce.ws.ConnectionException e) {
            throw new com.sforce.ws.ConnectionException(e.getMessage());

        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }

    }

    public static File stream2file (InputStream in) throws IOException {
        final File tempFile = File.createTempFile("ruleSet", ".xml");
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }catch (Exception e){
            e.printStackTrace();
        }
        return tempFile;
    }


    public static ApexClassWrapper createFiles(String type, String apexClassName, String partnerURL, String toolingURL, Cookie[] cookies) throws Exception {

        String instanceUrl = null;
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("ACCESS_TOKEN")){
                accessToken = cookie.getValue();
            }
            if(cookie.getName().equals("INSTANCE_URL")){
                instanceUrl = cookie.getValue();
                instanceUrl = instanceUrl + toolingURL;
            }
        }

        ConnectorConfig toolConfig = new ConnectorConfig();
        toolConfig.setServiceEndpoint(instanceUrl);
        toolConfig.setSessionId(accessToken);


        ToolingConnection toolingConnection = new ToolingConnection(toolConfig);
        BufferedReader bufferedReader = null;
        try {
            // Create a MetaData Container, this is like a bucket for ur modified member
            MetadataContainer container = new MetadataContainer();
            container.setName(String.valueOf(Math.random()));
            SObject[] con = {container};
            com.sforce.soap.tooling.SaveResult[] saveResults = toolingConnection.create(con);
            String containerId = saveResults[0].getId();


            // we create a member if we want to update and direttly ApexClass when we want to create


            ApexClass apexClass1 = new ApexClass();
            apexClass1.setBody("/** \n" +
                    "Class Name -> "+apexClassName +"\n"+
                    "**/\n" +
                    "public class "+apexClassName+" {\n" +
                               "\n" +
                               "}");
            ApexClassWrapper apexClassWrapper = new ApexClassWrapper();
            apexClassWrapper.setBody(apexClass1.getBody());
            apexClassWrapper.setName(apexClassName);

            con = new SObject[]{apexClass1};

            com.sforce.soap.tooling.SaveResult[] saveApex = toolingConnection.create(con);
            String apexId = saveApex[0].getId();
            ContainerAsyncRequest containerAsyncRequest = new ContainerAsyncRequest();
            containerAsyncRequest.setMetadataContainerId(containerId);
            containerAsyncRequest.setIsCheckOnly(false);

            con = new SObject[]{containerAsyncRequest};


            com.sforce.soap.tooling.SaveResult[] asyncResultMember = toolingConnection.create(con);

            String id = asyncResultMember[0].getId();
            List<PMDStructure> pmdStructures = new ArrayList<>();

            while (true) {
                com.sforce.soap.tooling.QueryResult containerSyncRequestCompile = toolingConnection.query("SELECT Id,State, DeployDetails, ErrorMsg FROM ContainerAsyncRequest where id = '" + id + "'");
                ContainerAsyncRequest sObject1 = (ContainerAsyncRequest) containerSyncRequestCompile.getRecords()[0];
                if ("Queued".equals(sObject1.getState())) {
                    Thread.sleep(5000);
                    continue;
                } else {
                    if ("Failed".equals(sObject1.getState())) {
                        DeployDetails deployDetails = sObject1.getDeployDetails();
                        if (deployDetails != null && deployDetails.getComponentFailures() != null) {
                            if (deployDetails.getComponentFailures().length > 0) {
                                apexClassWrapper.setCompilationError(true);
                                for (DeployMessage deployMessage : deployDetails.getComponentFailures()) {
                                    PMDStructure pmdStructure = new PMDStructure();
                                    pmdStructure.setLineNumber(deployMessage.getLineNumber());
                                    pmdStructure.setReviewFeedback(deployMessage.getProblem());
                                    pmdStructures.add(pmdStructure);
                                }
                            }
                        }
                    }
                }
                break;
            }
            apexClassWrapper.setPmdStructures(pmdStructures);

            return apexClassWrapper;

        } catch (com.sforce.ws.ConnectionException e) {
            throw new com.sforce.ws.ConnectionException(e.getMessage());

        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }

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

    private static void clearTheFile(Map<String, String> propertiesMap) throws IOException {
        FileWriter fwOb = new FileWriter(propertiesMap.get("ClassesTextFilepath"), false);
        PrintWriter pwOb = new PrintWriter(fwOb, false);
        pwOb.flush();
        pwOb.close();
        fwOb.close();
    }

    public static List<ApexClassWrapper> getAllApexClasses(String partnerURL, String toolingURL, Cookie[] cookies) throws IOException, ConnectionException, com.sforce.ws.ConnectionException {

        String instanceUrl = null;
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("ACCESS_TOKEN")){
                accessToken = cookie.getValue();
            }
            if(cookie.getName().equals("INSTANCE_URL")){
                instanceUrl = cookie.getValue();
                instanceUrl = instanceUrl + partnerURL;
            }
        }


        ConnectorConfig config = new ConnectorConfig();
        config.setSessionId(accessToken);
        config.setServiceEndpoint(instanceUrl);

        try {
            partnerConnection = Connector.newConnection(config);
            metadataConnection = com.sforce.soap.metadata.Connector.newConnection(config);
        } catch (Exception e) {
            throw new com.sforce.ws.ConnectionException("Cannot connect to Org");
        }

        String apexClassBody = "SELECT Id, Name FROM APEXCLASS";


        List<com.sforce.soap.partner.sobject.SObject> sObjectList = queryRecords(apexClassBody, partnerConnection, null, true);

        ApexClassWrapper apexClassWrapper = null;

        List<ApexClassWrapper> apexClassWrappers = new ArrayList<>();
        for (com.sforce.soap.partner.sobject.SObject sObject : sObjectList) {
            Object name = sObject.getField("Name");
            Object id = sObject.getField("Id");

            apexClassWrapper = new ApexClassWrapper();
            apexClassWrapper.setName(name.toString());
            apexClassWrapper.setId(id.toString());
            apexClassWrappers.add(apexClassWrapper);
        }


        return apexClassWrappers;
    }

    public static Map<String, SymbolTable> generateSymbolTable(String partnerURL, String toolingURL, Cookie[] cookies) throws IOException, ConnectionException, com.sforce.ws.ConnectionException {



        String accessToken = null;
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("ACCESS_TOKEN")){
                accessToken = cookie.getValue();
            }
            if(cookie.getName().equals("INSTANCE_URL")){
                String instanceUrl = cookie.getValue();
                partnerURL = instanceUrl + partnerURL;
                toolingURL = instanceUrl + toolingURL;
            }
        }




        Map<String, SymbolTable> stringSymbolTableMap = new HashMap<>();

        ConnectorConfig toolConfig = new ConnectorConfig();
        ConnectorConfig config = new ConnectorConfig();

        toolConfig.setServiceEndpoint(toolingURL);
        toolConfig.setSessionId(accessToken);

        ToolingConnection toolingConnection = new ToolingConnection(toolConfig);

        config.setSessionId(accessToken);
        config.setServiceEndpoint(partnerURL);
        partnerConnection = Connector.newConnection(config);


        String apexClassBodytooling = "SELECT Id, Name, SymbolTable FROM APEXCLASS";
        List<ApexClass> sObjectListTooling = queryRecords(apexClassBodytooling, partnerConnection, toolingConnection, false);

        for (ApexClass apexClasses : sObjectListTooling) {
            SymbolTable symbolTable = apexClasses.getSymbolTable();
            String name = apexClasses.getName();
            setValues(name, stringSymbolTableMap, symbolTable);
        }

        System.out.println("Symbol table generated");

        return stringSymbolTableMap;
    }

    private static void setValues(String name, Map<String, SymbolTable> stringSymbolTableMap, SymbolTable symbolTable) {
        stringSymbolTableMap.put(name, symbolTable);

    }

    public static <T> List<T> queryRecords(String query, PartnerConnection partnerConnection, ToolingConnection toolingConnection, boolean usePartner)
            throws com.sforce.ws.ConnectionException {
        if (usePartner) {
            List<T> sObjectList = new ArrayList<>();
            QueryResult qResult;
            qResult = partnerConnection.query(query);
            boolean done = false;
            if (qResult.getSize() > 0) {
                System.out.println("Logged-in user can see a total of "
                        + qResult.getSize() + " contact records.");
                while (!done) {
                    com.sforce.soap.partner.sobject.SObject[] records = qResult.getRecords();
                    for (com.sforce.soap.partner.sobject.SObject record : records) {
                        sObjectList.add((T) record);
                    }

                    if (qResult.isDone()) {
                        done = true;
                    } else {
                        qResult = partnerConnection.queryMore(qResult.getQueryLocator());
                    }
                }
            } else {
                System.out.println("No records found.");
            }
            System.out.println("Query successfully executed.");

            return sObjectList;
        } else {
            List<T> sObjectList = new ArrayList<>();
            com.sforce.soap.tooling.QueryResult qResult = toolingConnection.query(query);
            boolean done = false;
            if (qResult.getSize() > 0) {
                System.out.println("Logged-in user can see a total of "
                        + qResult.getSize() + " contact records.");
                while (!done) {
                    SObject[] records = qResult.getRecords();
                    for (SObject record : records) {
                        sObjectList.add((T) record);
                    }
                    if (qResult.isDone()) {
                        done = true;
                    } else {
                        qResult = toolingConnection.queryMore(qResult.getQueryLocator());
                    }
                }
            } else {
                System.out.println("No records found.");
            }
            System.out.println("Query successfully executed.");

            return sObjectList;

        }
    }

    public List<String> returnSymbolTable() throws IOException, XMLStreamException, JAXBException {
        List<String> returnList = new ArrayList<>();

        JAXBContext jc = JAXBContext.newInstance(Completions.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("xml/ruleSet.xml");
        String ruleSetFilePath = "";
        if (resourceAsStream != null) {
            File file = stream2file(resourceAsStream);
            ruleSetFilePath = file.getPath();

        }
        InputStream stream = new FileInputStream(ruleSetFilePath);
        Completions unmarshal = (Completions) unmarshaller.unmarshal(stream);
        List<Type> type = unmarshal.getSystemNamespace().getType();
        for (Type eachType : type) {

            if(!eachType.getMethodTrie().isEmpty()){
                PatriciaTrie<ArrayList<AbstractCompletionProposalDisplayable>> methodTrie = eachType.getMethodTrie();
                for (String methodKey : methodTrie.keySet()) {
                    String buildSuggestions = "";
                    buildSuggestions += eachType.name+"+method+"+methodKey;
                    returnList.add(buildSuggestions);
                }
            }
        }

        return returnList;
    }
}
