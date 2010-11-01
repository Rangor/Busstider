/*
 * "Busstider" made by Martin Syvertsen
 * www.a2bsoft.net for changelog and info
 * 
 * This code is modified from the NotePad example from developer.android.com, from this follows this notice:
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
package net.a2bsoft.buss.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class QueryDb {

    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_TIMESTAMP = "timestamp";
    
    public static final String KEY_PLACEID="_id";
    public static final String KEY_PLACENAME="place_name";
    
    public static final String KEY_STOP_ID="_id";
    public static final String KEY_STOP_NAME="name";
    public static final	String KEY_STOP_LATITUDE="latitude";
    public static final String KEY_STOP_LONGITUDE="longitude";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table notes (_id integer primary key autoincrement, "
    				+ "timestamp datetime not null,"
                    + "title text not null, body text not null);";

    private static final String DATABASE_PLACE_TABLE = "places";
    private static final String DATABASE_CREATE_PLACE =
            "create table places (_id integer primary key not null, place_name varchar(150) unique not null);";
    
    private static final String DATABASE_BUSSTOP_TABLE = "busstops";
    private static final String DATABASE_CREATE_BUSSTOP =
    	"create table busstops (_id integer primary key autoincrement, name varchar(150) not null, latitude real not null, longitude real not null);";
    
    
    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 7;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE_PLACE);
            db.execSQL(DATABASE_CREATE_BUSSTOP);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 6 && newVersion == 7){
            	Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", adding BUSSTOP table");
            	upgradeFrom6to7(db);
            }else{
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_PLACE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_BUSSTOP_TABLE);
            onCreate(db);
            }
        }
        
        public void upgradeFrom6to7(SQLiteDatabase db){
        	db.execSQL(DATABASE_CREATE_BUSSTOP);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public QueryDb(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public QueryDb open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long createNote(String title, String body, String timestamp) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_TIMESTAMP, timestamp);
        initialValues.put(KEY_BODY, body);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    public long createPlace(String place){
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_PLACENAME, place);
    	
    	return mDb.insert(DATABASE_PLACE_TABLE, null, initialValues);
    }
    
    public Cursor fetchAllPlaces(){
    	return mDb.query(true, DATABASE_PLACE_TABLE, new String[]{KEY_PLACEID,KEY_PLACENAME},null, null, null, null, null, null);
    }
    
    public long createBusstop(String name, double latitude, double longitude){
    	Log.w(TAG, "ADDING BUSTTOP");
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_STOP_NAME, name);
    	initialValues.put(KEY_STOP_LATITUDE, latitude);
    	initialValues.put(KEY_STOP_LONGITUDE, longitude);
    	return mDb.insert(DATABASE_BUSSTOP_TABLE, null, initialValues);
    }
    
    public Cursor fetchAllBusstops(){
    	return mDb.query(true, DATABASE_BUSSTOP_TABLE, new String[]{KEY_STOP_NAME, KEY_STOP_LATITUDE, KEY_STOP_LONGITUDE},null, null, null, null, null, null);
    }
    
//    public Cursor numberOfBusstops(){
////    	String numberOfStops = mDb.execSQL("select count(*) from "+ DATABASE_BUSSTOP_TABLE);
//    	
//        return mDb.query(DATABASE_BUSSTOP_TABLE, null, "select count(*)", null, null, null, null);
//    }
    

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY, KEY_TIMESTAMP}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_TITLE, KEY_BODY}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String title, String body, String timestamp) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);
        args.put(KEY_TIMESTAMP, timestamp);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}

