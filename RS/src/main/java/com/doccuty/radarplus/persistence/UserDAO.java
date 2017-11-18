package com.doccuty.radarplus.persistence;

import java.util.List;

import org.hibernate.Session;
import com.doccuty.radarplus.model.User;

public class UserDAO {

	public void save(User entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();
		session.save(entity);
        session.getTransaction().commit();
        session.close();
	}

	public User findById(long id) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		User user = session.get(User.class, id);
        session.close();
        
        return user;
	}

	public List<User> findAll() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<User> list = (List<User>) session.createQuery("FROM User ORDER BY firstname, lastname").list();
		session.close();
		return list;
	}
	
	public void update(User entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();
		session.update(entity);
        session.getTransaction().commit();
        session.close();
	}


	public void delete(User entity) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.getTransaction().begin();
		session.delete(entity);
        session.getTransaction().commit();
		session.close();
	}

	public void deleteAll() {
		List<User> entityList = findAll();
		for (User entity : entityList) {
			delete(entity);
		}
	}
}