package server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static server.ContinuousIntegrationServer.getJSON;

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
        BufferedReader inputString = null;
        BufferedReader inputString2 = null;

        {
            try {
                inputString = new BufferedReader(new FileReader("src/test/java/server/test2.json"));
                inputString2 = new BufferedReader(new FileReader("src/test/java/server/test3.json"));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        JSONObject json = null;
        JSONObject json2 = null;

        {
            try {
                assert inputString != null;
                assert inputString2 != null;
                json = getJSON(inputString);
                json2 = getJSON(inputString2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        assertEquals(json.isEmpty(), false);
        assertEquals(json2.isEmpty() ,true);
    }


    @Test
    public void test_getRepoURL(){
    //Test True    
    JSONObject json_true = new JSONObject();
    JSONObject git_url = new JSONObject();
    git_url.put("git_url","https://github.com/DD2480-Group-15/ci-server");
    json_true.put("repository", git_url);
    json_true.put("ref","/tree/issue/22");
    String true_return = "tree/issue/22" +""+"git://github.com/DD2480-Group-15/ci-server";
    //assertEquals(getRepoURL(json_true), true_return);
    
    //Test False
    JSONObject json_false = new JSONObject();
    JSONObject git_url_1 = new JSONObject();
    git_url_1.put("git_url","/tree/issue/22");
    json_false.put("repository", git_url_1);
    json_false.put("ref","https://github.com/DD2480-Group-15/ci-server");
    String false_return = "tree/issue/22" +""+"git://github.com/DD2480-Group-15/ci-server";
    //assertNotEquals(getRepoURL(json_false), false_return); 
    }

    @Test
    public void test_getStatusUrl(){

    }

   //@Test
   //public void test(){

   //}

   //@Test
   //public void test(){

   //}
    



    @Test
    public void dummyTest2() {
        ContinuousIntegrationServer server = new ContinuousIntegrationServer();
        int a = server.dummyFunction();
        assertEquals(1,a);
    }
}
