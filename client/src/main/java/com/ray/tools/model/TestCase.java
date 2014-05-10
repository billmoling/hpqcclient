package com.ray.tools.model;

/**
 * User: leiding Date: 11/25/13 Time: 10:34 AM
 */
public class TestCase extends HpqcEntity {
   @Override
   public String getReturnedFields() {
      StringBuilder sb = new StringBuilder();
      return sb.append(TestProperty.TEST_AUTOMATION_LEVEL.getProperty())
            .append(",").append(TestProperty.TEST_ID.getProperty()).toString();

   }
}
