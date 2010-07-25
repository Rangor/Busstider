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

import java.util.Calendar;

import org.json.JSONException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemSelectedListener;

public class ToAndFrom extends Activity {
	
	private ProgressDialog dialog;
	private Button sendQueryButton;
	private Button switchButton;
	private AutoCompleteTextView fromTextField;
	private AutoCompleteTextView toTextField;
	private TextView answerText;
	private String toString;
	private String fromString;
	private String answerString;
	private String timeChoiceString;
	private QueryDb mDbHelper;
	private Spinner timeSpinner;
	
    private TextView mTimeDisplay;
    private Button mPickTime;

    private int mHour;
    private int mMinute;

    static final int TIME_DIALOG_ID = 0;
    
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toandfrom_layout);
        
        mDbHelper = new QueryDb(this);
        mDbHelper.open();
        
        fromTextField = (AutoCompleteTextView)findViewById(R.id.fromField);
        toTextField = (AutoCompleteTextView)findViewById(R.id.toField);
        answerText = (TextView)findViewById(R.id.answerText);
        
        //Checks for last answers
        if (savedInstanceState != null){
        	answerText.setText(savedInstanceState.getString("LAST_TOANDFROM_ANS"));
        	toTextField.setText(savedInstanceState.getString("LAST_TO"));
        	fromTextField.setText(savedInstanceState.getString("LAST_FROM"));
        }
        
        fillAutoCompletion();
        
        setDefaultTab(1);
        
        sendQueryButton = (Button)findViewById(R.id.sendButton);
        sendQueryButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				sendQuery();
			}
		});
        
        switchButton = (Button)findViewById(R.id.switchButton);
        switchButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				switchDestinations();
			}
		});
        mPickTime = (Button) findViewById(R.id.pickAfterTime);
        mPickTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });

        // get the current time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // display the current date
        updateDisplay();
        
       timeSpinner = (Spinner) findViewById(R.id.time_spinner);
       ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.times_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(adapter);
        timeSpinner.setOnItemSelectedListener(new MySpinnerListener());
        
        
    }
	
	private void switchDestinations(){
		String tempString = fromTextField.getText().toString();
		fromTextField.setText(toTextField.getText());
		toTextField.setText(tempString);
	}
	
	private void sendQuery(){
		
		
		String query = "Neste buss fra "+ fromTextField.getText().toString() + " til " + toTextField.getText().toString();
		
		if(timeChoiceString != null){
			if(timeChoiceString.equals("Etter")){
				query = query + " etter " + mPickTime.getText().toString() ;
			}
			
			if(timeChoiceString.equals("F¿r")){
				query = query + " f¿r " + mPickTime.getText().toString() ;
			}
			
			if(timeChoiceString.equals("V¾re der til")){
				query = query +" for Œ v¾re der til " + mPickTime.getText().toString() ;
			}
			
			
		}
		
		addPlace(fromTextField.getText().toString());
		addPlace(toTextField.getText().toString());
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);  
		imm.hideSoftInputFromWindow(toTextField.getWindowToken(), 0);  
		new getAnswerTask().execute(query);   
		
	}
	
    private class getAnswerTask extends AsyncTask<String, Void, String> {
        @Override
    	protected String doInBackground(String... item) {
        	String query = item[0];
        	String returnText ="";
        	publishProgress();
        	
				
				try {
					returnText = SendQuery.sendQueryJson(query);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			return returnText;
        }
        
        @Override
		protected void onProgressUpdate(Void...voids){
        	dialog = ProgressDialog.show(ToAndFrom.this, "", "Sp¿r bussorakelet, vennligst vent", true);
        }

        @Override
		protected void onPostExecute(String result) {
        	dialog.dismiss();
        	answerText.setText(result);
        }

    }
    
 // updates the time we display in the TextView
    private void updateDisplay() {
    	mPickTime.setText(
            new StringBuilder()
                    .append(pad(mHour)).append(":")
                    .append(pad(mMinute)));
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
    
    private void fillAutoCompletion(){
    	Cursor placesCursor = mDbHelper.fetchAllPlaces();
    	startManagingCursor(placesCursor);
        
        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] db_places = new String[placesCursor.getCount()];
        if(placesCursor.moveToFirst()){
        	String name;
        	int nameColumn = placesCursor.getColumnIndex(QueryDb.KEY_PLACENAME);
        	int counter = 0;
        
        do {
        	
        	db_places[counter] = placesCursor.getString(nameColumn);
        	counter++;
        	
        }while(placesCursor.moveToNext());
        
        }
        
        ArrayAdapter<String> new_auto_complete_adapter = new ArrayAdapter<String>(this, R.layout.list_item, db_places );
        fromTextField.setAdapter(new_auto_complete_adapter);
        toTextField.setAdapter(new_auto_complete_adapter);
    }
    
    public void addPlace(String place){
    	mDbHelper.createPlace(place);
    	fillAutoCompletion();
    }
    
 // the callback received when the user "sets" the time in the dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
        new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mHour = hourOfDay;
                mMinute = minute;
                updateDisplay();
            }
        };
        
        @Override
        protected Dialog onCreateDialog(int id) {
            switch (id) {
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        mTimeSetListener, mHour, mMinute, true);
            }
            return null;
        }
        
        private void setDefaultTab(int newGui){
    		SharedPreferences settings = getSharedPreferences(Bus.PREFS_NAME, 0);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putInt("DEFAULT_GUI", newGui);
    		editor.commit();
    	}
        
        public class MySpinnerListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
				long id) {
			timeChoiceString = parent.getItemAtPosition(pos).toString();

		}

		public void onNothingSelected(AdapterView<?> arg0) {
			timeChoiceString = "";
		}
        }
        @Override
    	public void onSaveInstanceState(Bundle outState) {
        	super.onSaveInstanceState(outState);
        	outState.putString("LAST_TOANDFROM_ANS", answerText.getText().toString());
        	outState.putString("LAST_TO", toTextField.getText().toString());
        	outState.putString("LAST_FROM", fromTextField.getText().toString());
    	}

}
