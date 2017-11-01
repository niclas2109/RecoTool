package com.doccuty.radarplus.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "geoposition")
public class Geoposition {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	private double lat;
	private double lng;

	// ================================================

	public long getId() {
		return this.id;
	}

	public void setId(long value) {
		this.id = value;
	}

	public Geoposition withId(long value) {
		this.setId(value);
		return this;
	}

	// ================================================

	public double getLatitude() {
		return this.lat;
	}

	public void setLatitude(double value) {
		this.lat = value;
	}

	public Geoposition withLatitude(double value) {
		this.setLatitude(value);
		return this;
	}

	// ================================================

	public double getLongitude() {
		return this.lng;
	}

	public void setLongitude(double value) {
		this.lng = value;
	}

	public Geoposition withLongitude(double value) {
		this.setLongitude(value);
		return this;
	}

	/**
	 * Geographical distance in [km]
	 * 
	 * @param geoposition
	 * @return
	 */

	public double distance(Geoposition geoposition) {

		if(geoposition == null)
			return 0;
		
		double theta = this.lng - geoposition.getLongitude();
		double dist = Math.sin(deg2rad(this.lat)) * Math.sin(deg2rad(geoposition.getLatitude()))
				+ Math.cos(deg2rad(this.lat)) * Math.cos(deg2rad(geoposition.getLatitude())) * Math.cos(deg2rad(theta));

		dist = Math.acos(dist);
		dist = this.deg2rad(dist);
		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344;

		return (dist);
	}
	
	/**
	 * Euclidean distance in [km]
	 * 
	 * @param geoposition
	 * @return
	 */


	public double euclideanDistance(Geoposition geoposition) {

		double dLng = Math.pow(this.lng - geoposition.getLongitude(), 2);
		double dLat = Math.pow(this.lat - geoposition.getLatitude(), 2);
		double dist = Math.sqrt(dLng + dLat);

		return dist / 1000;
	}

	private double deg2rad(double rad) {
		return (rad * 180 / Math.PI);
	}
	
	
	
	@Override
	public String toString() {
		return this.id + " lat: " + Math.round(this.getLatitude() * 1000) / 1000 + " lng: " + Math.round(this.getLongitude() * 1000) / 1000;
	}
}
