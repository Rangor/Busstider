package net.a2bsoft.buss;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import net.a2bsoft.buss.http.Realtime;
import no.norrs.busbuddy.pub.api.model.Departure;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by A2BSoft
 * User: martinmi
 * Date: 14.05.11
 * Time: 19.45
 * To change this template use File | Settings | File Templates.
 */
public class RealtimeActivity extends ListActivity {

    private DepartureListAdapter departureListAdapter;
    private List<Departure> departureList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.realtime);

//        String ans = Realtime.getRealtimeForBusstop("12");
//        System.out.println("Realtime ans =" + ans);

        Departure testDeparture1 = new Departure();
        testDeparture1.setDestination("Voll");
        testDeparture1.setLine("5");
        testDeparture1.setRealtimeData(true);
        testDeparture1.setScheduledDepartureTime(new LocalDateTime());

        departureList = new ArrayList<Departure>();
        departureList.add(testDeparture1);

        departureListAdapter = new DepartureListAdapter(this, R.layout.realtime_item, departureList);
        this.setListAdapter(departureListAdapter);
    }

    public class DepartureListAdapter extends ArrayAdapter<Departure> {
        private final List<Departure> departureList;

        public DepartureListAdapter(Context context, int textViewResourceId,
                                    List<Departure> departureList) {
            super(context, textViewResourceId, departureList);
            this.departureList = departureList;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            LayoutInflater viewInflater;

            // v.setBackgroundResource(R.drawable.list_color);
            if (view == null) {
                viewInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            final Departure departure = departureList.get(position);

            viewInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = viewInflater.inflate(R.layout.realtime_item, null);

            TextView lineNumber = (TextView) view.findViewById(R.id.line_number_text);
            TextView destinationName = (TextView) view.findViewById(R.id.destination_name_text);
            TextView realtimeValue = (TextView) view.findViewById(R.id.realtime_value_text);

            if (lineNumber != null) {
                lineNumber.setText(departure.getLine());
            }

            if (destinationName != null) {
                destinationName.setText(departure.getDestination());
            }

            if (realtimeValue != null) {
                realtimeValue.setText(departure.getScheduledDepartureTime().toString());
            }

            return view;
        }

    }

}
