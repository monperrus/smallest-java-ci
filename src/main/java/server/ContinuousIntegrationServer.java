package server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.*;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.json.*;

import java.nio.file.*;

import org.apache.commons.io.FileUtils;

/**
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
*/
public class ContinuousIntegrationServer extends AbstractHandler
{
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);

        String who = request.getHeader("user-agent");

        if(who.contains("GitHub-Hookshot")) {
          String what = request.getHeader("X-GitHub-Event");
          if(what.contains("push")) {
            BufferedReader br = request.getReader();
            //read the request
            JSONObject JSON = getJSON(br);

            //TODO: remove when JSON is working for everybody
            JSON.put("name","foo");
            System.out.println(JSON.get("name"));
            System.out.println("Pusher: " + JSON.get("pusher"));

            //  String thing = getJSON(br);
            String URL = "git@github.com:DD2480-Group-15/Assignment_2.git"; //getRepoURL(JSON);
            String cloneOK = cloneRepo(URL);

            String buildOK = "build not done";
            String notifyOK = "notification not sent";

            if(cloneOK.contains("Cloning OK")){
              buildOK = buildAndTest("path");
            }

            if(buildOK.contains("Build OK")){
              notifyOK = notify(buildOK);
            }

            System.out.println("Request handled");

            if(notifyOK.contains("Notification sent successfully")){
              System.out.println(notifyOK);
            }

        /*
        //OLD CODE THAT NEEDS TO BE INTEGRATED
          if(who.contains("GitHub-Hookshot")){ //this branch is called if the request is a webhook
              //this reads the body of the webhook as a string that is formatted as a json
              BufferedReader br = request.getReader();
              String str;
              StringBuilder wholeStr = new StringBuilder();
              while ((str = br.readLine()) != null) {
                  wholeStr.append(str);
              }
              String ss = wholeStr.toString();
              //this interprets the string as a json object so that it's parameters can be pulled
              com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(ss);
              //this extracts the branch in which the event occurred as lastOne
              String refs = jsonObject.get("ref").toString();
              String[] sss = refs.split("/");
              String lastOne = sss[sss.length - 1];
              //this extracts the url of the repository where the event occurred as git_url
              String git_url = jsonObject.getJSONObject("repository").get("git_url").toString();
              //Process p1 = Runtime.getRuntime().exec("cd C:\\Users\\Kalle\\git\\cloneplace");
              String git_url_fixed = git_url.replaceFirst("git", "https");
              //this clones the specified branch of the specified repository to the folder specified in folder_path
              String folder_path = " C:\\Users\\Kalle\\git\\cloneplace";
              Process p = Runtime.getRuntime().exec("git clone -b" + " " + lastOne + " " + git_url_fixed + folder_path);

              //this I don't quite know what it does
              InputStream fis = p.getInputStream();
              InputStreamReader isr = new InputStreamReader(fis);
              BufferedReader fg = new BufferedReader(isr);
              String line = null;
              //System.out.println("git clone -b" + " " + lastOne + " " + git_url_fixed + " ..\\new");
              while ((line = fg.readLine()) != null) {
                  System.out.println(line);
                  response.getWriter().println(line);
              }
          } */
        }
      }
    }

    public int dummyFunction() {
        //dummy function to start testing
        System.out.println("Calling dummyFunction");
        return 1;
    }

    public JSONObject getJSON(BufferedReader br) throws IOException {
      //reads the request and converts it to a JSON object
      //when adding webhook in GitHub, you have to chose a payload of application/json. Otherwise, this function will not work.
        String str;
        StringBuilder wholeStr = new StringBuilder();
        while ((str = br.readLine()) != null) {
            wholeStr.append(str);
        }
        br.close();

        String ss = wholeStr.toString();
        
        //System.out.println(ss);

        return new JSONObject(ss);
    }

    public String getRepoURL(JSONObject json){
        //gets the URL for repository to be cloned
        System.out.println("Getting repository URL");

        //this extracts the branch in which the event occurred as lastOne
        String ref = json.get("ref").toString();
        String[] splitref = ref.split("/");
        String branch = splitref[splitref.length - 1];
        //this extracts the url of the repository where the event occurred as git_url
        String git_url = json.getJSONObject("repository").get("git_url").toString();
        String git_url_fixed = git_url.replaceFirst("git", "https");
        String full_url;
        full_url = branch + " " + git_url_fixed;
        return full_url;
    }

    /**
     * Clones a repo into the directory ./cloned-repo
     * @param sshURL the ssh url of the repo
     * @return status of of how the cloning went
     */
    public String cloneRepo(String sshURL){
      System.out.println("Cloning repository "+ sshURL);
      String cloneStatus;

      checkExistingClonedDirectory();

      try {
        Runtime.getRuntime().exec("git clone " + sshURL + " ./cloned-repo");
        cloneStatus = "Cloning OK";
      } catch (IOException e) {
        System.out.print("Could not clone repo.");
        cloneStatus = "Cloning Failed";
      }

      return cloneStatus;
    }

    /**
     * Checks if the ./cloned-repo directory already exist and if so
     * it deletes this directory.
     */
    private void checkExistingClonedDirectory() {      
      if(Files.exists(Paths.get("./cloned-repo"))) {
        System.out.println("Directory exists!");
        
        try {
          // directory path
          File file  = new File("./cloned-repo");
      
          // delete directory
          FileUtils.deleteDirectory(file);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
      } 
    }

    public String buildAndTest(String path){
      //builds the specified repo path using Maven and returns the status of the build
      System.out.println("Running mvn package");
      String buildStatus = "Build and test ok";
      return buildStatus;
    }

    public String notify(String status){
      //sends notification of the build to the webhook
      System.out.println("Notifying GitHub of build status");
      String notificationStatus = "Notification sent successfully";
      return notificationStatus;
    }

    // public void write_payload_to_json(JSONObject input, String file_name) {
    //     //this function writes a JSONObject to a specified json-file
    //     try (FileWriter file = new FileWriter(file_name)) {
    //         com.alibaba.fastjson.JSONWriter WT = new JSONWriter(file);
    //         WT.writeObject(input);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}
