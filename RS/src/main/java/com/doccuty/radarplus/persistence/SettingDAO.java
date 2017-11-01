package com.doccuty.radarplus.persistence;

import java.util.List;

import org.hibernate.Session;
import com.doccuty.radarplus.model.Setting;

public class SettingDAO {

	public void save(Setting entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();
		session.save(entity);
        session.getTransaction().commit();
        session.close();
	}

	public Setting findById(long id) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Setting setting = session.get(Setting.class, id);
        session.close();
        
        return setting;
	}

	public List<Setting> findAll() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<Setting> list = (List<Setting>) session.createQuery("from Setting").list();
		session.close();
		return list;
	}
	
	public void update(Setting entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();
		session.save(entity);
        session.getTransaction().commit();
        session.close();
	}


	public void delete(Setting entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();
		session.delete(entity);
        session.getTransaction().commit();
		session.close();
	}

	public void deleteAll() {
		List<Setting> entityList = findAll();
		for (Setting entity : entityList) {
			delete(entity);
		}
	}
}