package com.doccuty.radarplus.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="traffic_junction")
public class TrafficJunction {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	private String name;
	
	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="idaddress")
	private Address<?> address;

	@JsonIgnore
	//@JsonManagedReference(value="item-trafficjunction")
	@OneToMany(cascade=CascadeType.ALL, mappedBy="trafficJunction")
	private List<Item> item;
	
	//================================================
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long value) {
		this.id = value;
	}
	
	public TrafficJunction withId(long value) {
		this.setId(value);
		return this;
	}

	//================================================	
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String value) {
		this.name = value;
	}
	
	public TrafficJunction withName(String value) {
		this.setName(value);
		return this;
	}

	//================================================
	
	@SuppressWarnings("unchecked")
	public Address<TrafficJunction> getAddress() {
		return (Address<TrafficJunction>) this.address;
	}
	
	public void setAddress(Address<TrafficJunction> value) {
		this.address = value;
	}
	
	public TrafficJunction withAddress(Address<TrafficJunction> value) {
		this.setAddress(value);
		return this;
	}
	
	//================================================	
	
	public List<Item> getItem() {
		return this.item;
	}
	
	public void setItem(Item... values) {
		for(Item value : values) {
			if(!this.item.contains(value)) {
				this.item.add(value);
				value.withTrafficJunction(this);
			}
		}
	}
	
	public TrafficJunction withItem(Item... values) {
		this.setItem(values);
		return this;
	}
}
