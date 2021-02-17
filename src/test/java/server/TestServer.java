package server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
    public void TestGetJson(){
        //Test That it correctly converts to JSON
        String test_true = "{\"test\":\"working\"}";
        Reader inputString = new StringReader(test_true);
        JSONObject json_true = new JSONObject();

        BufferedReader reader = new BufferedReader(inputString);
        json_true.put("test","working");
        try{
        ContinuousIntegrationServer server = new ContinuousIntegrationServer();
        JSONObject true_result = server.getJSON(reader);
        assertEquals(true_result.toString(),json_true.toString());
        }
        catch(IOException e){
            e.printStackTrace();
        }

        //Test That it giving the false 
        String test_false = "{False : Not working}";
        Reader inputString_2 = new StringReader(test_false);
        JSONObject json_false = new JSONObject();
        BufferedReader reader_1 = new BufferedReader(inputString_2);
        json_true.put("False","Not working");
        try {
        ContinuousIntegrationServer server = new ContinuousIntegrationServer();
        JSONObject false_result = server.getJSON(reader_1);
        //assertNotEquals(false_result.toString(),json_false.toString());
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }


    @Test
    public void test_getRepoURL(){
    //Test True    
    JSONObject json_true = new JSONObject();
    JSONObject git_url = new JSONObject();
    git_url.put("git_url","https://github.com/DD2480-Group-15/ci-server");
    json_true.put("repository", git_url);
    json_true.put("ref","/tree/issue/22git");
    String true_return = "22git" +" "+"https://httpshub.com/DD2480-Group-15/ci-server";
    ContinuousIntegrationServer server = new ContinuousIntegrationServer();
    String result_1 = server.getRepoURL(json_true);
    System.out.println(result_1);
    assertEquals(result_1, true_return);

    //Test False
    JSONObject json_false = new JSONObject();
    JSONObject git_url_1 = new JSONObject();
    git_url_1.put("git_url","/tree/issue/22");
    json_false.put("repository", git_url_1);
    json_false.put("ref","https://github.com/DD2480-Group-15/ci-server");
    String false_return = "tree/issue/22" +""+"git://github.com/DD2480-Group-15/ci-server";
    String result_2 = server.getRepoURL(json_false);
    assertNotEquals(result_2,false_return); 
    }

    



    @Test
    public void dummyTest2() {
        ContinuousIntegrationServer server = new ContinuousIntegrationServer();
        int a = server.dummyFunction();
        assertEquals(1,a);
    }
}
