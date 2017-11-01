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
	
	@Test
	@Transactional
	public void deleteItem() {
         
        ItemDAO ud = new ItemDAO();
              
        Item item = new Item();
        item.withName("Café Panama");
        
        ud.save(item);
        
        int oldSize = ud.findAll().size();

        ud.delete(item);
        
        System.out.println(item);
        
        assertEquals("Not deleted",oldSize-1, ud.findAll().size());
	}
	
	/**
	 * generating random unique id
	 * @return
	 */
	
	public long generateId() {
		return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
	}
}
