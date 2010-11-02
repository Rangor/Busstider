/*
 * "Busstider" made by Martin Syvertsen
 * www.a2bsoft.net for changelog and info
 * 
 * Special Thanks to the query backend hosting and coding by Erlend Klakegg Bergheim http://klakegg.net/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package net.a2bsoft.buss.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class SendQuery {
	
	
	public static String sendQueryBusstuc(String query){
		
		String ans = "No answer";
		
		try {
			
    		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    		nameValuePairs.add(new BasicNameValuePair("lang", "nor"));
    		nameValuePairs.add(new BasicNameValuePair("quest", query));
    		
			
			Document doc = Jsoup.connect("http://www.idi.ntnu.no:80/~tagore/cgi-bin/busstuc/busq.cgi")
			  .data("lang","nor")
			  .data("quest", query)
			  .timeout(30000)
			  .header("Content-type", "application/x-www-form-urlencoded")
			  .header("Accept", "text/plain")
			  .post();
			
			 ans = doc.body().text();
		} catch (IOException e) {
			ans = e.toString();
			e.printStackTrace();
		}
		
		
		
		return ans;
	}
	
    public static String sendQuery(String query){
    	
    	try {
			query = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return e.getMessage();
		}
    	URL url = null;
		try {
			url = new URL("http://www.atb.no/xmlhttprequest.php?service=routeplannerOracle.getOracleAnswer&question="+ query);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
    	
    	String result = null;
		HttpParams my_httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(my_httpParams,30000);
        HttpConnectionParams.setSoTimeout(my_httpParams,30000);
    	HttpClient client = new DefaultHttpClient(my_httpParams);
    	HttpGet get = new HttpGet(url.toString());
    	HttpResponse resp;
    	
    	
    	try {
			resp = client.execute(get);
			InputStream data = resp.getEntity().getContent();
			result = new BufferedReader(new InputStreamReader(data)).readLine();
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return sendQueryBusstuc(query);
//			return e.getMessage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return sendQueryBusstuc(query);
//			return e.getMessage();
		}
    	
    	return result;
    }
	
}
