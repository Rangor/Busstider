package net.a2bsoft.buss.parser;

import com.google.android.maps.GeoPoint;

public class BusStop {
	
	private String name;
	private double longitude;
	private double latitude;
	private GeoPoint gPoint;
	
	public BusStop(){

	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setLat(double latitude){
		this.latitude = latitude;
	}
	
	public void setLng(double longitude){
		this.longitude = longitude;
	}
	
	public void setGpoint(GeoPoint gPoint){
		this.gPoint = gPoint;
	}
	
	public String getName(){
		return this.name;
	}
	
	public double getLongitude(){
		return this.longitude;
	}
	
	public double getLatitude(){
		return this.latitude;
	}
	
	public GeoPoint getGeoPoint(){
		return this.gPoint;
	}

}
