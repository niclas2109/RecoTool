package com.doccuty.radarplus.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_group")
public class UserGroup {
	
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	
	private String userGroup;
	
	//================================================
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long value) {
		this.id = value;
	}
	
	public UserGroup withId(long value) {
		this.setId(value);
		return this;
	}

	//================================================	
	
	public String getUserGroup() {
		return this.userGroup;
	}
	
	public void setUserGroup(String value) {
		this.userGroup = value;
	}
	
	public UserGroup withUserGroup(String value) {
		this.setUserGroup(value);
		return this;
	}
}
