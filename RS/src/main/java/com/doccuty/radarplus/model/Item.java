package com.doccuty.radarplus.model;

import java.util.Date;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "item")
@PrimaryKeyJoinColumn(name = "id")
public class Item extends AttributeTree {

	private String name;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "idimage")
	private Image image;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false, length = 10, updatable = false)
	private Date dateOfRegistration = new Date();

	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dateOfLastUpdate", nullable = false)
	private Date dateOfLastUpdate = new Date();

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "idgeoposition")
	private Geoposition geoposition;

	@JsonBackReference(value="item-trafficjunction")
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "idtraffic_junction")
	private TrafficJunction trafficJunction;

	@Column(length = 65535, columnDefinition = "text")
	private String description;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "iddomain")
	private Attribute domain;
	
	private boolean isTrainingItem;
	private boolean isProductivityItem;

	private boolean hasWifi;
	private boolean hasSockets;
	private boolean isOutdoor;
	private boolean hasOutdoorArea;
	
	@Column(columnDefinition="int default 0")
	private int minAge = 0;

	@Column(columnDefinition="float default 0")
	private float averageAge = 0;
		
	public Item() {
		super();
	}

	// ================================================

	public Item withId(long value) {
		super.setId(value);
		return this;
	}

	// ================================================

	public String getName() {
		return this.name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public Item withName(String value) {
		this.setName(value);
		return this;
	}

	// ================================================

	public Attribute getDomain() {
		return this.domain;
	}

	public void setDomain(Attribute value) {
		this.domain = value;
	}

	public Item withDomain(Attribute value) {
		this.setDomain(value);
		return this;
	}

	// ================================================

	public boolean getIsTrainingItem() {
		return this.isTrainingItem;
	}

	public void setIsTrainingItem(boolean value) {
		this.isTrainingItem = value;
	}

	public Item withIsTrainingItem(boolean value) {
		this.setIsTrainingItem(value);
		return this;
	}

	// ================================================

	public Geoposition getGeoposition() {
		return this.geoposition;
	}

	public void setGeoposition(Geoposition value) {
		this.geoposition = value;
	}

	public Item withGeoposition(Geoposition value) {
		this.setGeoposition(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_IMAGE = "drug_image";

	public Image getImage() {
		return this.image;
	}

	public void setImage(Image value) {
		if (this.image != value) {
			Image oldValue = this.image;
			this.image = value;
			this.firePropertyChange(PROPERTY_IMAGE, oldValue, value);
		}
	}

	public Item withImage(Image value) {
		setImage(value);
		return this;
	}

	// ================================================

	public TrafficJunction getTrafficJunction() {
		return this.trafficJunction;
	}

	public void setTrafficJunction(TrafficJunction value) {
		this.trafficJunction = value;
	}

	public Item withTrafficJunction(TrafficJunction value) {
		this.setTrafficJunction(value);
		return this;
	}

	// ================================================

	public boolean getIsProductivityItem() {
		return isProductivityItem;
	}

	public void setIsProductivityItem(boolean value) {
		this.isProductivityItem = value;
	}

	public Item withIsProductivityItem(boolean value) {
		setIsProductivityItem(value);
		return this;
	}

	// ================================================

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String value) {
		this.description = value;
	}

	public Item withDescription(String value) {
		setDescription(value);
		return this;
	}

	// ================================================

	public boolean isHasOutdoorArea() {
		return hasOutdoorArea;
	}

	public void setHasOutdoorArea(boolean value) {
		this.hasOutdoorArea = value;
	}

	public Item withHasOutdoorArea(boolean value) {
		setHasOutdoorArea(value);
		return this;
	}

	// ================================================

	public boolean isHasWifi() {
		return hasWifi;
	}

	public void setHasWifi(boolean value) {
		this.hasWifi = value;
	}

	public Item withHasWifi(boolean value) {
		setHasWifi(value);
		return this;
	}

	// ================================================

	public boolean isHasSockets() {
		return hasSockets;
	}

	public void setHasSockets(boolean value) {
		this.hasSockets = value;
	}

	public Item withHasSockets(boolean value) {
		setHasSockets(value);
		return this;
	}

	// ================================================

	public boolean getIsOutdoor() {
		return isOutdoor;
	}

	public void setIsOutdoor(boolean value) {
		this.isOutdoor = value;
	}

	public Item withIsOutdoor(boolean value) {
		setIsOutdoor(value);
		return this;
	}

	// ================================================

	public int getMinAge() {
		return minAge;
	}

	public void setMinAge(int value) {
		this.minAge = value;
	}

	public Item withMinAge(int value) {
		setMinAge(value);
		return this;
	}

	// ================================================

	public float getAverageAge() {
		return averageAge;
	}

	public void setAverageAge(float value) {
		this.averageAge = value;
	}

	public Item withAverageAge(float value) {
		setAverageAge(value);
		return this;
	}

	// ================================================

	public Item withAttribute(AttributeTupel... values) {
		super.setAttribute(values);
		return this;
	}

	// ================================================

	@Override
	public String toString() {
		return this.id + " " + this.name;
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
