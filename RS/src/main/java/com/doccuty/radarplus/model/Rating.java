package com.doccuty.radarplus.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="rating")
public class Rating {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	private double rating;
	
	@OneToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name="iditem")
	private Item item;
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="iduser")
	private User user;
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="idsetting")
	private Setting setting;

	
	//================================================
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long value) {
		this.id = value;
	}
	
	public Rating withId(long value) {
		this.setId(value);
		return this;
	}

	//================================================
	
	public double getRating() {
		return rating;
	}
	
	public void setRating(double value) {
		this.rating = value;
	}
	
	public Rating withRating(double value) {
		this.setRating(value);
		return this;
	}

	//================================================
	
	public Item getItem() {
		return item;
	}

	public void setItem(Item value) {
		this.item = value;
	}

	public Rating withItem(Item value) {
		this.setItem(value);
		return this;
	}

	//================================================
	

	public Setting getSetting() {
		return setting;
	}
	
	public void setSetting(Setting value) {
		this.setting = value;
	}

	public Rating withSetting(Setting value) {
		this.setSetting(value);
		return this;
	}

	//================================================
	

	public User getUser() {
		return user;
	}
	
	public void setUser(User value) {
		this.user = value;
	}

	public Rating withUser(User value) {
		this.setUser(value);
		return this;
	}
	
	
	@Override
	public String toString() {
		return this.id + " " + this.rating + " " + this.item;
	}
	
}
