package com.ray.tools.model;

/**
 * User: leiding Date: 11/25/13 Time: 10:43 AM
 */
public enum TestResult {
   NO_RUN("No Run"), PASSED("Passed"), FAILED("Failed"), NO_COMPLETE(
         "Not Completed"), RUNNING("Running"), BLOCKED("Blocked"), NA("N/A");

   private String result;

   public String getValue() {
      return result;
   }

   TestResult(String result) {
      this.result = result;
   }
}
