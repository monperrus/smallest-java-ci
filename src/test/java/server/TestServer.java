package server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static server.ContinuousIntegrationServer.getJSON;
import static server.ContinuousIntegrationServer.getRepoURL;
import static server.ContinuousIntegrationServer.getStatusUrl;

import java.io.*;
import org.json.*;
import server.ContinuousIntegrationServer.*;
// Run Maven tests: mvn test

public class TestServer {

    @Test
    public void dummyTest() {
        int a = 1;
        assertEquals(a,1);
    }

    @Test
    public void test_getJSON(){
        //True case: expected JSON file (webhook response from github), should return a non-empty JSON object
        //False: file with JSON that is empty, should return an empty JSON object.
        JSONObject json = help_getJSON("src/test/java/server/test2.json");
        JSONObject json2 = help_getJSON("src/test/java/server/test3.json");

        assertEquals(json.isEmpty(), false);
        assertEquals(json2.isEmpty(), true);
    }


    @Test
    public void test_getRepoURL(){
      //True case: An expected JSON file is inputed, should produce a url
        JSONObject json_true = help_getJSON("src/test/java/server/testdata_getstatusurl_T.json");
        //JSONObject json_false = help_getJSON("src/test/java/server/testdata_getstatusurl_F.json");

        String statusRepoURL_true = getRepoURL(json_true);
        assertEquals(statusRepoURL_true, "master https://github.com/gzh0528/ci-server.git");


    }

    @Test
    public void test_getStatusUrl(){
      //True case: An expected JSON file is inputed, should produce a url
        JSONObject json_true = help_getJSON("src/test/java/server/testdata_getstatusurl_T.json");
        //JSONObject json_false = help_getJSON("src/test/java/server/testdata_getstatusurl_F.json");

        String statusUrl_true = getStatusUrl(json_true);
        assertEquals(statusUrl_true, "https://api.github.com/repos/gzh0528/ci-server/statuses/81527927fbc0945598f9c70f9e279eb6db7fed0e");
        //String statusUrl_false = getStatusUrl(json_false);

    }
   //@Test
   //public void test(){

   //}

   //@Test
   //public void test(){

   //}


   public static JSONObject help_getJSON(String filepath){
     //helper to get JSON object from a filename

     BufferedReader inputFromFile = null;

     {
         try {
             inputFromFile = new BufferedReader(new FileReader(filepath));

         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
     }

     JSONObject json = null;

     {
         try {
             assert inputFromFile != null;
             json = getJSON(inputFromFile);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }

     return json;

   }

    @Test
    public void dummyTest2() {
        ContinuousIntegrationServer server = new ContinuousIntegrationServer();
        int a = server.dummyFunction();
        assertEquals(1,a);
    }
}
