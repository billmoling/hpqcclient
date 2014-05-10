package com.ray.tools;

import com.ray.tools.model.TestCase;
import com.ray.tools.model.TestInstance;
import com.ray.tools.model.TestProperty;
import com.ray.tools.model.TestResult;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import org.apache.log4j.Logger;

import javax.ws.rs.core.NewCookie;
import javax.xml.bind.JAXBException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * User: leiding Date: 9/29/13 Time: 4:06 PM
 */
public class HpqcClient {
   private static final String AUTHENTICATION_URL =
         "http://quality.eng.vmware.com:80/qcbin/rest/is-authenticated";
   private static final String LOGIN_URL =
         "http://quality.eng.vmware.com:80/qcbin/authentication-point/authenticate";
   private static final String TESTINSTANCE_URL =
         "http://quality.eng.vmware.com:80/qcbin/rest/domains/SOLUTIONS/projects/vFabric/test-instances";
   private static final String TESTS_URL =
         "http://quality.eng.vmware.com:80/qcbin/rest/domains/SOLUTIONS/projects/vFabric/tests";
   private static final String TESTS_FOLDERS =
         "http://quality.eng.vmware.com:80/qcbin/rest/domains/SOLUTIONS/projects/vFabric/test-folders";

   private static final String RUNS_URL =
         "http://quality.eng.vmware.com:80/qcbin/rest/domains/SOLUTIONS/projects/vFabric/runs";
   private static final String ENTITY_URL =
         "http://quality.eng.vmware.com:80/qcbin/rest/domains/SOLUTIONS/projects/vFabric/customization/entities/";
   private com.sun.jersey.api.client.Client client = null;
   private NewCookie cookie;
   private String user;
   private static Logger logger = Logger.getLogger(HpqcClient.class);
   private static HpqcClient instance = null;

   public static HpqcClient getInstance(String user, String password) {
      if (instance == null) {
         instance = new HpqcClient(user, password);
      }
      return instance;
   }

   private HpqcClient(String user, String password) {
      this.user = user;
      ClientConfig config = new DefaultClientConfig();
      client = com.sun.jersey.api.client.Client.create(config);

      client.addFilter(new HTTPBasicAuthFilter(user, password));

      int result =
            client.resource(AUTHENTICATION_URL).get(ClientResponse.class)
                  .getStatus();
      if (result == 401) {
         ClientResponse response =
               client.resource(LOGIN_URL).get(ClientResponse.class);
         cookie = response.getCookies().get(0);
      }
   }

   public void enableDebugModel() {
      client.addFilter(new LoggingFilter(System.out));
   }

   /**
    * Get all test instance within one test set
    * 
    * @param testSetId
    * @return
    * @throws javax.xml.bind.JAXBException
    */
   public List<TestInstance> getTestInstances(String testSetId, String filter)
         throws JAXBException {
      WebResource resource = client.resource(TESTINSTANCE_URL);
      StringBuilder query = new StringBuilder();

      resource =
            resource.queryParam("query", "{cycle-id[" + testSetId + "];"
                  + filter + "}");
      resource =
            resource.queryParam("fields",
                  new TestInstance().getReturnedFields());

      Entities entities =
            EntityMarshallingUtils.marshal(Entities.class,
                  resource.cookie(cookie).get(String.class));

      List<TestInstance> result = new ArrayList<TestInstance>();
      for (Entity entity : entities.getEntities()) {
         TestInstance testInstance = new TestInstance();
         testInstance.setEntity(entity);
         result.add(testInstance);
      }
      return result;
   }

   public int getTestInstancesNum(String testSetId, String... filter)
         throws JAXBException {
      WebResource resource = client.resource(TESTINSTANCE_URL);
      StringBuilder query = new StringBuilder();
      query.append("{cycle-id[" + testSetId + "];");

      for (String f : filter) {
         if (!f.isEmpty()) {
            query.append(f).append(";");
         }
      }
      query.append("}");

      resource = resource.queryParam("query", query.toString());

      logger.info("getTestInstancesNum with query " + query.toString());
      resource =
            resource.queryParam("fields",
                  new TestInstance().getReturnedFields());

      Entities entities =
            EntityMarshallingUtils.marshal(Entities.class,
                  resource.cookie(cookie).get(String.class));


      return entities.getTotalResults();
   }

   private String getTestInstanceId(int testSetId, String caseId)
         throws JAXBException {
      WebResource resource = client.resource(TESTINSTANCE_URL);

      resource =
            resource.queryParam("query", "{cycle-id[" + testSetId
                  + "];test-id[" + caseId + "]}");
      resource =
            resource.queryParam("fields",
                  TestProperty.INSTANCE_ID.getProperty());

      Entities entities =
            EntityMarshallingUtils.marshal(Entities.class,
                  resource.cookie(cookie).get(String.class));

      //Return null if not test instance found
      if (entities.getTotalResults() == 0) {
         logger.error("Cant find test instance for case " + caseId
               + " under test set " + testSetId);
         return null;
      }

      if (entities.getTotalResults() > 1) {
         logger.warn(String.format("Duplicated instances for test %s", caseId));
      }
      TestInstance testInstance = new TestInstance();
      testInstance.setEntity(entities.getEntities().get(0));
      return testInstance.getValue(TestProperty.INSTANCE_ID).get(0);

   }

