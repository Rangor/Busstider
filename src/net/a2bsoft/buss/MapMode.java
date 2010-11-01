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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.a2bsoft.buss.db.QueryDb;
import net.a2bsoft.buss.http.SendQuery;
import net.a2bsoft.buss.parser.BusStop;
import net.a2bsoft.buss.parser.SaxFeedParser;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;


public class MapMode extends MapActivity implements LocationListener {

	private MapView mapView;
	private MapController mapCont;
	private GeoPoint gPoint;
	public static double mylat, mylng;
	private ProgressDialog dialog;
	private QueryDb mDbHelper;
	private List<BusStop> bussStopList;
	public static OverlayItem fromItem;
	public static OverlayItem toItem;
	public static String fromString;
	public static String toString;
	
	public static String answerString;
	
	private Cursor stopsCursor;
 	List<Overlay> mapOverlays;
 	private Drawable busstopDrawable;
 	private Bitmap myLocationDrawable;
	
	private List<BusStop> stops;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_layout);

		mapView = (MapView)findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		
		//Fetch settings
		SharedPreferences settings = getSharedPreferences(Bus.PREFS_NAME, 0);
		
		//Google Analytics tracker object
        Bus.tracker.trackPageView("/Mapmode");
		
		String coordinates[] = {"63.41738283634186", "10.407153367996216"};
		double lat = Double.parseDouble(coordinates[0]);
		double lng = Double.parseDouble(coordinates[1]);
        
		mapCont = mapView.getController();

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 50.0f, this);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 50.0f, this);

		mapOverlays = mapView.getOverlays();
		busstopDrawable = this.getResources().getDrawable(R.drawable.busstop_blue);
		myLocationDrawable = BitmapFactory.decodeResource(getResources(), R.drawable.red_dot);

		mapCont.animateTo(new GeoPoint(
				(int) (lat * 1E6), 
				(int) (lng * 1E6)));
		mapCont.setZoom(14);
		
		mapView.invalidate();
		
        mDbHelper = new QueryDb(this);
        mDbHelper.open();
         	
    	stopsCursor = mDbHelper.fetchAllBusstops();
        startManagingCursor(stopsCursor);	
        
        //Create a list of BusStops from the DB
        if(stopsCursor.getCount() > 100){
        	populateBusstopList();
        }else{
        	
      	  AlertDialog.Builder builder = new AlertDialog.Builder(MapMode.this);
    	  
    	  builder.setMessage("Kartmodus krever nedlasting av posisjonsdata til bussholdeplasser, vil du gjøre dette nå?")
    	         .setCancelable(false)
    	         .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
    	             public void onClick(DialogInterface dialog, int id) {
    	             	new populateBusstopsDbTask().execute("String");
    	             }
    	         })
    	         .setNegativeButton("Nei", new DialogInterface.OnClickListener() {
    	             public void onClick(DialogInterface dialog, int id) {
    	                  dialog.cancel();
    	             }
    	         });
    	  
    	  AlertDialog alert = builder.create();
    	  
    	  alert.show();
        	
        }
        if(!settings.getBoolean("HAS_READ_INSTRUCTIONS", false)){
      	  AlertDialog.Builder secondbuilder = new AlertDialog.Builder(MapMode.this);
    	  secondbuilder.setMessage("Vent på at holdeplassene skal tegnes (tar litt tid). Trykk på holdeplassen du vil reise fra." +
    	  		" Trykk så på holdeplassen du vil reise til.")
    	         .setCancelable(false)
    	         .setTitle("Hvordan bruke kartmodus")
    	         .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	             public void onClick(DialogInterface dialog, int id) {
    	            	 setViewedInstructions();
    	             }
    	         });
    	  
    	  AlertDialog secondalert = secondbuilder.create();
    	  
    	  secondalert.show();
        }

		MapOverlay mapOverlay = new MapOverlay();
		
		mapOverlays.add(mapOverlay);

	}
	
	public void populateBusstopList(){
		stopsCursor = mDbHelper.fetchAllBusstops();
        bussStopList = new ArrayList<BusStop>();
        if(stopsCursor.moveToFirst()){
        	int nameColumn = stopsCursor.getColumnIndex(QueryDb.KEY_STOP_NAME);
        	int latColumn = stopsCursor.getColumnIndex(QueryDb.KEY_STOP_LATITUDE);
        	int lngColumn = stopsCursor.getColumnIndex(QueryDb.KEY_STOP_LONGITUDE);
        
        do {
        	BusStop tempStop = new BusStop();
        	tempStop.setName(stopsCursor.getString(nameColumn));
        	tempStop.setLng(stopsCursor.getDouble(lngColumn));
        	tempStop.setLat(stopsCursor.getDouble(latColumn));
        	tempStop.setGpoint(
        			new GeoPoint( 
        			(int)(tempStop.getLatitude() * 1E6),
        			(int)(tempStop.getLongitude()* 1E6)));
        	bussStopList.add(tempStop);
//        	String tempString = " " + tempStop.getLatitude() + tempStop.getLongitude();
//        	Log.w("array creation", tempString);
        	
        }while(stopsCursor.moveToNext());
        	
        }
        
        new populateMapWithBusstopsTask().execute("String");
	}


	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void onLocationChanged(Location location) {
		if (location != null) {
			mylat = location.getLatitude();
			mylng = location.getLongitude();
			gPoint = new GeoPoint((int) (mylat * 1E6), (int) (mylng * 1E6));
			mapCont.animateTo(gPoint);
		}

	}

	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

