package net.a2bsoft.buss.http;
import no.norrs.busbuddy.pub.api.BusBuddyAPIServiceController;
import no.norrs.busbuddy.pub.api.model.BusStopContainer;
import no.norrs.busbuddy.pub.api.model.Departure;
import no.norrs.busbuddy.pub.api.model.DepartureContainer;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by A2BSoft
 * User: martinmi
 * Date: 22.05.11
 * Time: 10.40
 * To change this template use File | Settings | File Templates.
 */
public class Realtime {

    private final String realtimeUrl = "http:";
    private static final String apiKey = "YYe483Yfxz8ck2Xc";

    public static String getRealtimeForBusstop(String id){
        BusBuddyAPIServiceController apiKontroller = new BusBuddyAPIServiceController(apiKey);
        try {
            DepartureContainer container = apiKontroller.getBusStopForecasts(100307);
            Departure departure = container.getDepartures().get(0);
            return departure.getRegisteredDepartureTime().toString();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
      return null;
    }

public static String sendQueryHTML(String query){
    	String url = "http://www.idi.ntnu.no:80/~tagore/cgi-bin/busstuc/busq.cgi";
    	HttpClient client = new DefaultHttpClient();
    	HttpPost post = new HttpPost(url);

        try {
			//query = URLEncoder.encode(query, "UTF-8)");
			query = URLEncoder.encode(query, "US-ASCII)");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	try{
    		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    		nameValuePairs.add(new BasicNameValuePair("lang", "nor"));
    		nameValuePairs.add(new BasicNameValuePair("quest", query));

    		//Set http header
    		post.setHeader("Content-type", "application/x-www-form-urlencoded");
    		post.setHeader("Accept", "text/plain");
    		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    		HttpResponse response;
    		response = client.execute(post);
    		return "string";

    	} catch (ClientProtocolException e){
    		e.printStackTrace();
    		return "Error CPE";
    	} catch (IOException e){
    		e.printStackTrace();
    		return e.getMessage();
    	}


    }


}
