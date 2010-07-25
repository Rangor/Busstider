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
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;


public class MapMode extends MapActivity implements LocationListener {
	
	private MapView mapView;
	private MapController mapCont;
	private GeoPoint gPoint;
	public static double mylat, mylng;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        
        mapView = (MapView)findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        
        
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
	
	class MapOverlay extends com.google.android.maps.Overlay
    {
        @Override
        public boolean draw(Canvas canvas, MapView mapView, 
        boolean shadow, long when) 
        {
        	return true;
        }
 
        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) 
        {   
            //---when user lifts his finger---
            if (event.getAction() == 1) {
            	
                GeoPoint p = mapView.getProjection().fromPixels(
                    (int) event.getX(),
                    (int) event.getY());
                    Toast.makeText(getBaseContext(), 
                        p.getLatitudeE6() / 1E6 + "," + 
                        p.getLongitudeE6() /1E6 , 
                        Toast.LENGTH_SHORT).show();
                    
                    Geocoder geoCoder = new Geocoder(
                            getBaseContext(), Locale.getDefault());    
                            try {
                                List<Address> addresses = geoCoder.getFromLocation(
                                    p.getLatitudeE6()  / 1E6, 
                                    p.getLongitudeE6() / 1E6, 1);
             
                                String add = "";
                                if (addresses.size() > 0) 
                                {
                                    for (int i=0; i<addresses.get(0).getMaxAddressLineIndex(); 
                                         i++)
                                       add += addresses.get(0).getAddressLine(i) + "\n";
                                }
             
                                Toast.makeText(getBaseContext(), add, Toast.LENGTH_SHORT).show();
                            }
                            catch (IOException e) {                
                                e.printStackTrace();
                            }   
                            return true;
                        }                       
            return false;
        }        
    }

}
