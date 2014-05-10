package com.ray.tools;

import com.ray.tools.model.TestResult;
import com.ray.tools.stats.TestExecution;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.util.HashMap;
import java.util.Map;


/**
 * User: leiding Date: 11/8/13 Time: 11:10 AM
 */
public class HpqcTool {

   private Map<String, TestResult> getTestResultFromCli(String cases,
         TestResult result) {
      Map<String, TestResult> caseResult = new HashMap<String, TestResult>();
      if (cases.contains("-")) {
         long start = Long.valueOf(cases.split("-")[0]);
         long end = Long.valueOf(cases.split("-")[1]);
         while (start <= end) {
            caseResult.put(String.valueOf(start), result);
            start++;
         }
      } else {
         String[] pcs = cases.split(",");
         for (int i = 0; i < pcs.length; i++) {
            caseResult.put(pcs[i], result);
         }
      }
      return caseResult;
   }

   public static void main(String[] args) throws Exception {
      OptionParser parser = new OptionParser();
      try {
         OptionSpec<String> user =
               parser.accepts("user", "HPQC user name").withRequiredArg()
                     .required();
         OptionSpec<String> password =
               parser.accepts("pwd", "HPQC user password").withRequiredArg()
                     .required();
         //Test execution generate
         OptionSpec<Void> report =
               parser.accepts("report", "update test result under test set");

         //TODO:Add filter parameter check
         OptionSpec<String> filter =
               parser.accepts("filter",
                     "Test instance filter:test.creation-time[>=2013-10-07]")
                     .withOptionalArg().defaultsTo("");

         //Test result update
         OptionSpec<Void> update =
               parser.accepts("update", "update test result under test set");
         OptionSpec<Integer> testSet =
               parser.accepts("testset", "Specify test set id")
                     .requiredIf("update", "report").withRequiredArg()
                     .ofType(Integer.class);
         OptionSpec<Integer> passed =
               parser.accepts("passed", "Specify passed test case id 1,2,3")
                     .withRequiredArg().ofType(Integer.class)
                     .withValuesSeparatedBy(",");
         OptionSpec<Integer> passed2 =
               parser.accepts("passed2", "Specify passed test case id 1-10")
                     .withRequiredArg().ofType(Integer.class)
                     .withValuesSeparatedBy("-");

         OptionSpec<Integer> failed =
               parser.accepts("failed", "Specify failed test case id 1,2,3")
                     .withRequiredArg().ofType(Integer.class)
                     .withValuesSeparatedBy(",");
         OptionSpec<Integer> failed2 =
               parser.accepts("failed2", "Specify failed test case id 1-10")
                     .withRequiredArg().ofType(Integer.class)
                     .withValuesSeparatedBy("-");


         OptionSpec<Void> debug =
               parser.accepts("debug",
                     "Make hpqc client display rest request and response");

         OptionSet options = parser.parse(args);

         HpqcClient client =
               HpqcClient.getInstance(options.valueOf(user),
                     options.valueOf(password));
         if (options.has(debug)) {
            client.enableDebugModel();
         }

         if (options.has(update)) {
            int testSetId = options.valueOf(testSet);
            Map<String, TestResult> caseResult =
                  new HashMap<String, TestResult>();
            if (options.has(passed)) {
               for (Integer id : options.valuesOf(passed)) {
                  caseResult.put(id.toString(), TestResult.PASSED);
               }
            }
            if (options.has(passed2)) {
               int start = options.valuesOf(passed2).get(0);
               int end = options.valuesOf(passed2).get(1);
               while (start <= end) {
                  caseResult.put(String.valueOf(start), TestResult.PASSED);
                  start++;
               }
            }

            if (options.has(failed)) {
               for (Integer id : options.valuesOf(failed)) {
                  caseResult.put(id.toString(), TestResult.FAILED);
               }
            }
            if (options.has(failed2)) {
               int start = options.valuesOf(failed2).get(0);
               int end = options.valuesOf(failed2).get(1);
               while (start <= end) {
                  caseResult.put(String.valueOf(start), TestResult.FAILED);
                  start++;
               }
            }
            client.updateTestsResult(testSetId, caseResult);
         }
         if (options.has(report)) {
            TestExecution tx = new TestExecution();
            tx.generateSprintReport(client,
                  options.valueOf(testSet).toString(), options.valueOf(filter));
         }

      } catch (OptionException e) {
         System.out.println(e.getMessage());
         parser.printHelpOn(System.out);
      }

   }

}