//	@Override
//	public boolean dispatchTouchEvent(MotionEvent event){
//		return super.dispatchTouchEvent(event);
//	}

	class MapOverlay extends com.google.android.maps.Overlay
	{
		@Override
		public boolean draw(Canvas canvas, MapView mapView, 
				boolean shadow, long when) 
		{
			super.draw(canvas, mapView, shadow);                   
			
			if(gPoint != null){
				Point screenPts = new Point();
				mapView.getProjection().toPixels(gPoint, screenPts);
				canvas.drawBitmap(myLocationDrawable, screenPts.x, screenPts.y, null);
			}
			

			return true;
		}
	}
	
    private void setViewedInstructions(){
    	SharedPreferences settings = getSharedPreferences(Bus.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("HAS_READ_INSTRUCTIONS", true);
		editor.commit();
	}


	public void sendQuery(String fromString, String toString){
		//		String query = queryField.getText().toString();
		String query = "Neste fra "+fromString+" til "+ toString;
		//		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);  
		//		imm.hideSoftInputFromWindow(queryField.getWindowToken(), 0);  
		new getAnswerTask().execute(query);   
	}

	private class getAnswerTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... item) {
			String query = item[0];
			String returnText ="";
			publishProgress();
			returnText = SendQuery.sendQueryJsoup(query);
			
			return returnText;
		}

		@Override
		protected void onProgressUpdate(Void...voids){
			dialog = ProgressDialog.show(MapMode.this, "", getString(R.string.sending_query), true);
		}

		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
			//        	if(textEdited){
			//        		addQuery();
			//        	}else{
			//        		updateQuery();
			//        	}
		}

	}
	
	private class populateMapWithBusstopsTask extends AsyncTask<String, Integer, String>{
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(MapMode.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setMax(bussStopList.size());
			dialog.setMessage("Tegner holdeplasser på kart");
			dialog.setCancelable(false);
			dialog.show();
		}
		
		@Override
		protected synchronized String doInBackground(String... arg0){
			
			BusstopOverlay itemizedoverlay_first = new BusstopOverlay(busstopDrawable, MapMode.this);
			BusstopOverlay itemizedoverlay_second = new BusstopOverlay(busstopDrawable, MapMode.this);
			BusstopOverlay itemizedoverlay_third = new BusstopOverlay(busstopDrawable, MapMode.this);
			BusstopOverlay itemizedoverlay_fourth = new BusstopOverlay(busstopDrawable, MapMode.this);
			Iterator<BusStop> tempIterator = bussStopList.iterator();
			publishProgress(0);
			while (tempIterator.hasNext()){
				BusStop tempStop = tempIterator.next();
				OverlayItem tempItem = new OverlayItem(tempStop.getGeoPoint(), tempStop.getName(),"Bussholdeplass");
//				itemizedoverlay_first.addOverlay(tempItem);
				if(itemizedoverlay_first.size() < 70){
					itemizedoverlay_first.addOverlay(tempItem);
				}else if(itemizedoverlay_second.size() < 70){
					itemizedoverlay_second.addOverlay(tempItem);
				}else if(itemizedoverlay_third.size() < 70){
					itemizedoverlay_third.addOverlay(tempItem);
				}else{
					itemizedoverlay_fourth.addOverlay(tempItem);
				}
				publishProgress(itemizedoverlay_first.size()+itemizedoverlay_second.size()+itemizedoverlay_third.size()+itemizedoverlay_fourth.size());
			}
				mapOverlays.add(itemizedoverlay_first);
				mapOverlays.add(itemizedoverlay_second);
				mapOverlays.add(itemizedoverlay_third);
				
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer...voids){
			int progress = voids[0];
			dialog.setProgress(progress);
		}
		
		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			mapView.postInvalidate();
		}
	}
	
	private class populateBusstopsDbTask extends AsyncTask<String, Integer, String>{
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(MapMode.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setMax(340);
			dialog.setMessage("Laster ned holdeplassposisjoner");
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected String doInBackground(String... arg0) {
//	        Testing
			SaxFeedParser superParser = new SaxFeedParser("http://folk.ntnu.no/martinmi/bus_data.xml");
			stops = superParser.parse();
			int counter = 0;
        	Iterator<BusStop> iterator = stops.iterator();
        	while(iterator.hasNext()){
        		BusStop temp = iterator.next();
        		counter++;
        		publishProgress(counter);
        		mDbHelper.createBusstop(temp.getName(), temp.getLatitude(), temp.getLongitude());
        	}

			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer...voids){
			int progress = voids[0];
			dialog.setProgress(progress);
		}
		
		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			populateBusstopList();
		}
		
	}
	
    protected void onDestroy() {
        super.onDestroy();
        Bus.tracker.stop();
      }
}
