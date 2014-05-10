package com.ray.tools.model;

/**
 * User: leiding Date: 11/22/13 Time: 1:58 PM
 */
public class TestInstance extends HpqcEntity {
   @Override
   public String getReturnedFields() {
      //TODO:
      return new StringBuilder()
            .append(TestProperty.INSTANCE_CASE_ID.getProperty()).append(",")
            .append(TestProperty.INSTANCE_COMMENT.getProperty()).append(",")
            .append(TestProperty.INSTANCE_ID.getProperty()).append(",")
            .append(TestProperty.INSTANCE_STATUS.getProperty()).toString();
   }


}
