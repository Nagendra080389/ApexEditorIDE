package com.forceFilesEditor.algo;

import com.forceFilesEditor.model.ApexClassWrapper;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.tooling.*;
import com.sforce.soap.tooling.sobject.*;
import com.sforce.ws.ConnectorConfig;
import org.apache.commons.io.FileUtils;
import org.apache.coyote.http2.ConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import java.io.*;
import java.util.*;

public class MetadataLoginUtil {
    public static final String FILE_NAME = "C:\\JenkinsPOC\\Jenkins\\ConfigurationFile.txt";


    static PartnerConnection partnerConnection;
    static MetadataConnection metadataConnection;

    public static ApexClassWrapper getApexBody(String className, String partnerURL, String apexClassName) throws Exception {
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

            String apexClassBody = "SELECT Id,Body, Name FROM APEXCLASS Where Name = '"+apexClassName+"'";
            QueryResult query = partnerConnection.query(apexClassBody);

            Object body = query.getRecords()[0].getField("Body");
            Object name = query.getRecords()[0].getField("Name");
            Object id = query.getRecords()[0].getField("Id");

            ApexClassWrapper apexClassWrapper = new ApexClassWrapper();
            apexClassWrapper.setName(name.toString());
            apexClassWrapper.setBody(body.toString());
            apexClassWrapper.setId(id.toString());

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
        BufferedReader bufferedReader= null;
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

            List<String> lines = Arrays.asList(apexClassWrapper.getBody());
            File file = new File("C:\\Users\\nagesingh\\IdeaProjects\\ApexEditorIDE\\apexClass\\"+apexClassWrapper.getName()+".cls");
            for (String line : lines) {
                FileUtils.writeStringToFile(file, line);
            }
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
                                    if(lineNumberError.containsKey(deployMessage.getLineNumber())){
                                        List<String> strings = lineNumberError.get(deployMessage.getLineNumber());
                                        strings.add(deployMessage.getProblem());
                                    }else {
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


            if(!apexClassWrapper.isCompilationError()) {
                ProcessBuilder processBuilder = new ProcessBuilder(propertiesMap.get("PmdBatFile"));
                File log = new File(propertiesMap.get("apexClassReviewResult") + "\\" + apexClassWrapper.getName() + "_result" + ".txt");
                if (log.exists()) {
                    log.delete();
                }
                processBuilder.redirectErrorStream(true);
                processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
                Process process = processBuilder.start();
                process.waitFor();
                System.out.println("PMD ruleset Done");

                FileReader fileReader1 = new FileReader(log);
                bufferedReader = new BufferedReader(fileReader1);
                String sCurrentLine;

                while ((sCurrentLine = bufferedReader.readLine()) != null) {
                    if (sCurrentLine.contains(apexClassWrapper.getName())) {
                        String[] split = sCurrentLine.split("\\\\");
                        String lastElement = split[split.length - 1];
                        String[] lastTwoDetails = lastElement.split("\\t");
                        String[] nameAndNumber = lastTwoDetails[0].split(":");
                        Integer lineNumber = Integer.valueOf(nameAndNumber[1]);
                        String errorMessage = lastTwoDetails[1];

                        if (lineNumberError.containsKey(lineNumber)) {
                            List<String> strings = lineNumberError.get(lineNumber);
                            strings.add(errorMessage);
                        } else {
                            List<String> problemList = new ArrayList<>();
                            problemList.add(errorMessage);
                            lineNumberError.put(lineNumber, problemList);
                        }
                    }
                }
            }


            return apexClassWrapper;

        } catch (com.sforce.ws.ConnectionException e) {
            throw new com.sforce.ws.ConnectionException(e.getMessage());

        }finally {
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

    public static Map<String, SymbolTable> generateSymbolTable(String partnerURL, String toolingURL) throws IOException, ConnectionException, com.sforce.ws.ConnectionException{

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


        String apexClassBody = "SELECT Id, Name FROM APEXCLASS";
        List<String> idList  = new ArrayList<>();

        QueryResult className = partnerConnection.query(apexClassBody);
        for (com.sforce.soap.partner.sobject.SObject sObject : className.getRecords()) {
            String Id = (String) sObject.getField("Id");
            idList.add(Id);
        }

        String[] classArray = new String[idList.size()];
        classArray = idList.toArray(classArray);

        SObject[] apexClasses = toolingConnection.retrieve("SymbolTable, Id, Name", "ApexClass", classArray);

        for(SObject sObjects : apexClasses) {
            ApexClass apexClass = (ApexClass) sObjects;
            SymbolTable symbolTable = apexClass.getSymbolTable();
            if(symbolTable==null) { // No symbol table, then class likely is invalid
                continue;
            }

            idList.parallelStream().forEach(id -> setValues(id, apexClass, stringSymbolTableMap, symbolTable));
        }


        System.out.println("Symbol Table Generated");
        return stringSymbolTableMap;
    }

    private static void setValues(String id, ApexClass apexClass,Map<String, SymbolTable> stringSymbolTableMap,SymbolTable symbolTable){
        if(id.equals(apexClass.getId())){
            stringSymbolTableMap.put(apexClass.getName(), symbolTable);
        }
    }
}
