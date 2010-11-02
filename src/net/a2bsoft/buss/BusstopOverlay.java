package net.a2bsoft.buss;

import java.util.ArrayList;
import java.util.List;

import net.a2bsoft.buss.http.SendQuery;
import net.a2bsoft.buss.parser.BusStop;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class BusstopOverlay extends ItemizedOverlay<OverlayItem> {
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext;
	private Drawable marker;
	private ProgressDialog dialog;
	private Drawable fromDrawable;
	private Drawable toDrawable;
//	private OverlayItem fromItem;
//	private OverlayItem toItem;
	
	
	public BusstopOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}
	
	public BusstopOverlay(Drawable defaultMarker, Context context) {
		  this(defaultMarker);
		  mContext = context;
		}
	
	public BusstopOverlay(Drawable defaultMarker, Context context, List<BusStop> list) {
		  this(defaultMarker);
		  mContext = context;
		}
	
	public void setMarker(OverlayItem overlay, Drawable marker){
		overlay.setMarker(boundCenterBottom(marker));
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}

	@Override
	public int size() {
	  return mOverlays.size();
	}
	
	public void addOverlay(OverlayItem overlay) {
//		overlay.setMarker(boundCenterBottom(marker));
	    mOverlays.add(overlay);
	    populate();
	}
	
	public void addOverlay(OverlayItem overlay, Drawable marker){
		overlay.setMarker(boundCenterBottom(marker));
    	mOverlays.add(overlay);
    	populate();
    }

	
	@Override
	protected boolean onTap(int index) {
	  final OverlayItem item = mOverlays.get(index);
//	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
//	  dialog.setTitle(item.getTitle());
//	  dialog.setMessage(item.getSnippet());
//	  dialog.show();
//	  if(travelFrom == null){
	  AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	  if(MapMode.fromString == null){
	  builder.setMessage("Reise fra " + item.getTitle() + "?")
	         .setCancelable(false)
	         .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
	             public void onClick(DialogInterface dialog, int id) {
	            	  marker = mContext.getResources().getDrawable(R.drawable.busstop_blue);
	            	  if(MapMode.fromItem != null){
	            		  setMarker(MapMode.fromItem, marker);
	            	  }
	            	  if(MapMode.toItem != null){
	            	  	  setMarker(MapMode.toItem, marker);
	            	  }
	            	  MapMode.fromString = (item.getTitle());
	                  fromDrawable = mContext.getResources().getDrawable(R.drawable.busstop_from);
	            	  MapMode.fromItem = item;
	                  setMarker(item,fromDrawable);
	             }
	         })
	         .setNegativeButton("Nei", new DialogInterface.OnClickListener() {
	             public void onClick(DialogInterface dialog, int id) {
	                  dialog.cancel();
	             }
	         });
	  }else{
		  builder.setMessage("Reise til " + item.getTitle() + "?")
	         .setCancelable(false)
	         .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
	             public void onClick(DialogInterface dialog, int id) {
	                  MapMode.toString = (item.getTitle());
	                  MapMode.toItem = item;
	                  toDrawable = mContext.getResources().getDrawable(R.drawable.busstop_to);
	                  setMarker(item,toDrawable);
//	                  String returnText = SendQuery.sendQueryJsoup("Neste fra "+ travelFrom + " til " + travelTo);
	  				  Bus.tracker.trackEvent(
			                "Clicks",  // Category
			                "Send sporring",  // Action
			                "mapmode", // Label
			                1);       // Value
	  				  Bus.tracker.dispatch();
	                  new getAnswerTask().execute("Neste fra "+ MapMode.fromString + " til " + MapMode.toString);
	                  
	                  MapMode.fromString = null;
	                  MapMode.toString = null;
	             }
	         })
	         .setNegativeButton("Nei", new DialogInterface.OnClickListener() {
	             public void onClick(DialogInterface dialog, int id) {
	                  dialog.cancel();
	             }
	         });
	  }
//	  }
	  
	  AlertDialog alert = builder.create();
	  
	  alert.show();
	  
	  return true;
	}
	
	private class getAnswerTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... item) {
			String query = item[0];
			String returnText ="";
			publishProgress();
			returnText = SendQuery.sendQuery(query);
			
			return returnText;
		}

		@Override
		protected void onProgressUpdate(Void...voids){
			dialog = ProgressDialog.show(mContext, "", mContext.getString(R.string.sending_query), true);
		}

		@Override
		protected void onPostExecute(String result) {
			MapMode.answerString = result;
			dialog.dismiss();
      	  	AlertDialog.Builder resultDialog = new AlertDialog.Builder(mContext);
      	  	resultDialog.setTitle("Svar");
      	  	resultDialog.setMessage(result);
      	  	resultDialog.show();
//			Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
			//        	if(textEdited){
			//        		addQuery();
			//        	}else{
			//        		updateQuery();
			//        	}
		}

	}
	
	

}


