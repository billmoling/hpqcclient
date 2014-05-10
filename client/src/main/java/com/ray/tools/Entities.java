package com.ray.tools;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: leiding Date: 11/7/13 Time: 11:04 AM
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Entities")
public class Entities {
   @XmlElement(name = "Entity", required = true)
   protected List<Entity> entities = new ArrayList<Entity>();
   @XmlAttribute(name = "TotalResults", required = true)
   protected int totalResults;

   public Entities(Entities entities) {
      this.totalResults = entities.totalResults;
      this.entities = entities.entities;
   }

   public Entities() {
   }

   public List<Entity> getEntities() {
      return entities;
   }

   public void setEntities(List<Entity> entities) {
      this.entities = entities;
   }

   public int getTotalResults() {
      return totalResults;
   }

   public void setTotalResults(int totalResults) {
      this.totalResults = totalResults;
   }
}
