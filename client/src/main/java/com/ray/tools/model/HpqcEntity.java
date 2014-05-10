package com.ray.tools.model;

import com.ray.tools.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: leiding Date: 11/25/13 Time: 9:50 AM
 */
public abstract class HpqcEntity {
   private Map<TestProperty, List<String>> propertyStringMap =
         new HashMap<TestProperty, List<String>>();

   private Entity entity;


   public void setEntity(Entity entity) {
      this.entity = entity;
   }

   public List<String> getValue(TestProperty property) {
      if (!propertyStringMap.containsKey(property)) {
         propertyStringMap.put(property,
               entity.getFiledValue(property.getProperty()));
      }
      return propertyStringMap.get(property);
   }

   abstract public String getReturnedFields();
}
