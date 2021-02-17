package server;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.json.*;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
 */
public class ContinuousIntegrationServer<BASE64Encoder, BASE64Decoder> extends AbstractHandler
{

    private static final String token = "85a83fdab6ad97cf2a"+"HEaLOc7d67ff650bd40bc7993db";
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
                //write test_file
                try {
                    JsonWrite(JSON);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String URL = getRepoURL(JSON);
                String status_url = getStatusUrl(JSON);

                String cloneOK = cloneRepo(URL);

                String buildOK = "build not done";
                String notifyOK = "notification not sent";
                String [] build_res=new String[4];
                if(cloneOK.contains("Cloning OK")){
                    build_res = (buildAndTest("./cloned-repo",status_url));
                    buildOK=build_res[0];
                }

                if(buildOK.contains("Build OK")){
                    notifyOK = set_commit_status(token, status_url, 2, "Build OK");
                } else {
                    notifyOK = set_commit_status(token, status_url, 3, "Build Failed");
                }
                int id = JSOread(build_res[1],build_res[2],build_res[3]);
                JS_HTML(id,status_url);
                System.out.println("Request handled");

                if(notifyOK.contains("Notification sent successfully")){
                    System.out.println(notifyOK);
                }
            }
        }
    }

    public int dummyFunction() {
        //dummy function to start testing
        System.out.println("Calling dummyFunction");
        return 1;
    }

    /**
     * Creates a JSON object from the body of a http POST request from a GitHub webhook.
     * @param br contains the body of a http POST request
     * @return A JSON object containing the parameters from a GitHub webhook
     * @throws IOException
     */
    public static JSONObject getJSON(BufferedReader br) throws IOException {
        //reads the request and converts it to a JSON object
        //when adding webhook in GitHub, you have to chose a payload of application/json. Otherwise, this function will not work.
        String str;
        StringBuilder wholeStr = new StringBuilder();
        while ((str = br.readLine()) != null) {
            wholeStr.append(str);
        }
        br.close();

        String ss = wholeStr.toString();
        String dummy_json = "{}";
        //System.out.println(ss);
        try{
            return new JSONObject(ss);
        }catch (JSONException e){
            return new JSONObject(dummy_json);
        }
    }

    /**
     * Gets the GitHub repo url and recently pushed branch from the input json
     * and combine these to form a compatible string to use with the 'git clone' command.
     * @param json A JSON object containing the parameters from a GitHub webhook
     * @return A string with the recently pushed branch and the GitHub repo url.
     */
    public static String getRepoURL(JSONObject json){
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

    public static String getStatusUrl(JSONObject json){
        //this gets the complete url to the status of the latest commit in a given push from
        //a json object
        String complete_url;

        String commit_sha = json.getString("after");
        String url = json.getJSONObject("repository").getString("statuses_url");
        String replace = "{sha}";
        complete_url = url.replace(replace, commit_sha);
        return complete_url;
    }
    /**
     * Clones a repo into the directory ./cloned-repo
     * @param httpURL the http url of the repo
     * @return status of of how the cloning went
     */
    public static String cloneRepo(String httpURL){

        System.out.println("Cloning repository "+ httpURL);
        String cloneStatus;

        try {
            System.out.println(httpURL);
            Process P1=Runtime.getRuntime().exec("git clone -b " + httpURL + " ./cloned-repo");
            P1.waitFor();
            cloneStatus = "Cloning OK";
        } catch (IOException | InterruptedException e) {
            System.out.print("Could not clone repo.");
            cloneStatus = "Cloning Failed";
        }

        return cloneStatus;
    }
    /**
     * Build and test ./cloned-repo directory
     * if BUILD SUCCESS, deletes this directory.
     * @param path The path to the github repo that should be built and tested
     * @return "Build OK" if the build and test were successful and otherwise "Build and test Failed"
     */
    public static String[] buildAndTest(String path,String url) {
        //builds the specified repo path using Maven and returns the status of the build
        System.out.println("Running mvn package");
        File file=new File(path);
        String buildStatus = "Build and test Failed";
        String Date="";
        String log="";
        String sha="";
        String[] tt1=url.split("/");
        sha=tt1[tt1.length-1];
        String [] res=new String[4];
        try {
            ProcessBuilder p1 = new ProcessBuilder(new String[]{"mvn","package"});
            p1.redirectErrorStream(true);
            p1.directory(file);
            Process p = p1.start();
            set_commit_status(token, url, 1, "Build pending");
            p.waitFor();
            InputStream fis = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader fg = new BufferedReader(isr);
            String line = null;
            int tag=0;
            while ((line = fg.readLine()) != null) {
                {
                    buildStatus="Build OK";
                }
                System.out.println(line);
                log=log+line+"<br>";
                String temp=line;
                if((temp.contains("BUILD"))&&(temp.contains("SUCCESS")))
                {
                    buildStatus="Build OK";

                }
                if((temp.contains("Finished at")))
                {
                    String []tt=temp.split(" ");
                    Date=tt[tt.length-1];
                }
            }
            // Delete the repository.
            if(file.exists())
            {
                Process pp=Runtime.getRuntime().exec("rm -rf cloned-repo");
            }
        } catch (IOException | InterruptedException e) {
            System.out.print("Could not build.");
        }
        res[0]=buildStatus;
        res[1]=Date;
        res[2]=sha;
        res[3]=log;
        return res;
    }

    //Sends a notification to the webhook
    //And tell User dymnaically that Repo has
    //Been successfully build
    public static String set_commit_status(String token, String status_url, int state,
                                           String message) {
        //this function sets the status of a commit to one of the four possible values,
        // with the provided context and message
        token= token.replaceFirst("HEaLO", "");
        String[] statelist =  {"error", "pending", "success", "failure"};
        try {
            //this opens sends a http post request to github given the above parameters
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(status_url);
            httpPost.setHeader("Authorization", "token " + token);
            httpPost.setHeader("Content-type","application/json");
            httpPost.setHeader("Accept","application/vnd.github.v3+json");
            StringEntity params = new StringEntity(
                    "{\"state\": \""+statelist[state]
                            +"\", \"description\": \""+message
                            +"\", \"context\": \"Continuous Integration Server\"}", ContentType.APPLICATION_JSON);
            httpPost.setEntity(params);
            CloseableHttpResponse response = httpclient.execute(httpPost);
            int responseCode = response.getStatusLine().getStatusCode();
            httpclient.close();
            response.close();
            System.out.println(responseCode);
            //this returns a string based on the message recieved back from github
            if(responseCode == 201){
                return "Successful: "+ responseCode;
            }
            else {
                return "Unsuccessful: "+ responseCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Unsuccessful: Exception";
        }
    }

    //for test
    public static void JsonWrite(JSONObject obj) throws Exception{
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("./test_file.json"),"UTF-8");
        osw.write(obj.toString());
        osw.flush();
        osw.close();
    }

    /**
     * Update the HTML
     * @param id the length of current history
     * @param status_url the build url
     */
    public static void JS_HTML(int id,String status_url){
        String ids=String.valueOf(id-1);
        try
        {
            File file=new File("./History.html");
            Document document = Jsoup.parse(file, "utf-8");
            Element element = document.select("a").last();
            element.appendElement("a")
                    .attr("onclick","CreateHistory("+ids+")")
                    .attr("href","#")
                    .text(status_url).appendElement("br");
            element.appendElement("br");
            Path output = Path.of("History.html");
            Files.writeString(output, document.outerHtml());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Update the history JSON file.
     * @param Date
     * @param sha
     * @param log
     * @return return the length of current history
     */
    public static int JSOread(String Date,String sha,String log) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(
                "./data.json"));
        String s = null, ws = null;
        String js="";
        while ((s = br.readLine()) != null) {
            js=js+s;
        }
        br.close();
        BufferedWriter bw = new BufferedWriter(new FileWriter(
                "./data.json"));
        JSONArray features = new JSONArray(js);
        JSONObject properties = new JSONObject();
        properties.put("log", log);
        properties.put("Date", Date);
        properties.put("SHA", sha);
        features.put(properties);
        int id=features.length();
        ws = features.toString();
        bw.write(ws);
        bw.flush();
        bw.close();
        return id;
    }

    /**
     * Main method.
     * Used to start the CI server in command line.
     * @param args Not used
     */
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}
