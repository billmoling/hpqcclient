package com.ray.tools.model;

/**
 * User: leiding Date: 11/25/13 Time: 10:32 AM
 */
public enum TestProperty {

   INSTANCE_ID("id"), INSTANCE_CASE_ID("test-id"), INSTANCE_COMMENT(
         "user-template-03"), INSTANCE_STATUS("status"), TEST_AUTOMATION_LEVEL(
         "user-template-03"), TEST_ID("id");
   String property;

   private TestProperty(String property) {
      this.property = property;
   }

   public String getProperty() {
      return property;
   }
}
