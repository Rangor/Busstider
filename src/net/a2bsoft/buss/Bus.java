/*
 * "Busstider" made by Martin Syvertsen
 * www.a2bsoft.net for changelog and info
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

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class Bus extends TabActivity {
	
	public static final String PREFS_NAME = "MyPrefsFile";
	public static final String TRACKER_UA = "UA-11054626-4";
	
	public static GoogleAnalyticsTracker tracker;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    Resources res = getResources(); 
	    TabHost tabHost = getTabHost();
	    TabHost.TabSpec spec;
	    Intent intent;
	    
		//Google Analytics tracker object
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(Bus.TRACKER_UA, this);
		tracker.trackPageView("/0_5_0_Code_7");
	    
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

	    
	    intent = new Intent().setClass(this, Semantic.class);
	    spec = tabHost.newTabSpec("semantic").setIndicator(res.getString(R.string.semantic_tab),
	                      res.getDrawable(R.drawable.ic_tab_semantic))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    
	    intent = new Intent().setClass(this, ToAndFrom.class);
	    spec = tabHost.newTabSpec("toandfrom").setIndicator(res.getString(R.string.toandfrom_tab),
	                      res.getDrawable(R.drawable.ic_tab_toandfrom))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, MapMode.class);
	    spec = tabHost.newTabSpec("mapmode").setIndicator(res.getString(R.string.mapmode_tab),
	                      res.getDrawable(R.drawable.ic_tab_mapmode))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    tabHost.setCurrentTab(settings.getInt("DEFAULT_GUI", 1));
	}
}
