package com.doccuty.radarplus.persistence;

import static org.junit.Assert.*;

import java.util.UUID;

import javax.transaction.Transactional;

import org.junit.Test;

import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.persistence.ItemDAO;

public class ItemPersistenceTest {

	
	@Test
	@Transactional
	public void getItemImage() {
         
        ItemDAO ud = new ItemDAO();
              
	    Item item = ud.findById(4);
	    

        assertNotNull("Not null", item);
       
        assertNotEquals("Image", item.getImage(), null);
	}

	/**
	 * generating random unique id
	 * @return
	 */
	
	public long generateId() {
		return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
	}
}
