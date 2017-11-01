package com.doccuty.radarplus.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name="disability")
public class Disability {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	private String disability;
		
	@ManyToMany(cascade=CascadeType.ALL, mappedBy="disability")
	private List<User> user;
	
	//================================================
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long value) {
		this.id = value;
	}
	
	public Disability withId(long value) {
		this.setId(value);
		return this;
	}

	//================================================	
	
	public String getDisability() {
		return this.disability;
	}
	
	public void setDisability(String value) {
		this.disability = value;
	}
	
	public Disability withDisability(String value) {
		this.setDisability(value);
		return this;
	}

	//================================================	
	
	public List<User> getUser() {
		return this.user;
	}
	
	public void setUser(User... values) {
		
		if(this.user == null)
			this.user = new ArrayList<User>();
		
		for(User value : values) {
			if(!this.user.contains(value)) {
				this.user.add(value);
				value.withDisability(this);
			}
		}
	}
	
	public Disability withUser(User... values) {
		this.setUser(values);
		return this;
	}
}
