package com.ray.tools.stats;

import com.ray.tools.HpqcClient;
import com.ray.tools.model.TestResult;

import javax.xml.bind.JAXBException;


/**
 * User: leiding Date: 11/22/13 Time: 4:47 PM
 */
public class TestExecution {
   private long passedNum;
   private long failedNum;
   private long totalPlanNum = 350; //TOTO: configurable later
   private long totalDesignNum;
   private long automatedNum;
   private float testCoverage;
   private float passRate;

   public void generateSprintReport(HpqcClient client, String testSetId,
         String filter) throws JAXBException {
      totalDesignNum = client.getTestInstancesNum(testSetId, filter);
      passedNum =
            client.getTestInstancesNum(testSetId, filter, "status["
                  + TestResult.PASSED.getValue() + "]");
      failedNum =
            client.getTestInstancesNum(testSetId, filter, "status["
                  + TestResult.FAILED.getValue() + "]");
      automatedNum =
            client.getTestInstancesNum(testSetId, filter,
                  "test.user-template-03[Automated]");

      testCoverage = (float) (passedNum + failedNum) / (float) totalDesignNum;
      if ((passedNum + failedNum) != 0) {
         passRate = (float) passedNum / (float) (passedNum + failedNum);
      } else {
         passRate = 0;
      }


      System.out
            .println(String
                  .format(
                        "Total plan %s, Designed %s, Automated %s, TestCoverage %s, Pass Rate %s",
                        totalPlanNum, totalDesignNum, automatedNum,
                        testCoverage, passRate));

   }

   public void setTotalPlanNum(long totalPlanNum) {
      this.totalPlanNum = totalPlanNum;
   }
}
