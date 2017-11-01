package com.doccuty.radarplus.persistence;

import java.util.List;

import org.hibernate.Session;
import com.doccuty.radarplus.model.TrafficJunction;

public class TrafficJunctionDAO {

	public void save(TrafficJunction entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();
		session.save(entity);
        session.getTransaction().commit();
        session.close();
	}

	public TrafficJunction findById(long id) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		TrafficJunction trafficJunction = session.get(TrafficJunction.class, id);
        session.close();
        
        return trafficJunction;
	}

	public List<TrafficJunction> findAll() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<TrafficJunction> list = (List<TrafficJunction>) session.createQuery("from TrafficJunction").list();
		session.close();
		return list;
	}
	
	public List<TrafficJunction> findAllOrderByNameAsc() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<TrafficJunction> list = (List<TrafficJunction>) session.createQuery("FROM TrafficJunction ORDER BY name ASC").list();
		session.close();
		return list;
	}
	
	public List<TrafficJunction> findAllOrderByNameDesc() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<TrafficJunction> list = (List<TrafficJunction>) session.createQuery("FROM TrafficJunction ORDER BY name DESC").list();
		session.close();
		return list;
	}
	
	public void update(TrafficJunction entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();
		session.save(entity);
        session.getTransaction().commit();
        session.close();
	}


	public void delete(TrafficJunction entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.delete(entity);
		session.close();
	}

	public void deleteAll() {
		List<TrafficJunction> entityList = findAll();
		for (TrafficJunction entity : entityList) {
			delete(entity);
		}
	}
}