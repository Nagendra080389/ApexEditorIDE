package com.forceFilesEditor.pmd;

import net.sourceforge.pmd.*;
import net.sourceforge.pmd.stat.Metric;
import net.sourceforge.pmd.util.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PmdRun {

    private static String classString = "/** * Created by nagesingh on 12/1/2017. */@IsTestprivate class TST_AHAfterUpdateTriggerHandler {    static testMethod void testBehaviorForFR() {        User user = TestBusinessHelper.createAdminuser(true, 'FR', 'EUR');        System.runAs(user) {            Disable_Triggers__c disable_triggers = SYT_TestUtility_Class.InsertDisableTriggerCS('Profile', SYT_Constant.PR_SYSTEM_ADMINISTRATOR,                    'Account,Account_hierarchy__c');            disable_triggers.Validation_rules__c = true;            insert disable_triggers;            //Accounts            Account acc1 = TestBusinessHelper.createAccount();            acc1.Country__c = 'RU';            acc1.Account_Level_FR__c = 'Purchasing / Regional';            update acc1;            Account acc2 = TestBusinessHelper.createAccount();            acc2.Country__c = 'RU';            acc2.Account_Level_FR__c = 'Distribution / Regional';            Update acc2;            Account acc3 = TestBusinessHelper.createAccount();            acc3.Country__c = 'RU';            acc3.Account_Level_FR__c = 'Distribution / Local';            Update acc3;            Account acc4 = TestBusinessHelper.createAccount();            acc4.Country__c = 'RU';            acc4.Account_Level_FR__c = 'Warehouse';            Update acc4;            //Account Hierarchies            Account_hierarchy__c ah1 = testBusinessHelper.CreateAccount_Hierarchy(acc1.Id);            ah1.Node_Type__c = 'National Purchasing Group';            ah1.Country__c = 'RU';            update ah1;            Account_hierarchy__c ah2 = testBusinessHelper.CreateAccount_Hierarchy(acc2.Id);            ah2.ParentId__c = ah1.Id;            ah2.Hierarchy_Parent__c = ah1.Id;            ah2.Node_Type__c = 'Local Purchasing Group';            ah2.country__c = 'RU';            update ah2;            Account_hierarchy__c ah3 = testBusinessHelper.CreateAccount_Hierarchy(acc3.Id);            ah3.ParentId__c = ah2.Id;            ah3.Hierarchy_Parent__c = ah1.Id;            ah3.Node_Type__c = 'Regional Distributor';            ah3.country__c = 'RU';            update ah3;            Account_hierarchy__c ah4 = testBusinessHelper.CreateAccount_Hierarchy(acc4.Id);            ah4.ParentId__c = ah3.Id;            ah4.Hierarchy_Parent__c = ah1.Id;            ah4.Node_Type__c = 'Local Distributor';            ah4.country__c = 'RU';            update ah4;            Map<Id, Account_Hierarchy__c> accountHierarchyMap = new Map<Id, Account_Hierarchy__c>([SELECT Id FROM Account_Hierarchy__c]);            Test.startTest();            AHAfterUpdateTriggerHandler.processLevelSyncOnAccounts(accountHierarchyMap, accountHierarchyMap);            Test.stopTest();        }    }}";

    public static void main(String[] args) throws PMDException {

        PMDConfiguration pmdConfiguration = new PMDConfiguration();
        //pmdConfiguration.setInputPaths("/home/amarflybot/Desktop/");
        pmdConfiguration.setReportFormat("text");
        pmdConfiguration.setRuleSets("/home/amarflybot/Downloads/ruleset.xml");
        pmdConfiguration.setThreads(4);
        //int doPMD = PMD.doPMD(pmdConfiguration);
        //System.out.println(doPMD);
        SourceCodeProcessor sourceCodeProcessor = new SourceCodeProcessor(pmdConfiguration);
        InputStream stream = new ByteArrayInputStream(classString.getBytes(StandardCharsets.UTF_8));
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
