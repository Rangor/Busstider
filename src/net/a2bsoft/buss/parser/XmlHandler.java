package net.a2bsoft.buss.parser;

import java.util.ArrayList;
import java.util.List;

import net.a2bsoft.buss.db.QueryDb;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlHandler extends DefaultHandler{
    private List<BusStop> busstops;
    private BusStop currentBusStop;
    private StringBuilder builder;
    private int counter;
    private QueryDb mDbHelper;
    
    public List<BusStop> getNames(){
        return this.busstops;
    }
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);
        builder.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        super.endElement(uri, localName, name);
        if (this.busstops != null){
            if (localName.equalsIgnoreCase(BaseFeedParser.NODE)){
            	busstops.add(currentBusStop);
            }
            
            
        }
//    	MapMode.MapModeMessage = Integer.toString(this.counter);
        
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        busstops = new ArrayList<BusStop>();
        builder = new StringBuilder();
        counter = 0;
//        mDbHelper = new QueryDb(context);
    }

    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);
        if (localName.equalsIgnoreCase(BaseFeedParser.NODE)){
            
        	this.currentBusStop = new BusStop();
        	
        	String latitude = attributes.getValue("lat");
        	String longitude = attributes.getValue("lon");
        	
        	
        	this.currentBusStop .setLat(Double.parseDouble(latitude));
        	this.currentBusStop.setLng(Double.parseDouble(longitude));
        	
        }else if(localName.equalsIgnoreCase(BaseFeedParser.TAG)){
        		
        		if(attributes.getValue("k").equals("name")){
            		String stopName = (attributes.getValue("v"));
            		this.currentBusStop.setName(stopName);
//            		this.busstops.add(this.currentBusStop);
            	}
        	}
    }
}
