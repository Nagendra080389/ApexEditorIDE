package com.forceFilesEditor.config;

import net.sourceforge.pmd.*;
import net.sourceforge.pmd.util.ResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

@Configuration
public class PmdConf {



    @Bean
    public PMDConfiguration pmdConfiguration() {
        ClassPathResource res = new ClassPathResource("xml/ruleset.xml");
        File file = new File(res.getPath());
        PMDConfiguration pmdConfiguration = new PMDConfiguration();
        //pmdConfiguration.setInputPaths("/home/amarflybot/Desktop/");
        pmdConfiguration.setReportFormat("text");
        pmdConfiguration.setRuleSets("/home/amarflybot/Downloads/ruleset.xml");
        pmdConfiguration.setThreads(4);
        return pmdConfiguration;
    }

    @Bean
    public SourceCodeProcessor sourceCodeProcessor(PMDConfiguration pmdConfiguration) {
        return new SourceCodeProcessor(pmdConfiguration);
    }

    @Bean
    public RuleSetFactory ruleSetFactory(PMDConfiguration pmdConfiguration){
        return RulesetsFactoryUtils.getRulesetFactory(pmdConfiguration, new ResourceLoader());
    }

    @Bean
    public RuleSets ruleSets(PMDConfiguration pmdConfiguration, RuleSetFactory ruleSetFactory) {
        return RulesetsFactoryUtils.getRuleSetsWithBenchmark(pmdConfiguration.getRuleSets(), ruleSetFactory);
    }


}
