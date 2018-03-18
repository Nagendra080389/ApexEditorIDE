package com.forceFilesEditor.algo;

import com.forceFilesEditor.model.ApexClassWrapper;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.coyote.http2.ConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.*;

public class MetadataLoginUtil {
    public static final String FILE_NAME = "C:\\JenkinsPOC\\Jenkins\\ConfigurationFileForIDE.txt";

    static PartnerConnection partnerConnection;
    static MetadataConnection metadataConnection;

    public static ApexClassWrapper getApexBody(String className, String partnerURL, String toolingURL) throws Exception {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        FileReader fileReader = new FileReader(FILE_NAME);
        createMapOfProperties(fileReader, propertiesMap);

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(propertiesMap.get("username"));
        config.setPassword(propertiesMap.get("password"));
        config.setAuthEndpoint(partnerURL);
        try {
            try {
                partnerConnection = Connector.newConnection(config);
                metadataConnection = com.sforce.soap.metadata.Connector.newConnection(config);
            } catch (Exception e) {
                throw new com.sforce.ws.ConnectionException("Cannot connect to Org");
            }

            String apexClassBody = "SELECT Id,Body,SystemModStamp, Name FROM APEXCLASS Where Name = '" + className + "'";
            QueryResult query = partnerConnection.query(apexClassBody);

            Object body = query.getRecords()[0].getField("Body");
            Object name = query.getRecords()[0].getField("Name");
            Object id = query.getRecords()[0].getField("Id");
            Object salesForceSystemModStamp = query.getRecords()[0].getField("SystemModstamp");

            ApexClassWrapper apexClassWrapper = new ApexClassWrapper();
            apexClassWrapper.setName(name.toString());
            apexClassWrapper.setBody(body.toString());
            apexClassWrapper.setId(id.toString());
            apexClassWrapper.setSalesForceSystemModStamp(DateUtils.parseDateStrictly(salesForceSystemModStamp.toString(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));

            return apexClassWrapper;

        } catch (com.sforce.ws.ConnectionException e) {
            throw new com.sforce.ws.ConnectionException(e.getMessage());
        }

    }

    public static ApexClassWrapper modifyApexBody(ApexClassWrapper apexClassWrapper, String partnerURL, String toolingURL) throws Exception {

        Map<String, String> propertiesMap = new HashMap<String, String>();
        FileReader fileReader = new FileReader(FILE_NAME);
        createMapOfProperties(fileReader, propertiesMap);

        ConnectorConfig toolConfig = new ConnectorConfig();
        toolConfig.setUsername(propertiesMap.get("username"));
        toolConfig.setPassword(propertiesMap.get("password"));
        toolConfig.setAuthEndpoint(toolingURL);


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

            ContainerAsyncRequest containerAsyncRequest = new ContainerAsyncRequest();
            containerAsyncRequest.setMetadataContainerId(containerId);
            containerAsyncRequest.setIsCheckOnly(true);

            con = new SObject[]{containerAsyncRequest};
            com.sforce.soap.tooling.SaveResult[] asyncResultMember = toolingConnection.create(con);

            String id = asyncResultMember[0].getId();

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
                                    if (lineNumberError.containsKey(deployMessage.getLineNumber())) {
                                        List<String> strings = lineNumberError.get(deployMessage.getLineNumber());
                                        strings.add(deployMessage.getProblem());
                                    } else {
                                        List<String> problemList = new ArrayList<>();
                                        problemList.add(deployMessage.getProblem());
                                        lineNumberError.put(deployMessage.getLineNumber(), problemList);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }

            apexClassWrapper.setLineNumberError(lineNumberError);

            if (!apexClassWrapper.isCompilationError()) {
                PMDConfiguration pmdConfiguration = new PMDConfiguration();
                File ruleSet = new ClassPathResource("xml/ruleSet.xml").getFile();
                pmdConfiguration.setReportFormat("text");
                pmdConfiguration.setRuleSets(ruleSet.getAbsolutePath());
                pmdConfiguration.setThreads(4);
                SourceCodeProcessor sourceCodeProcessor = new SourceCodeProcessor(pmdConfiguration);
                RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(pmdConfiguration, new ResourceLoader());
                RuleSets ruleSets = RulesetsFactoryUtils.getRuleSetsWithBenchmark(pmdConfiguration.getRuleSets(), ruleSetFactory);

                PmdReviewService pmdReviewService = new PmdReviewService(sourceCodeProcessor, ruleSets);
                List<RuleViolation> review = pmdReviewService.review(apexClassWrapper.getBody(), apexClassWrapper.getName() + ".cls");

                for (RuleViolation ruleViolation : review) {
                    if (lineNumberError.containsKey(ruleViolation.getBeginLine())) {
                        List<String> strings = lineNumberError.get(ruleViolation.getBeginLine());
                        strings.add(ruleViolation.getDescription());
                    } else {
                        List<String> problemList = new ArrayList<>();
                        problemList.add(ruleViolation.getDescription());
                        lineNumberError.put(ruleViolation.getBeginLine(), problemList);
                    }
                }
            }
            return apexClassWrapper;

        } catch (com.sforce.ws.ConnectionException e) {
            throw new com.sforce.ws.ConnectionException(e.getMessage());

        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }

    }


