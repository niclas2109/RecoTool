package com.doccuty.radarplus.persistence;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;

import com.doccuty.radarplus.model.Item;

public class ItemDAO {

	public void save(Item entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();
		session.save(entity);
        session.getTransaction().commit();
        session.close();
	}

	public Item findById(long id) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Item item = session.get(Item.class, id);
        session.close();
        
        return item;
	}

	public List<Item> findAll() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<Item> list = (List<Item>) session.createQuery("FROM Item").list();
		session.close();
		
		for(Item item : list) {
			item = this.unproxy(item);
		}
		
		return list;
	}
	

	public List<Item> findRegularItems() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<Item> list = (List<Item>) session.createQuery("FROM Item i  WHERE isTrainingItem = false").list();
		session.close();
		
		for(Item item : list) {
			item = this.unproxy(item);
		}
		
		return list;
	}
	
	public List<Item> findTrainingItems() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<Item> list = (List<Item>) session.createQuery("from Item i where isTrainingItem = true").list();
		session.close();
		
		for(Item item : list) {
			item = this.unproxy(item);
		}
		
		return list;
	}
	
	public void update(Item entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();
		session.save(entity);
        session.getTransaction().commit();
        session.close();
	}


	public void delete(Item entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();
		session.delete(entity);
        session.getTransaction().commit();
		session.close();
	}

	public void deleteAll() {
		List<Item> entityList = findAll();
		for (Item entity : entityList) {
			delete(entity);
		}
	}
	
	public Item unproxy(Item proxied)
	{
		Item entity = proxied;
	    if (entity instanceof HibernateProxy) {
	        Hibernate.initialize(entity);
	        entity = (Item) ((HibernateProxy) entity)
	                  .getHibernateLazyInitializer()
	                  .getImplementation();
	    }
	    return entity;
	}
}