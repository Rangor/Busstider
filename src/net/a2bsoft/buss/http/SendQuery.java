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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class SendQuery {
	
	
	public static String sendQueryJsoup(String query){
		
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
	
}
