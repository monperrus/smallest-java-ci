import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;


/**
 * Sends Posts to Github with data that are given in JSON
 * @param t as token
 * @param u as URL
 * @param jsonStr as the dataformat in JSON
 */

public class Github {

    //Start initiate the Github connection
    private HttpsURLConnection set_up_url(u){
     connect = (HttpsURLConnection) u.openConnection();
     return connect;
    }

    //Set The connection to POST
    private void setPOST(u){
        u.setRequestMethod("POST");
    }

    private void setProperty(u){
        u.setRequestMethod("Content-Type,","application/json");
    }

    private void post_git(String jsonStr, String t, URL u){


    }
}
