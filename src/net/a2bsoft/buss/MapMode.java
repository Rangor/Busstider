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

import android.view.MotionEvent;
import android.view.View;
import net.a2bsoft.buss.db.QueryDb;
import net.a2bsoft.buss.parser.BusStop;
import net.a2bsoft.buss.parser.JsonParser;
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
    private ArrayList<OverlayItem> bussStopList;
    public static OverlayItem fromItem;
    public static OverlayItem toItem;
    public static String fromString;
    public static String toString;

    public static String answerString;

    private Cursor stopsCursor;
    List<Overlay> mapOverlays;
    private Drawable busstopDrawable;
    private Bitmap myLocationDrawable;
    private LocationManager lm;

    private List<BusStop> stops;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        System.out.println("Oncreate ble kjort");
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);

        //Fetch settings
        SharedPreferences settings = getSharedPreferences(Bus.PREFS_NAME, 0);

        //Google Analytics tracker object
        Bus.tracker.trackPageView("/Mapmode");

        String coordinates[] = {"63.41738283634186", "10.407153367996216"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);

        mapCont = mapView.getController();

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

        //stopsCursor = mDbHelper.fetchAllBusstops();
        stopsCursor = mDbHelper.fetchAllBusstopsByLatLong(10,10,10,10);
        startManagingCursor(stopsCursor);
        populateBusstopList();

        //Create a list of BusStops from the DB
//        if (stopsCursor.getCount() > 1) {
//            populateBusstopList();
//        } else {
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(MapMode.this);
//
//            builder.setMessage(getString(R.string.mapmode_download_data_question))
//                    .setCancelable(false)
//                    .setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            new populateBusstopsDbTask().execute("String");
//                        }
//                    })
//                    .setNegativeButton(getString(R.string.answer_no), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.cancel();
//                        }
//                    });
//
//            AlertDialog alert = builder.create();
//
//            alert.show();
//
//        }

        if (!settings.getBoolean("HAS_READ_INSTRUCTIONS", false)) {
            AlertDialog.Builder secondbuilder = new AlertDialog.Builder(MapMode.this);
            secondbuilder.setMessage(getString(R.string.mapmode_instructions))
                    .setCancelable(false)
                    .setTitle(getString(R.string.mapmode_instructions_title))
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

    public void mapClickHandler(View target) {
        System.out.println("Du hakket kartet!");
    }

    public void populateBusstopList() {

//        stopsCursor = mDbHelper.fetchAllBusstops();
        stopsCursor = mDbHelper.fetchAllBusstopsByLatLong(10,10,10,10);
        bussStopList = new ArrayList<OverlayItem>();
        if (stopsCursor.moveToFirst()) {
            int nameColumn = stopsCursor.getColumnIndex(QueryDb.KEY_STOP_NAME);
            int latColumn = stopsCursor.getColumnIndex(QueryDb.KEY_STOP_LATITUDE);
            int lngColumn = stopsCursor.getColumnIndex(QueryDb.KEY_STOP_LONGITUDE);

            do {
                GeoPoint tempGeoPoint =
                        new GeoPoint(
                                (int) (stopsCursor.getDouble(latColumn) * 1E6),
                                (int) (stopsCursor.getDouble(lngColumn) * 1E6));

                bussStopList.add(new OverlayItem(tempGeoPoint, stopsCursor.getString(nameColumn), "Bussholdeplass"));
//        	String tempString = " " + tempStop.getLatitude() + tempStop.getLongitude();
//        	Log.w("array creation", tempString);

            } while (stopsCursor.moveToNext());

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

    class MapOverlay extends com.google.android.maps.Overlay {
        @Override
        public boolean draw(Canvas canvas, MapView mapView,
                            boolean shadow, long when) {
            super.draw(canvas, mapView, shadow);

            if (gPoint != null) {
                Point screenPts = new Point();
                mapView.getProjection().toPixels(gPoint, screenPts);
                canvas.drawBitmap(myLocationDrawable, screenPts.x, screenPts.y, null);
            }


            return true;
        }
    }

    private void setViewedInstructions() {
        SharedPreferences settings = getSharedPreferences(Bus.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("HAS_READ_INSTRUCTIONS", true);
        editor.commit();
    }

    private class populateMapWithBusstopsTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MapMode.this);
            dialog.setMessage(getString(R.string.mapmode_drawing_places_message));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected synchronized String doInBackground(String... arg0) {

            BusstopOverlay itemizedoverlay = new BusstopOverlay(busstopDrawable, MapMode.this, bussStopList);
            mapOverlays.add(itemizedoverlay);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... voids) {
            int progress = voids[0];
            dialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            mapView.postInvalidate();
        }
    }

    private class populateBusstopsDbTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MapMode.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Laster ned holdeplassposisjoner");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {

            JsonParser parser = new JsonParser();
            parser.parseJsonFile(getApplicationContext());

//            SaxFeedParser superParser = new SaxFeedParser(getString(R.string.mapmode_url));
//            stops = superParser.parse();
//            int counter = 0;
//            Iterator<BusStop> iterator = stops.iterator();
//            while (iterator.hasNext()) {
//                BusStop temp = iterator.next();
//                counter++;
//                publishProgress(counter);
//                mDbHelper.createBusstop(temp.getName(), temp.getLatitude(), temp.getLongitude());
//            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... voids) {
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

    protected void onPause() {
        super.onPause();
        lm.removeUpdates(this);
    }

    protected void onResume() {
        super.onResume();
        if (lm != null) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 50.0f, this);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 50.0f, this);
        }
    }

}
