package net.a2bsoft.buss.parser;

import android.content.Context;
//import com.sun.codemodel.internal.fmt.JSerializedObject;
import net.a2bsoft.buss.db.QueryDb;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

/**
 * Created by A2BSoft
 * User: martinmi
 * Date: 14.05.11
 * Time: 19.45
 * To change this template use File | Settings | File Templates.
 */
public class JsonParser {

    public void parseJsonFile(Context context){
        String filePath = "json/busstop";
        File file = new File(filePath);
        FileInputStream fileInputStream;
        DataInputStream dataInputStream;
        JSONTokener jsonTokener;

        QueryDb mDbHelper = new QueryDb(context);
        mDbHelper.open();

        try {
            jsonTokener = new JSONTokener(convertStreamToString(context.getResources().getAssets().open(filePath)));
            try {
                JSONObject busstopsJson = new JSONObject(jsonTokener);




                JSONArray busstopArray = busstopsJson.getJSONArray("busStops");

                System.out.println("Antall busstops! - " + busstopArray.length());

                int i = 0;

                while (i < busstopArray.length()){
                    JSONObject jsonStop = busstopArray.getJSONObject(i);
                    mDbHelper.createBusstop(jsonStop.getInt("busStopId"), jsonStop.getInt("locationId"), jsonStop.getString("name"), jsonStop.getDouble("latitude"), jsonStop.getDouble("longitude"));
                    i++;
                }

                mDbHelper.close();

            } catch (JSONException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static String convertStreamToString(InputStream is) {
	    /*
	     * To convert the InputStream to String we use the BufferedReader.readLine()
	     * method. We iterate until the BufferedReader return null which means
	     * there's no more data to read. Each line will appended to a StringBuilder
	     * and returned as String.
	     */

	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append(line + "\n");
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return sb.toString();
	}

}
