package com.forceFilesEditor.ruleSets;

import com.forceFilesEditor.algo.MetadataLoginUtil;
import com.forceFilesEditor.model.Completions;
import com.forceFilesEditor.model.Type;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ConvertXmlToObjects {
    public RulesetType convert(String[] args) throws JAXBException, IOException {
        //1. We need to create JAXContext instance
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);

        //2. Use JAXBContext instance to create the Unmarshaller.
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

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
}
