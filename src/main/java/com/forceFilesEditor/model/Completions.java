
package com.forceFilesEditor.model;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import wiremock.org.apache.commons.collections4.trie.PatriciaTrie;


@XmlRootElement
public class Completions {

    // Mirror what we have at https://www.salesforce.com/us/developer/docs/apexcode/Content/apex_reference.htm
    // Since the completions?type=apex seems to return more than we would like
    @VisibleForTesting
    public static final List<String> kosherNamespace = ImmutableList.of(
            "ApexPages",
            "Approval",
            "Canvas",
            "ChatterAnswers",
            "ConnectApi",
            "Database",
            "Datacloud",
            "Dom",
            "Flow",
            "KbManagement",
            "Messaging",
            "Process",
            "QuickAction",
            "Reports",
            "Schema",
            "Site",
            "Support",
            "System",
            "UserProvisioning"
    );

    @XmlElement(name = "namespace", required = true)
    public List<Namespace> namespace;

    public PatriciaTrie<AbstractCompletionProposalDisplayable> namespaceTrie;

    public Namespace getSystemNamespace() {
        return (Namespace) namespaceTrie.get("system");
    }

    void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {
        namespaceTrie = new PatriciaTrie<>();
    }

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        for (Namespace ns : namespace) {
            String namespace = ns.name;
            if (kosherNamespace.contains(namespace)) {
                namespaceTrie.put(namespace.toLowerCase(), ns);
            }
        }
    }
}
