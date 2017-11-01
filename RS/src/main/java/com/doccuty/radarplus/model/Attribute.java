package com.doccuty.radarplus.model;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="attribute")
public class Attribute implements Comparable<Attribute>, Serializable {


	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected long id;

	private String attribute;

	@OneToOne(cascade=CascadeType.ALL)
	private Image image;


	// minimum duration of usage
	@JsonIgnore
	@Column(columnDefinition = "bigint default 0")
	Duration minDuration;

	
	//================================================
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long value) {
		this.id = value;
	}
	
	public Attribute withId(long value) {
		this.setId(value);
		return this;
	}

	//================================================	
	
	public String getAttribute() {
		return this.attribute;
	}
	
	public void setAttribute(String value) {
		this.attribute = value;
	}
	
	public Attribute withAttribute(String value) {
		this.setAttribute(value);
		return this;
	}
	
	//================================================	
	
	public Duration getMinDuration() {
		if(this.minDuration == null)
			this.minDuration = Duration.ZERO;
		
		return this.minDuration;
	}
	
	public void setMinDuration(Duration value) {
		this.minDuration = value;
	}
	
	public Attribute withMinDuration(Duration value) {
		this.setMinDuration(value);
		return this;
	}
	
	
	//================================================	
	
	public Image getImage() {
		return this.image;
	}
	
	public void setImage(Image value) {
		this.image = value;
	}
	
	public Attribute withImage(Image value) {
		this.setImage(value);
		return this;
	}
	
	

	
	@Override
	public int compareTo(Attribute value) {
		if(value == null)
			return -1;
		
		if(value.getId() == this.id) {
			return 0;
		} else {
			return this.attribute.compareTo(value.getAttribute());
		}
	}
	
	
	@Override
	public String toString() {
		return this.id + " " + this.getAttribute();
	}
	

	@Override
	public boolean equals(Object obj) {
	    if (obj == this)
	        return true;
	    if (!(obj instanceof Item))
	        return false;

	    Item other = (Item) obj;
	    return Objects.equals(getId(), other.getId());
	}

	@Override
	public int hashCode() {
	    return Objects.hash(getId());
	}
}