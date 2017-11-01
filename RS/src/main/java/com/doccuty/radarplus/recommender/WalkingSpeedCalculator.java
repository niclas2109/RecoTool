package com.doccuty.radarplus.recommender;

import java.util.Date;
import java.util.LinkedHashMap;

import com.doccuty.radarplus.model.Geoposition;
import com.doccuty.radarplus.model.RecoTool;

import java.util.Iterator;

public class WalkingSpeedCalculator extends LinkedHashMap<Long, Geoposition> {

	private static final long serialVersionUID = 1L;
	private static final int MAX_SIZE = 10;

	RecoTool app;
	
	public WalkingSpeedCalculator (RecoTool app) {
		super();
		this.app = app;
	}
	
	public void withPosition(Geoposition value) {
		this.put(new Date().getTime(), value);
	}

	// Calculate walking speed in [km/h]
	public long getCurrentWalkingSpeed() {
		
		if(this.size() < 3)
			return 0;
		
		double distance = 0;
		Geoposition lastPos = null;

		long start = 0;
		long end = 0;

		for (Iterator<java.util.Map.Entry<Long, Geoposition>> it = this.entrySet().iterator(); it.hasNext();) {

			java.util.Map.Entry<Long, Geoposition> e = it.next();

			if (lastPos == null) {
				lastPos = e.getValue();
				start = e.getKey();
				continue;
			}

			distance += (this.app.getUseGeocoordinates()) ? lastPos.distance(e.getValue()) : lastPos.euclideanDistance(e.getValue());
			end = e.getKey();
			
			lastPos = e.getValue();
		}
		
		double speed = distance * 1000 * 3600 / (end - start);
		
		return (long) speed;
	}
	
	@Override
	public Geoposition put(Long key, Geoposition value) {
		
		if(this.size() > MAX_SIZE) {
			Long oldKey = this.keySet().iterator().next();
			this.remove(oldKey);
		}
		
        return super.put(key, value);
    }

}
