package com.forceFilesEditor.pmd;

import net.sourceforge.pmd.*;
import net.sourceforge.pmd.stat.Metric;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PmdReviewService {

    private final SourceCodeProcessor sourceCodeProcessor;

    private final RuleSets ruleSets;

    public PmdReviewService(SourceCodeProcessor sourceCodeProcessor, RuleSets ruleSets) {
        this.sourceCodeProcessor = sourceCodeProcessor;
        this.ruleSets = ruleSets;
    }

    public List<RuleViolation> review(String data, String fileName){

        InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        RuleContext ctx = new RuleContext();
        ctx.setSourceCodeFilename(fileName);
        List<RuleViolation> ruleViolations = new ArrayList<>();
        ctx.getReport().addListener(new ThreadSafeReportListener() {
            @Override
            public void ruleViolationAdded(RuleViolation ruleViolation) {
                ruleViolations.add(ruleViolation);
            }

            @Override
            public void metricAdded(Metric metric) {
            }
        });
        try {
            sourceCodeProcessor.processSourceCode(stream,ruleSets,ctx);
        } catch (PMDException e) {
            e.printStackTrace();
        }

        return ruleViolations;
    }

}
