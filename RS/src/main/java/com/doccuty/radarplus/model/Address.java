package com.doccuty.radarplus.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="address")
public class Address<T> {

	@Id
    @GeneratedValue
	private long id;
	
	private String street;
	private String housingNumber;
	

	@OneToMany(cascade=CascadeType.ALL, mappedBy="address", targetEntity=TrafficJunction.class)
	private List<T> object;
	
	//================================================
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long value) {
		this.id = value;
	}
	
	public Address<T> withId(long value) {
		this.setId(value);
		return this;
	}

	//================================================	
	
	public String getStreet() {
		return this.street;
	}
	
	public void setStreet(String value) {
		this.street = value;
	}
	
	public Address<T> withStreet(String value) {
		this.setStreet(value);
		return this;
	}

	//================================================	
	
	public String getHousingNumber() {
		return this.housingNumber;
	}
	
	public void setHousingNumber(String value) {
		this.housingNumber = value;
	}
	
	public Address<T> withHousingNumber(String value) {
		this.setHousingNumber(value);
		return this;
	}
	
	//================================================	
	
	public List<T> getObject() {
		return object;
	}

	@SuppressWarnings("unchecked")
	public void setObject(Object... objects) {
		for(Object object : objects) {
			this.object.add((T) object);
		}
	}
	
	public Address<T> withObject(Object... objects) {
		setObject(objects);
		return this;
	}

}