    public static ApexClassWrapper createFiles(String type, ApexClassWrapper apexClassWrapper) throws Exception {

        Map<String, String> propertiesMap = new HashMap<String, String>();
        FileReader fileReader = new FileReader(FILE_NAME);
        createMapOfProperties(fileReader, propertiesMap);

        ConnectorConfig toolConfig = new ConnectorConfig();
        toolConfig.setUsername(propertiesMap.get("username"));
        toolConfig.setPassword(propertiesMap.get("password"));
        toolConfig.setAuthEndpoint(propertiesMap.get("toolingURL"));


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
            apexClass1.setBody(" public class TestClassFromToolingAPI {\n" +
                    "                public string SayHello() {\n" +
                    "                    return 'Hello1';\n" +
                    "                }\n" +
                    "           }");

            con = new SObject[]{apexClass1};

            com.sforce.soap.tooling.SaveResult[] saveApex = toolingConnection.create(con);
            String apexId = saveApex[0].getId();


            /*ApexClassMember apexClass = new ApexClassMember();
            apexClass.setMetadataContainerId(containerId);
            apexClass.setBody(" public class TestClassFromToolingAPI {\n" +
                    "                public string SayHello() {\n" +
                    "                    return 'Hello1';\n" +
                    "                }\n" +
                    "           }");
            apexClass.setFullName("TestClassFromToolingAPI");
            apexClass.setContentEntityId(apexId);*/

            Map<Integer, List<String>> lineNumberError = new HashMap<>();


            //con = new SObject[]{apexClass};
            //com.sforce.soap.tooling.SaveResult[] saveMember = toolingConnection.create(con);

            ContainerAsyncRequest containerAsyncRequest = new ContainerAsyncRequest();
            containerAsyncRequest.setMetadataContainerId(containerId);
            containerAsyncRequest.setIsCheckOnly(false);

            con = new SObject[]{containerAsyncRequest};


            com.sforce.soap.tooling.SaveResult[] asyncResultMember = toolingConnection.create(con);

            String id = asyncResultMember[0].getId();


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
                                    if (lineNumberError.containsKey(deployMessage.getLineNumber())) {
                                        List<String> strings = lineNumberError.get(deployMessage.getLineNumber());
                                        strings.add(deployMessage.getProblem());
                                    } else {
                                        List<String> problemList = new ArrayList<>();
                                        problemList.add(deployMessage.getProblem());
                                        lineNumberError.put(deployMessage.getLineNumber(), problemList);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }


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

    public static List<ApexClassWrapper> getAllApexClasses(String partnerURL, String toolingURL) throws IOException, ConnectionException, com.sforce.ws.ConnectionException {

        Map<String, String> propertiesMap = new HashMap<String, String>();
        FileReader fileReader = new FileReader(FILE_NAME);
        createMapOfProperties(fileReader, propertiesMap);


        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(propertiesMap.get("username"));
        config.setPassword(propertiesMap.get("password"));
        config.setAuthEndpoint(partnerURL);

        try {
            partnerConnection = Connector.newConnection(config);
            metadataConnection = com.sforce.soap.metadata.Connector.newConnection(config);
        } catch (Exception e) {
            throw new com.sforce.ws.ConnectionException("Cannot connect to Org");
        }

        String apexClassBody = "SELECT Id, Name FROM APEXCLASS";


        QueryResult query = partnerConnection.query(apexClassBody);

        ApexClassWrapper apexClassWrapper = null;

        List<ApexClassWrapper> apexClassWrappers = new ArrayList<>();
        for (com.sforce.soap.partner.sobject.SObject sObject : query.getRecords()) {
            Object name = sObject.getField("Name");
            Object id = sObject.getField("Id");

            apexClassWrapper = new ApexClassWrapper();
            apexClassWrapper.setName(name.toString());
            apexClassWrapper.setId(id.toString());
            apexClassWrappers.add(apexClassWrapper);
        }


        return apexClassWrappers;
    }

    public static Map<String, SymbolTable> generateSymbolTable(String partnerURL, String toolingURL) throws IOException, ConnectionException, com.sforce.ws.ConnectionException {

        Map<String, String> propertiesMap = new HashMap<String, String>();
        FileReader fileReader = new FileReader(FILE_NAME);
        createMapOfProperties(fileReader, propertiesMap);

        Map<String, SymbolTable> stringSymbolTableMap = new HashMap<>();

        ConnectorConfig toolConfig = new ConnectorConfig();
        ConnectorConfig config = new ConnectorConfig();
        toolConfig.setUsername(propertiesMap.get("username"));
        toolConfig.setPassword(propertiesMap.get("password"));
        toolConfig.setAuthEndpoint(toolingURL);

        ToolingConnection toolingConnection = new ToolingConnection(toolConfig);

        config.setUsername(propertiesMap.get("username"));
        config.setPassword(propertiesMap.get("password"));
        config.setAuthEndpoint(partnerURL);
        partnerConnection = Connector.newConnection(config);


        String apexClassBody = "SELECT Id, Name FROM APEXCLASS WHERE Name = '" + "TestBusinessHelper'";
        List<String> idList = new ArrayList<>();

        QueryResult className = partnerConnection.query(apexClassBody);
        for (com.sforce.soap.partner.sobject.SObject sObject : className.getRecords()) {
            String Id = (String) sObject.getField("Id");
            idList.add(Id);
        }

        String[] classArray = new String[idList.size()];
        classArray = idList.toArray(classArray);

        SObject[] apexClasses = toolingConnection.retrieve("SymbolTable, Id, Name", "ApexClass", classArray);

        for (SObject sObjects : apexClasses) {
            ApexClass apexClass = (ApexClass) sObjects;
            SymbolTable symbolTable = apexClass.getSymbolTable();
            if (symbolTable == null) { // No symbol table, then class likely is invalid
                continue;
            }

            idList.parallelStream().forEach(id -> setValues(id, apexClass, stringSymbolTableMap, symbolTable));
        }


        return stringSymbolTableMap;
    }

    private static void setValues(String id, ApexClass apexClass, Map<String, SymbolTable> stringSymbolTableMap, SymbolTable symbolTable) {
        if (id.equals(apexClass.getId())) {
            stringSymbolTableMap.put(apexClass.getName(), symbolTable);
        }
    }
}