   private String getRunId(int testSetId, String caseId) throws JAXBException {
      WebResource resource = client.resource(RUNS_URL);

      resource =
            resource.queryParam("query", "{cycle-id[" + testSetId
                  + "];test-id[" + caseId + "]}");
      //TODO: field filter
      Entities entities =
            EntityMarshallingUtils.marshal(Entities.class,
                  resource.cookie(cookie).get(String.class));
      if (entities.getEntities().size() == 0) {
         return null;
      }
      return getFiledValue(entities.getEntities().get(0), "id").get(0);
   }

   private void createRunId(int testSetId, String caseId, String testInstanceId) {
      WebResource resource = client.resource(RUNS_URL);

      SimpleDateFormat df = new SimpleDateFormat("MM-dd_HH-mm-ss");
      String time = df.format(new Date());
      String xml =
            "<Entity Type='run'><Fields><Field Name='execution-date'><Value></Value></Field><Field Name='state'><Value></Value></Field><Field Name='user-template-01'><Value>"
                  + "999"
                  + "</Value></Field><Field Name='id'><Value></Value></Field><Field Name='test-config-id'><Value></Value></Field><Field Name='name'><Value>Run_"
                  + time
                  + "</Value></Field><Field Name='vc-version-number'/><Field Name='os-build'><Value></Value></Field><Field Name='testcycl-id'><Value>"
                  + testInstanceId
                  + "</Value></Field><Field Name='cycle-id'><Value>"
                  + testSetId
                  + "</Value></Field><Field Name='cycle'><Value></Value></Field><Field Name='host'><Value></Value></Field><Field Name='assign-rcyc'><Value></Value></Field><Field Name='last-modified'><Value></Value></Field><Field Name='status'><Value>Running</Value></Field><Field Name='test-id'><Value>"
                  + caseId
                  + "</Value></Field><Field Name='subtype-id'><Value>hp.qc.run.MANUAL</Value></Field><Field Name='owner'><Value>"
                  + user
                  + "</Value></Field><Field Name='comments'><Value></Value></Field></Fields></Entity>";

      resource.cookie(cookie).type("application/xml").post(xml);
   }

   private void updateTestResult(String runId, String status) {
      WebResource resource = client.resource(RUNS_URL + "/" + runId);

      String entity =
            "<Entity Type=\"run\"><Fields>" + "<Field Name=\"" + "status"
                  + "\"><Value>" + status + "</Value></Field>"
                  + "</Fields></Entity>";
      resource.cookie(cookie).type("application/xml").put(entity);

   }

   public TestCase getTest(String caseId) throws JAXBException {
      WebResource resource = client.resource(TESTS_URL + "/" + caseId);
      TestCase testCase = new TestCase();
      resource = resource.queryParam("fields", testCase.getReturnedFields());
      Entity entity =
            com.ray.tools.EntityMarshallingUtils.marshal(Entity.class, resource
                  .cookie(cookie).get(String.class));
      testCase.setEntity(entity);
      return testCase;

   }

   /**
    * Test method
    * 
    * @param caseId
    * @return
    * @throws javax.xml.bind.JAXBException
    */
   public TestCase getTests(String caseId) throws JAXBException {
      //TODO: filter the unused field
      WebResource resource = client.resource(TESTS_URL);
      resource = resource.queryParam("query", "{creation-time[>=2013-10-07] }");
      resource = resource.queryParam("fields", "id,name,creation-time");


      String result = resource.cookie(cookie).get(String.class);
      return null;

   }

   public void getTestFolder() {
      WebResource resource = client.resource(TESTS_FOLDERS + "/1548");
      resource.cookie(cookie).get(String.class);

   }

   public void getEntity() {
      WebResource resource = client.resource(ENTITY_URL + "/test/fields");
      resource.cookie(cookie).get(String.class);
   }

   /**
    * 
    * @param testSetId
    * @param results
    *           [CaseId:CaseResult]
    * @throws javax.xml.bind.JAXBException
    */
   public void updateTestsResult(int testSetId, Map<String, TestResult> results)
         throws JAXBException {
      for (String caseId : results.keySet()) {
         String instanceId = getTestInstanceId(testSetId, caseId);
         if (instanceId == null) {
            logger.error(String.format("Case %s is not added test set %s",
                  caseId, testSetId));
            continue;
         }
         String runId = getRunId(testSetId, caseId);
         if (runId == null) {
            createRunId(testSetId, caseId, instanceId);
            runId = getRunId(testSetId, caseId);
            if (runId == null) {
               logger.error(String.format(
                     "Can't create run id for test case %s", caseId));
               continue;
            }
         }

         updateTestResult(runId, results.get(caseId).getValue());
         logger.info(String.format(
               "Update test case %s with status %s successfully ", caseId,
               results.get(caseId).getValue()));
      }

   }

   private List<String> getFiledValue(Entity entity, String filedName) {
      for (Entity.Fields.Field field : entity.getFields().field) {

         if (field.getName().equals(filedName)) {
            return field.getValue();
         }
      }
      return new ArrayList<String>();
   }
}
