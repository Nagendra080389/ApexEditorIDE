package com.forceFilesEditor.algo;

import com.forceFilesEditor.dao.RuleSetsDomainMongoRepository;
import com.forceFilesEditor.model.*;
import com.forceFilesEditor.pmd.PmdReviewService;
import com.google.gson.Gson;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.tooling.DeployDetails;
import com.sforce.soap.tooling.DeployMessage;
import com.sforce.soap.tooling.SymbolTable;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.soap.tooling.sobject.*;
import com.sforce.ws.ConnectorConfig;
import net.sourceforge.pmd.*;
import net.sourceforge.pmd.util.ResourceLoader;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.coyote.http2.ConnectionException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;
import wiremock.org.apache.commons.collections4.trie.PatriciaTrie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public class MetadataLoginUtil {

    static PartnerConnection partnerConnection;
    static MetadataConnection metadataConnection;
    org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MetadataLoginUtil.class);

    public static ApexClassWrapper getApexBody(String className, String partnerURL, String toolingURL, Cookie[]
            cookies) throws Exception {

        String instanceUrl = null;
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("ACCESS_TOKEN")) {
                accessToken = cookie.getValue();
            }
            if (cookie.getName().equals("INSTANCE_URL")) {
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

            String apexClassBody = "SELECT Id,Body,LastModifiedDate, Name FROM APEXCLASS Where Name = '" + className
                    + "'";
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

    public ApexClassWrapper modifyApexBody(ApexClassWrapper apexClassWrapper, String partnerURL, String toolingURL,
                                           Cookie[] cookies, boolean save, RuleSetsDomainMongoRepository
                                                   ruleSetsDomainMongoRepository) throws Exception {

        String instanceUrl = null;
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("ACCESS_TOKEN")) {
                accessToken = cookie.getValue();
            }
            if (cookie.getName().equals("INSTANCE_URL")) {
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

            if (save) {
                String apexClassBody = "SELECT Body FROM APEXCLASS Where Name = '" + apexClassWrapper.getName() + "'";
                if (partnerConnection == null) {
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

            System.out.println("after return cookies -> " + apexClassWrapper);

            ContainerAsyncRequest containerAsyncRequest = new ContainerAsyncRequest();
            containerAsyncRequest.setMetadataContainerId(containerId);
            containerAsyncRequest.setIsCheckOnly(!save);

            con = new SObject[]{containerAsyncRequest};
            com.sforce.soap.tooling.SaveResult[] asyncResultMember = toolingConnection.create(con);

            String id = asyncResultMember[0].getId();
            List<PMDStructure> pmdStructures = new ArrayList<>();

            while (true) {

                com.sforce.soap.tooling.QueryResult containerSyncRequestCompile = toolingConnection.query("SELECT Id," +
                        "State, DeployDetails, ErrorMsg FROM ContainerAsyncRequest where id = '" + id + "'");
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

            if (!save) {
                apexClassWrapper.setLineNumberError(lineNumberError);

                if (!apexClassWrapper.isCompilationError()) {
                    PMDConfiguration pmdConfiguration = new PMDConfiguration();
                    pmdConfiguration.setReportFormat("text");
                    RuleSetsDomain byorgId = ruleSetsDomainMongoRepository.findByorgId(apexClassWrapper.getOrgId());
                    String ruleSetXML = byorgId.getRuleSetXML();
                    LOGGER.info("ruleSetXML -> "+ruleSetXML);
                    InputStream stream = new ByteArrayInputStream(ruleSetXML.getBytes(StandardCharsets.UTF_8));
                    String ruleSetFilePath = "";
                    if (stream != null) {
                        File file = stream2file(stream);
                        ruleSetFilePath = file.getPath();
                    }
                    LOGGER.info("ruleSetFilePath -> "+ruleSetFilePath);
                    pmdConfiguration.setRuleSets(ruleSetFilePath);
                    pmdConfiguration.setThreads(4);
                    SourceCodeProcessor sourceCodeProcessor = new SourceCodeProcessor(pmdConfiguration);
                    RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(pmdConfiguration, new
                            ResourceLoader());
                    RuleSets ruleSets = RulesetsFactoryUtils.getRuleSetsWithBenchmark(pmdConfiguration.getRuleSets(),
                            ruleSetFactory);
                    LOGGER.info("pmdConfiguration.getRuleSets() -> "+pmdConfiguration.getRuleSets());
                    PmdReviewService pmdReviewService = new PmdReviewService(sourceCodeProcessor, ruleSets);
                    List<RuleViolation> review = pmdReviewService.review(apexClassWrapper.getBody(), apexClassWrapper
                            .getName() + ".cls");

                    for (RuleViolation ruleViolation : review) {
                        PMDStructure pmdStructure = new PMDStructure();
                        pmdStructure.setReviewFeedback(ruleViolation.getDescription());
                        pmdStructure.setLineNumber(ruleViolation.getBeginLine());
                        pmdStructure.setRuleName(ruleViolation.getRule().getName());
                        pmdStructure.setRuleUrl(ruleViolation.getRule().getExternalInfoUrl());
                        pmdStructure.setRulePriority(ruleViolation.getRule().getPriority().getPriority());
                        pmdStructures.add(pmdStructure);
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

    public static File stream2file(InputStream in) throws IOException {
        final File tempFile = File.createTempFile("ruleSet", ".xml");
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempFile;
    }


    public static ApexClassWrapper createFiles(String type, String apexClassName, String partnerURL, String toolingURL,
                                               Cookie[] cookies, RuleSetsDomainMongoRepository
                                                       ruleSetsDomainMongoRepository) throws Exception {

        String instanceUrl = null;
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("ACCESS_TOKEN")) {
                accessToken = cookie.getValue();
            }
            if (cookie.getName().equals("INSTANCE_URL")) {
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
            System.out.println(apexClassName);
            String[] split = apexClassName.split("\\+");

            ApexClass apexClass1 = new ApexClass();
            apexClass1.setBody("/** \n" +
                    "Created By -> " + split[2] + "\n" +
                    "Created Date -> " + new Date() + "\n" +
                    "Class Description -> " + split[1] + "\n" +
                    "**/\n" +
                    "public class " + split[0] + " {\n" +
                    "\n" +
                    "}");
            ApexClassWrapper apexClassWrapper = new ApexClassWrapper();
            apexClassWrapper.setBody(apexClass1.getBody());
            apexClassWrapper.setName(split[0]);

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
                com.sforce.soap.tooling.QueryResult containerSyncRequestCompile = toolingConnection.query("SELECT Id," +
                        "State, DeployDetails, ErrorMsg FROM ContainerAsyncRequest where id = '" + id + "'");
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

    public static List<ApexClassWrapper> getAllApexClasses(String partnerURL, String toolingURL, Cookie[] cookies,
                                                           HttpServletResponse response) throws IOException,
            ConnectionException, com.sforce.ws.ConnectionException {

        String instanceUrl = null;
        String accessToken = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("ACCESS_TOKEN")) {
                accessToken = cookie.getValue();
            }
            if (cookie.getName().equals("INSTANCE_URL")) {
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


        List<com.sforce.soap.partner.sobject.SObject> sObjectList = queryRecords(apexClassBody, partnerConnection,
                null, true, response);

        ApexClassWrapper apexClassWrapper = null;

        List<ApexClassWrapper> apexClassWrappers = new ArrayList<>();
        apexClassWrapper = new ApexClassWrapper();
        apexClassWrapper.setName("New Apex Class....");
        apexClassWrapper.setGroupName("Create New");
        apexClassWrappers.add(apexClassWrapper);
        for (com.sforce.soap.partner.sobject.SObject sObject : sObjectList) {
            Object name = sObject.getField("Name");
            Object id = sObject.getField("Id");

            apexClassWrapper = new ApexClassWrapper();
            apexClassWrapper.setName(name.toString());
            apexClassWrapper.setId(id.toString());
            apexClassWrapper.setGroupName("Edit Apex Class");
            apexClassWrappers.add(apexClassWrapper);
        }
        return apexClassWrappers;
    }

    public static Map<String, SymbolTable> generateSymbolTable(String partnerURL, String toolingURL, Cookie[] cookies,
                                                               OutputStream outputStream, Gson gson,
                                                               HttpServletResponse response) throws IOException,
            ConnectionException, com.sforce.ws.ConnectionException {

        Map<String, SymbolTable> stringSymbolTableMap = new HashMap<>();

        String accessToken = null;
        String instanceUrlForQuery = null;
        if (cookies == null) {
            return stringSymbolTableMap;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("ACCESS_TOKEN")) {
                accessToken = cookie.getValue();
            }
            if (cookie.getName().equals("INSTANCE_URL")) {
                String instanceUrl = cookie.getValue();
                instanceUrlForQuery = instanceUrl;
                partnerURL = instanceUrl + partnerURL;
                toolingURL = instanceUrl + toolingURL;
            }
        }

        String path = "/services/data/v41.0/tooling/query/?q=";


        String apexClassBodytooling = "Select+SymbolTable+From+ApexClass";
        HttpClient httpclient = new HttpClient();
        GetMethod getMethod = new GetMethod(instanceUrlForQuery + path + apexClassBodytooling);
        getMethod.setRequestHeader("Authorization", "Bearer " + accessToken);
        getMethod.setRequestHeader("Sforce-Query-Options", "batchSize=200");

        httpclient.executeMethod(getMethod);
        if (getMethod.getStatusCode() == HttpStatus.SC_OK) {
            try {
                boolean done = false;
                JSONObject jsonResponse = new JSONObject(new JSONTokener(new InputStreamReader(getMethod
                        .getResponseBodyAsStream())));
                if ((Integer) jsonResponse.get("size") > 0) {
                    while (!done) {
                        for (Object records : ((JSONArray) jsonResponse.get("records"))) {
                            ClassStructure classStructure = new ClassStructure();
                            Object symbolTable = ((JSONObject) records).get("SymbolTable");
                            if (!JSONObject.NULL.equals(symbolTable)) {
                                Object methods = ((JSONObject) symbolTable).get("methods");
                                Object className = ((JSONObject) symbolTable).get("name");
                                classStructure.setClassName(className.toString());
                                List<String> methodList = new ArrayList<>();
                                for (Object eachMethod : ((JSONArray) methods)) {
                                    Object name = ((JSONObject) eachMethod).get("name");
                                    methodList.add(name.toString());

                                }
                                classStructure.setMethodsNames(methodList);
                                outputStream.write(gson.toJson(classStructure).getBytes());
                                outputStream.flush();
                            }
                        }

                        if ((Boolean) jsonResponse.get("done")) {
                            done = true;
                        } else {
                            if (!JSONObject.NULL.equals(jsonResponse.get("nextRecordsUrl"))) {
                                getMethod = new GetMethod(instanceUrlForQuery + jsonResponse.get("nextRecordsUrl")
                                        .toString());
                                getMethod.setRequestHeader("Authorization", "Bearer " + accessToken);
                                getMethod.setRequestHeader("Sforce-Query-Options", "batchSize=200");
                                httpclient.executeMethod(getMethod);
                                jsonResponse = new JSONObject(new JSONTokener(new InputStreamReader(getMethod
                                        .getResponseBodyAsStream())));
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //List<ApexClass> sObjectListTooling = queryRecords(apexClassBodytooling, partnerConnection,
        // toolingConnection, false, response);

        /*for (ApexClass apexClasses : sObjectListTooling) {
            SymbolTable symbolTable = apexClasses.getSymbolTable();
            String name = apexClasses.getName();
            ClassStructure classStructure = new ClassStructure();
            setValues(name, stringSymbolTableMap, symbolTable, outputStream, gson, classStructure);
        }*/

        System.out.println("Symbol table generated");

        return stringSymbolTableMap;
    }

    public static <T> List<T> queryRecords(String query, PartnerConnection partnerConnection, ToolingConnection
            toolingConnection, boolean usePartner, HttpServletResponse response)
            throws com.sforce.ws.ConnectionException {
        if (usePartner) {
            List<T> sObjectList = new ArrayList<>();
            QueryResult qResult;
            partnerConnection.setQueryOptions(100);
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

    public List<String> returnSymbolTable(OutputStream outputStream, Gson gson) throws IOException,
            XMLStreamException, JAXBException {
        List<String> returnList = new ArrayList<>();
        JAXBContext jc = JAXBContext.newInstance(Completions.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("xml/completion.xml");
        String ruleSetFilePath = "";
        if (resourceAsStream != null) {
            File file = stream2file(resourceAsStream);
            ruleSetFilePath = file.getPath();

        }
        InputStream stream = new FileInputStream(ruleSetFilePath);
        Completions unmarshal = (Completions) unmarshaller.unmarshal(stream);
        List<Type> type = unmarshal.getSystemNamespace().getType();
        for (Type eachType : type) {
            List<String> methodsNames = new ArrayList<>();
            if (!eachType.getMethodTrie().isEmpty()) {
                PatriciaTrie<ArrayList<AbstractCompletionProposalDisplayable>> methodTrie = eachType.getMethodTrie();
                ClassStructure classStructure = new ClassStructure();
                for (String methodKey : methodTrie.keySet()) {
                    methodsNames.add(methodKey);
                    classStructure.setMethodsNames(methodsNames);
                }
                classStructure.setClassName(eachType.name);
                outputStream.write(gson.toJson(classStructure).getBytes());
                outputStream.flush();

            }
        }
        return returnList;
    }
}
