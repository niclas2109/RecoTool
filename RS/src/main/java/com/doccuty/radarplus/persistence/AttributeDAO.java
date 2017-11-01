package com.doccuty.radarplus.persistence;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;

import com.doccuty.radarplus.model.Attribute;

public class AttributeDAO {

	public void save(Attribute entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();
		session.save(entity);
        session.getTransaction().commit();
        session.close();
	}

	public Attribute findById(long id) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Attribute attribute = session.get(Attribute.class, id);
        session.close();
        
        return this.unproxy(attribute);
	}

	public List<Attribute> findAll() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<Attribute> list = (List<Attribute>) session.createQuery("from Attribute").list();
		session.close();
		
		for(Attribute a : list) {
			a = this.unproxy(a);
		}
		
		return list;
	}
	
	public void update(Attribute entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();
		session.save(entity);
        session.getTransaction().commit();
        session.close();
	}


	public void delete(Attribute entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.delete(entity);
		session.close();
	}

	public void deleteAll() {
		List<Attribute> entityList = findAll();
		for (Attribute entity : entityList) {
			delete(entity);
		}
	}
	
	public Attribute unproxy(Attribute proxied)
	{
		Attribute entity = proxied;
	    if (entity instanceof HibernateProxy) {
	        Hibernate.initialize(entity);
	        entity = (Attribute) ((HibernateProxy) entity)
	                  .getHibernateLazyInitializer()
	                  .getImplementation();
	    }
	    return entity;
	}
}