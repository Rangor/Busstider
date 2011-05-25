package net.a2bsoft.buss;

import android.app.Activity;
import android.os.Bundle;
import net.a2bsoft.buss.http.Realtime;

/**
 * Created by A2BSoft
 * User: martinmi
 * Date: 14.05.11
 * Time: 19.45
 * To change this template use File | Settings | File Templates.
 */
public class RealtimeActivity extends Activity{

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.realtime);

        String ans = Realtime.getRealtimeForBusstop("12");
        System.out.println("Realtime ans =" + ans);

    }

}
