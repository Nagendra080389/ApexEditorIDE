package com.forceFilesEditor.pmd;

import net.sourceforge.pmd.*;
import net.sourceforge.pmd.cache.AnalysisCache;
import net.sourceforge.pmd.stat.Metric;
import net.sourceforge.pmd.util.ResourceLoader;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PmdRun {


    public static void main(String[] args) throws PMDException, IOException {

        PMDConfiguration pmdConfiguration = new PMDConfiguration();
        File file = new ClassPathResource("xml/ruleSet.xml").getFile();
        pmdConfiguration.setReportFormat("text");
        pmdConfiguration.setRuleSets(file.getAbsolutePath());
        pmdConfiguration.setThreads(4);
        SourceCodeProcessor sourceCodeProcessor = new SourceCodeProcessor(pmdConfiguration);
        InputStream stream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(pmdConfiguration, new ResourceLoader());
        RuleSets ruleSets = RulesetsFactoryUtils.getRuleSetsWithBenchmark(pmdConfiguration.getRuleSets(), ruleSetFactory);
        RuleContext ctx = new RuleContext();
        ctx.setSourceCodeFilename("something.cls");
        final AtomicInteger violations = new AtomicInteger(0);
        List<RuleViolation> ruleViolations = new ArrayList<>();
        ctx.getReport().addListener(new ThreadSafeReportListener() {
            @Override
            public void ruleViolationAdded(RuleViolation ruleViolation) {
                System.out.println(ruleViolation);
                ruleViolations.add(ruleViolation);
                violations.incrementAndGet();
            }

            @Override
            public void metricAdded(Metric metric) {
                System.out.println(metric);
            }
        });
        sourceCodeProcessor.processSourceCode(stream,ruleSets,ctx);

    }
}
