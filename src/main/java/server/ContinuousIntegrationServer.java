package server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonStreamParser;
import jdk.nashorn.internal.parser.JSONParser;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ajax.JSON;


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
        System.out.println(1);
        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        // 2nd compile the code
        System.out.println(request.getHeader("user-agent"));
        BufferedReader br = request.getReader();
        String str;
        StringBuilder wholeStr = new StringBuilder();
        while((str = br.readLine()) != null){
            wholeStr.append(str);
        }
        String ss = wholeStr.toString();
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(ss);

        String refs = jsonObject.get("ref").toString();
        String[] sss = refs.split("/");
        String lastOne = sss[sss.length-1];
        String git_url = jsonObject.getJSONObject("repository").get("git_url").toString();
        //Process p1 = Runtime.getRuntime().exec("cd C:\\Users\\Kalle\\git\\cloneplace");
        String git_url_fixed = git_url.replaceFirst("git", "https");
        Process p = Runtime.getRuntime().exec("git clone -b" +" "+lastOne+" "+ git_url_fixed+" C:\\Users\\Kalle\\git\\cloneplace");
        InputStream fis=p.getInputStream();

        InputStreamReader isr=new InputStreamReader(fis);

        BufferedReader fg=new BufferedReader(isr);
        String line=null;
        System.out.println("git clone -b" +" "+lastOne+" "+ git_url_fixed+" ..\\new");
        while((line=fg.readLine())!=null)
        {
            System.out.println(line);
            response.getWriter().println(line);
        }

        //System.out.println(jsonObject.get(lastOne));

        //System.out.println(jsonObject.getJSONObject("repository").get("git_url"));


    }
 
    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer()); 
        server.start();
        server.join();
    }
}
