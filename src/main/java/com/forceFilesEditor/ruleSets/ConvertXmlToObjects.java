package com.forceFilesEditor.ruleSets;

import com.forceFilesEditor.algo.MetadataLoginUtil;
import com.forceFilesEditor.model.Completions;
import com.forceFilesEditor.model.Type;
import org.springframework.stereotype.Component;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class ConvertXmlToObjects {
    private final static QName _Ruleset_QNAME = new QName("http://pmd.sourceforge.net/ruleset/2.0.0", "ruleset");
    public RulesetType convertToObjects(String xmlFile) throws JAXBException, IOException {
        //1. We need to create JAXContext instance
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);

        //2. Use JAXBContext instance to create the Unmarshaller.
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        if(xmlFile != null){
            InputStream stream = new ByteArrayInputStream(xmlFile.getBytes(StandardCharsets.UTF_8));
            JAXBElement<RulesetType> unmarshalledObject =
                    (JAXBElement<RulesetType>)unmarshaller.unmarshal(stream);
            return unmarshalledObject.getValue();
        }


        //3. Use the Unmarshaller to unmarshal the XML document to get an instance of JAXBElement.

        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("xml/ruleSet.xml");
        String ruleSetFilePath = "";

        if (resourceAsStream != null) {
            File file = MetadataLoginUtil.stream2file(resourceAsStream);
            ruleSetFilePath = file.getPath();
        }

        InputStream stream = new FileInputStream(ruleSetFilePath);

        JAXBElement<RulesetType> unmarshalledObject =
                (JAXBElement<RulesetType>)unmarshaller.unmarshal(stream);
        return unmarshalledObject.getValue();
    }

    public String convertFromObjects(RulesetType rulesetType) throws JAXBException, IOException {

        String xmlString = "";
        try {
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            Marshaller m = context.createMarshaller();

            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // To format XML

            StringWriter sw = new StringWriter();
            JAXBElement<RulesetType> rootElement = new JAXBElement<RulesetType>(_Ruleset_QNAME, RulesetType.class, null, rulesetType);
            m.marshal(rootElement, sw);
            xmlString = sw.toString();

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return xmlString;
    }
}
