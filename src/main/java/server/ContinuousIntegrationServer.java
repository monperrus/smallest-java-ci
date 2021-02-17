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

        String who = request.getHeader("user-agent");
        if(who.contains("GitHub-Hookshot")) {
            String what = request.getHeader("X-GitHub-Event");
            if(what.contains("push")) {
                BufferedReader br = request.getReader();
                //read the request
                JSONObject JSON = getJSON(br);

                String URL = getRepoURL(JSON);
                String status_url = getStatusUrl(JSON);

                String cloneOK = cloneRepo(URL);

                String buildOK = "build not done";
                String notifyOK = "notification not sent";
                String [] build_res=new String[4];
                if(cloneOK.contains("Cloning OK")){
                    build_res = (buildAndTest("./cloned-repo", status_url));
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
        else if(target.equals("/build-history.html")) {
            System.out.println("Accessing build history log");
            PrintWriter out = response.getWriter();

            FileReader fr = new FileReader("build-history.html");
            StringBuilder sb = new StringBuilder();

            int k;
            char ch;

            while((k = fr.read()) != -1 ) {
                ch = (char) k;
                out.append(ch);
            }

            fr.close();
            // Sends the http response with the contents of the build-history.html file
            out.println(sb.toString());
            out.close();
        }
        else {
            System.out.println(target);
        }
    }

    /**
     * Creates a JSON object from the body of a http POST request from a GitHub webhook.
     * @param br contains the body of a http POST request
     * @return A JSON object containing the parameters from a GitHub webhook
     * @throws IOException
     */
    public static JSONObject getJSON(BufferedReader br) throws IOException {
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
        System.out.println("Getting repository URL");

        //this extracts the branch in which the event occurred as lastOne
        String ref = json.get("ref").toString();
        String[] splitref = ref.split("/");
        String branch = splitref[splitref.length - 1];
        //extracts the url of the repository where the event occurred as git_url
        String git_url = json.getJSONObject("repository").get("git_url").toString();
        String git_url_fixed = git_url.replaceFirst("git", "https");
        String full_url;
        full_url = branch + " " + git_url_fixed;
        return full_url;
    }

    /**
     * Gets url to the status of the latest commit in a given push
     * @param json the JSON object of the commit
     * @return url of the status
     */
    public static String getStatusUrl(JSONObject json){
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
     * Builds and tests a specified repository
     * if BUILD SUCCESS, the directory is deleted
     * @param path The path to the cloned repo that should be built and tested
     * @param url Url to commit status
     * @return Array of information about the build status
     */
    public static String[] buildAndTest(String path,String url) {
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

    /**
     * Sets status of a commit to one of the four possible values (error/pending/success/failure) with provided context and message
     * @param token a token to access the repository
     * @param status_url status url of the commit
     * @param state state of the
     * @param message the message to send to the commit
     * @return "Successful 201" if status set was successful, "Unsucessful [responsecode]" if not
     */
    public static String set_commit_status(String token, String status_url, int state,
                                           String message) {
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
