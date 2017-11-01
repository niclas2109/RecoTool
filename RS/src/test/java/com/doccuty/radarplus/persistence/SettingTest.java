package com.doccuty.radarplus.persistence;

import static org.junit.Assert.*;

import java.util.Date;

import javax.transaction.Transactional;

import org.junit.Test;

import com.doccuty.radarplus.model.Geoposition;
import com.doccuty.radarplus.model.Setting;
import com.doccuty.radarplus.model.TrafficJunction;
import com.doccuty.radarplus.persistence.SettingDAO;

public class SettingTest {

	@Test
	@Transactional
	public void saveSetting() {

		Geoposition geoposition = new Geoposition().withLatitude(9.121442027324).withLongitude(7.232384914);
		
		TrafficJunction trafficJunction = new TrafficJunction().withName("Flughafen");
		
        Setting setting = new Setting().withCurrentDepartureTime(new Date())
    			.withEstimatedDepartureTime(new Date())
    			.withEstimatedArrivalTime(new Date())
    			.withCurrentTime(new Date())
    			.withGeoposition(geoposition)
    			.withTrafficJunction(trafficJunction);
        
        SettingDAO ud = new SettingDAO();
        
        int oldSize = ud.findAll().size();
        
        ud.save(setting);

        assertEquals("Not saved", oldSize+1, ud.findAll().size());
	}
	
	@Test
	@Transactional
	public void deleteSetting() {
         
        SettingDAO ud = new SettingDAO();
              
        Setting setting = new Setting().withCurrentDepartureTime(new Date())
        			.withEstimatedDepartureTime(new Date())
        			.withCurrentTime(new Date())
        			.withGeoposition(new Geoposition().withLatitude(9.121442027324).withLongitude(7.232384914));
        
        ud.save(setting);
        
        int oldSize = ud.findAll().size();

        ud.delete(setting);
        
        System.out.println(setting);
        
        assertEquals("Not deleted",oldSize-1, ud.findAll().size());
	}
}
