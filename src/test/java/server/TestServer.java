package server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.*;
import org.json.*;

// Run Maven tests: mvn test

public class TestServer {
    
    @Test
    public void dummyTest() {
        int a = 1;
        assertEquals(a,1);
    }

    @Test
    public void TestGetJson(){
        //Test That it correctly converts to JSON
        String test_true = "[{\"test\":\"working\"}]";
        Reader inputString = new StringReader(test_true);
        JSONObject json_true = new JSONObject();
        BufferedReader reader = new BufferedReader(inputString);
        json_true.put("test","working");
        assertEquals(getJSON(reader),json_true);


        //Test That it giving the false 
        String test_false = "False : Not working";
        Reader inputString_2 = new StringReader(test_false);
        JSONObject json_false = new JSONObject();
        BufferedReader reader = new BufferedReader(inputString_2);
        json_true.put("false","Not working");
        assertNotEquals(getJSON(reader),json_true);
    }


    @Test
    public void test_getRepoURL(){
    //Test True    
    JSONObject json_true = new jsonObject();
    JSONObject git_url = new JSONObject();
    git_url.put("git_url","https://github.com/DD2480-Group-15/ci-server");
    json_true("repository", git_url);
    json_true("ref","/tree/issue/22");
    String true_return = "tree/issue/22" +""+"git://github.com/DD2480-Group-15/ci-server";
    assertEquals(getRepoURL(json_true), true_return);
    
    //Test False
    JSONObject json_false = new jsonObject();
    JSONObject git_url_1 = new JSONObject();
    git_url_1.put("git_url","/tree/issue/22");
    json_false("repository", git_url_1);
    json_false("ref","https://github.com/DD2480-Group-15/ci-server");
    String false_return = "tree/issue/22" +""+"git://github.com/DD2480-Group-15/ci-server";
    assertNotEquals(getRepoURL(json_false), false_return); 
    }

    



    @Test
    public void dummyTest2() {
        ContinuousIntegrationServer server = new ContinuousIntegrationServer();
        int a = server.dummyFunction();
        assertEquals(1,a);
    }
}
