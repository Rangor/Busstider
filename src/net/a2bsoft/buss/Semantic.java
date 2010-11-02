/*
 * "Busstider" made by Martin Syvertsen
 * www.a2bsoft.net for changelog and info
 * 
 * Parts of this code is modified from the NotePad example from developer.android.com, from this follows this notice:
 * 
 * Copyright (C) 2008 Google Inc.
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
import java.util.TimeZone;

import net.a2bsoft.buss.db.QueryDb;
import net.a2bsoft.buss.http.SendQuery;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class Semantic extends ListActivity implements TextWatcher {
	
	private static final int DELETE_ID = Menu.FIRST + 1;
	
	private QueryDb mDbHelper;
	private EditText queryField;
	private Button clearAnswerButton;
	private Button sendQueryButton;
	private TextView answerText;
	private ProgressDialog dialog;
	private String qTimestamp;
	private boolean textEdited;
	private long lastQueryId;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bus.tracker.trackPageView("/Semantic");
		
		setContentView(R.layout.semantic_layout);
        mDbHelper = new QueryDb(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
        
        //Sets the current tab as default tab
        setDefaultTab(0);
        
        queryField = (EditText)findViewById(R.id.entry);
        queryField.addTextChangedListener(this);
        
        answerText = (TextView)findViewById(R.id.answer_text);
        if(savedInstanceState != null){
  			String myString = savedInstanceState.getString("LAST_ANS");
  	  		answerText.setText(myString);
        }
        
        sendQueryButton = (Button)findViewById(R.id.send_button);
        sendQueryButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
		        Bus.tracker.trackEvent(
		                "Clicks",  // Category
		                "Send sporring",  // Action
		                "semantic", // Label
		                1);       // Value
		        Bus.tracker.dispatch();
				sendQuery();
			}
		});
        
        clearAnswerButton = (Button)findViewById(R.id.clear_answer_button);
        clearAnswerButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {	
				Bus.tracker.trackEvent(
		                "Clicks",  // Category
		                "Tom svar",  // Action
		                "semantic", // Label
		                1);       // Value
					answerText.setText("");
				
			}
		});
        
    }
    
    
    private void fillData() {
        Cursor notesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(notesCursor);

        String[] from = new String[]{QueryDb.KEY_TITLE, QueryDb.KEY_TIMESTAMP};
        int[] to = new int[]{R.id.text1, R.id.timestamp};
        	
        SimpleCursorAdapter notes = 
        	    new SimpleCursorAdapter(this, R.layout.query_row, notesCursor, from, to);
        setListAdapter(notes);
    }       
	
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
    	case DELETE_ID:
    		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	        mDbHelper.deleteNote(info.id);
	        fillData();
	        if(info.id == lastQueryId){
	        	textEdited = true;
	        }
	        return true;
		}
		return super.onContextItemSelected(item);
	}
	
    private void addQuery() {
    	final Calendar c = Calendar.getInstance();
    	c.setTimeZone(TimeZone.getTimeZone("CET"));
        qTimestamp = c.getTime().toLocaleString();
    	mDbHelper.createNote(queryField.getText().toString(), answerText.getText().toString(), qTimestamp);
    	fillData();
    	textEdited = false;
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	Cursor note = mDbHelper.fetchNote(id);
        startManagingCursor(note);
        queryField.setText(note.getString(note.getColumnIndexOrThrow(QueryDb.KEY_TITLE)));
        answerText.setText(note.getString(note.getColumnIndexOrThrow(QueryDb.KEY_BODY)));
        lastQueryId = id;
        textEdited = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
    
    public void sendQuery(){
		String query = queryField.getText().toString();
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);  
		imm.hideSoftInputFromWindow(queryField.getWindowToken(), 0);  
		new getAnswerTask().execute(query);   
    }
    
    public void updateQuery(){
    	final Calendar c = Calendar.getInstance();
    	c.setTimeZone(TimeZone.getTimeZone("CET"));
        qTimestamp = c.getTime().toLocaleString();
    	mDbHelper.updateNote(lastQueryId, queryField.getText().toString(), answerText.getText().toString(), qTimestamp);
    	fillData();
    	textEdited = false;
    }
    
    private class getAnswerTask extends AsyncTask<String, Void, String> {
        @Override
    	protected String doInBackground(String... item) {
        	String query = item[0];
        	String returnText ="";
        	publishProgress();
//        	returnText = SendQuery.sendQueryJsoup(query);
        	returnText = SendQuery.sendQuery(query);

			return returnText;
        }
        
        @Override
		protected void onProgressUpdate(Void...voids){
        	dialog = ProgressDialog.show(Semantic.this, "", getString(R.string.sending_query), true);
        }

        @Override
		protected void onPostExecute(String result) {
        	dialog.dismiss();
        	answerText.setText(result);
        	if(textEdited){
        		addQuery();
        	}else{
        		updateQuery();
        	}
        }

    }

	public void afterTextChanged(Editable arg0) {
		textEdited = true;
	}


	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}


	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}
	
    private void setDefaultTab(int newGui){
		SharedPreferences settings = getSharedPreferences(Bus.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("DEFAULT_GUI", newGui);
		editor.commit();
	}
    
    @Override
    protected void onDestroy() {
      super.onDestroy();
      Bus.tracker.stop();
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	  outState.putString("LAST_ANS", answerText.getText().toString());
	}
}
