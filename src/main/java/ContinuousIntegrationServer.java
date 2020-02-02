import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        cloneTheProject();
        // 2nd compile the code
        compile();
        runTests();
        notifyUser();

        response.getWriter().println("CI job done");
        response.getWriter().println(baseRequest);
        response.getWriter().println(request);
        response.getWriter().println(response);


    }
 
    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8083);
        server.setHandler(new ContinuousIntegrationServer()); 
        server.start();
        server.join();
    }

    private static void cloneTheProject(){

    }

    private static void compile(){

    }

    private static void runTests(){

    }

    private static void notifyUser(){

    }
}
