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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;


public class SecondMapMode extends MapActivity implements LocationListener {

	private static final int SEND_QUERY = Menu.FIRST + 1;

	private MapView mapView;
	private MapController mapCont;
	private GeoPoint gPoint, gPointClick;
	public static double mylat, mylng;
	private ProgressDialog dialog;
	private String toAdress;
	private String fromAdress; 

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_layout);

		mapView = (MapView)findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		mapView.setLongClickable(true);
		registerForContextMenu(mapView);
	
		try {
			InputStream in = getBaseContext().getAssets().open("bus_data.xml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mapCont = mapView.getController();

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 50.0f, this);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 50.0f, this);

		String coordinates[] = {"63.41738283634186", "10.407153367996216"};
		double lat = Double.parseDouble(coordinates[0]);
		double lng = Double.parseDouble(coordinates[1]);



		gPoint = new GeoPoint(
				(int) (lat * 1E6), 
				(int) (lng * 1E6));



		mapCont.animateTo(gPoint);
		mapCont.setZoom(16);

		MapOverlay mapOverlay = new MapOverlay();
		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		listOfOverlays.add(mapOverlay);        

		mapView.invalidate();

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (fromAdress != null){
			menu.add(0, SEND_QUERY, 0,"Fra "+ fromAdress + " til " + toAdress);
		}else{
			menu.add(0, SEND_QUERY, 0, R.string.send_query);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case SEND_QUERY:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			if (fromAdress != null && toAdress != null){
				sendQuery(fromAdress, toAdress);
			}else{
				//Showtoast that something is wrong
				Toast.makeText(getBaseContext(), "Something is wrong", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onContextItemSelected(item);
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

	@Override
	public boolean dispatchTouchEvent(MotionEvent event){
		return super.dispatchTouchEvent(event);
	}

	class MapOverlay extends com.google.android.maps.Overlay
	{
		@Override
		public boolean draw(Canvas canvas, MapView mapView, 
				boolean shadow, long when) 
		{
			super.draw(canvas, mapView, shadow);                   

			//---translate the GeoPoint to screen pixels---
			Point screenPts = new Point();
			Point screenPtsClick = new Point();
			mapView.getProjection().toPixels(gPoint, screenPts);

			Paint blue = new Paint();
			blue.setStyle(Style.FILL);
			blue.setARGB(128,0,98,213);

			Paint paint = new Paint();
			paint.setStyle(Style.FILL);
			paint.setStrokeWidth(5);
			paint.setAntiAlias(true);
			paint.setARGB(128, 255, 0, 0);


			//Draw you'r location
			canvas.drawCircle(screenPts.x, screenPts.y, 5, blue);

			if (gPointClick != null){
				mapView.getProjection().toPixels(gPointClick, screenPtsClick);
				//Draw the point you clicked on
				canvas.drawCircle(screenPtsClick.x, screenPtsClick.y, 5, blue);
			}


			return true;
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) 
		{

			//---when user lifts his finger---
			if (event.getAction() == 1) {

				gPointClick = mapView.getProjection().fromPixels(
						(int) event.getX(),
						(int) event.getY());

				Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
				double lat = gPointClick.getLatitudeE6()/1E6;
				double lng = gPointClick.getLongitudeE6()/1E6;
				List<Address> addresses = null;
				try {
					addresses = geoCoder.getFromLocation(gPointClick.getLatitudeE6() / 1E6 , gPointClick.getLongitudeE6() / 1E6, 1);
					toAdress = addresses.get(0).getAddressLine(0);

					addresses = geoCoder.getFromLocation(gPoint.getLatitudeE6()/1E6 ,gPoint.getLongitudeE6()/1E6,1);
					fromAdress = addresses.get(0).getAddressLine(0);
					//						Toast.makeText(getBaseContext(), "Martin bor: "+ addresses.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();
					//						sendQuery("Sverres gate",addresses.get(0).getAddressLine(0).toString() );
				} catch (IOException e) {
					// TODO Auto-generated catch block

					Toast.makeText(getBaseContext(), e.getMessage() , Toast.LENGTH_SHORT).show();
				}
				return false;
			}                       
			return false;
		}

		@Override
		public boolean onTap(GeoPoint p, MapView mapView){
			openContextMenu(mapView);
			return true;
		}

	}

	//	public boolean onTap(GeoPoint p, MapView mapView) {
	//		 
	//        openContextMenu(mapView);
	// 
	//         return true;
	//	}

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
			//returnText = SendQuery.sendQueryJsoup(query);
			
			return returnText;
		}

		@Override
		protected void onProgressUpdate(Void...voids){
			//dialog = ProgressDialog.show(MapMode.this, "", "Sp�r bussorakelet, vennligst vent", true);
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

}
