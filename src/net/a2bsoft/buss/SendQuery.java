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


package net.a2bsoft.buss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class SendQuery {
	
    public static String sendQueryJson(String query) throws JSONException{

    	
    	try {
			query = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
			return e2.getMessage();
		}
    	URL url = null;
		try {
			url = new URL("http://puma.hoo9.com/bussorakel.php?question="+ query);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
    	
    	String result = null;
    	String answer = "no response";
    	
    	HttpClient client = new DefaultHttpClient();
    	HttpGet get = new HttpGet(url.toString());
    	HttpResponse resp;
    	
    	
    	try {
			resp = client.execute(get);
			InputStream data = resp.getEntity().getContent();
			result = new BufferedReader(new InputStreamReader(data)).readLine();
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}

		
		JSONObject jsonObj = new JSONObject(result);
		try {
			answer = jsonObj.getString("answer");
		} catch (JSONException e) {
			e.printStackTrace();
			return e.getMessage();
		}
    	
    	return answer;
    }
	
	

}
