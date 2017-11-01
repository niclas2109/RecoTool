package com.doccuty.radarplus.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;
import java.util.HashMap;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "setting")
public class Setting implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private Date currentTime;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "idgeoposition")
	private Geoposition currentGeoposition;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "idtraffic_junction")
	private TrafficJunction trafficJunction;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "idnext_departure")
	private Geoposition nextDeparture;

	private Date estimatedDepartureTime;
	private Date currentDepartureTime;

	private Date estimatedArrivalTime;

	@Transient
	private HashMap<Item, Setting> usedItems;

	public Setting() {
		this.usedItems = new HashMap<Item, Setting>();
	}

	// ================================================

	public long getId() {
		return this.id;
	}

	public void setId(long value) {
		this.id = value;
	}

	public Setting withId(long value) {
		this.setId(value);
		return this;
	}

	// ================================================

	public Date getCurrentTime() {
		if(this.currentTime == null)
			this.currentTime = new Date();
		
		return this.currentTime;
	}

	public void setCurrentTime(Date value) {
		this.currentTime = value;
	}

	public Setting withCurrentTime(Date value) {
		this.setCurrentTime(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_UPDATE_USER_POSITION = "updateUserPosition";

	public Geoposition getGeoposition() {
		return this.currentGeoposition;
	}

	public void setGeoposition(Geoposition value) {
		if (value != null && !value.equals(this.currentGeoposition)) {
			Geoposition oldValue = this.currentGeoposition;
			this.currentGeoposition = value;
			this.firePropertyChange(PROPERTY_UPDATE_USER_POSITION, oldValue, this.currentGeoposition);
		}
	}

	public Setting withGeoposition(Geoposition value) {
		this.setGeoposition(value);
		return this;
	}

	// ================================================

	public TrafficJunction getTrafficJunction() {
		return this.trafficJunction;
	}

	public void setTrafficJunction(TrafficJunction value) {
		this.trafficJunction = value;
	}

	public Setting withTrafficJunction(TrafficJunction value) {
		this.setTrafficJunction(value);
		return this;
	}

	// ================================================

	@Transient
	public long getTimeToDeparture() {

		if (currentDepartureTime == null) {
			return 0;
		}

		long df = currentDepartureTime.getTime();
		long d = df - this.getCurrentTime().getTime();

		return d;
	}

	// ================================================

	public static final String PROPERTY_ESTIMATED_DEPARTURE_TIME = "estimatedDepartureTime";

	public Date getEstimatedDepartureTime() {
		return currentDepartureTime;
	}

	public void setEstimatedDepartureTime(Date value) {
		if (this.estimatedDepartureTime == null || !this.estimatedDepartureTime.equals(value)) {
			Date oldValue = this.estimatedDepartureTime;
			this.estimatedDepartureTime = value;
			this.firePropertyChange(PROPERTY_ESTIMATED_DEPARTURE_TIME, oldValue, value);
		}
	}

	public Setting withEstimatedDepartureTime(Date value) {
		setEstimatedDepartureTime(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_CURRENT_DEPARTURE_TIME = "currentDepartureTime";

	public Date getCurrentDepartureTime() {
		return currentDepartureTime;
	}

	public void setCurrentDepartureTime(Date value) {
		if (this.currentDepartureTime == null || !this.currentDepartureTime.equals(value)) {
			Date oldValue = this.currentDepartureTime;
			this.currentDepartureTime = value;
			this.firePropertyChange(PROPERTY_CURRENT_DEPARTURE_TIME, oldValue, value);
		}
	}

	public Setting withCurrentDepartureTime(Date value) {
		setCurrentDepartureTime(value);
		return this;
	}

	// ================================================

	public Date getEstimatedArrivalTime() {
		return estimatedArrivalTime;
	}

	public void setEstimatedArrivalTime(Date value) {
		this.estimatedArrivalTime = value;
	}

	public Setting withEstimatedArrivalTime(Date value) {
		setEstimatedArrivalTime(value);
		return this;
	}

	// ================================================

	public HashMap<Item, Setting> getUsedItem() {
		return this.usedItems;
	}

	public void setUsedItem(Item... values) {
		for (Item value : values) {
			try {
				Setting s = (Setting) this.clone();
				s.clearUsedItem();
				s.withId(0).withCurrentTime(this.currentTime);

				this.usedItems.put(value, s);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}

	public Setting withUsedItem(Item... values) {
		this.setUsedItem(values);
		return this;
	}

	public void clearUsedItem() {
		this.usedItems = new HashMap<Item, Setting>();
	}

	// ================================================

	public Geoposition getNextDeparture() {
		return this.nextDeparture;
	}

	public void setNextDeparture(Geoposition value) {
		this.nextDeparture = value;
	}

	public Setting withNextDestination(Geoposition value) {
		setNextDeparture(value);
		return this;
	}

	// ================================================

	public long getDelay() {

		if (this.currentDepartureTime == null || this.estimatedDepartureTime == null) {
			return 0;
		}

		return this.currentDepartureTime.getTime() - this.estimatedDepartureTime.getTime();
	}

	/**
	 * Event handling
	 */

	@Transient
	protected PropertyChangeSupport listeners = null;

	public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		if (listeners != null) {
			listeners.firePropertyChange(propertyName, oldValue, newValue);
			return true;
		}
		return false;
	}

	public boolean addPropertyChangeListener(PropertyChangeListener listener) {
		if (listeners == null) {
			listeners = new PropertyChangeSupport(this);
		}
		listeners.addPropertyChangeListener(listener);
		return true;
	}

	public boolean addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		if (listeners == null) {
			listeners = new PropertyChangeSupport(this);
		}
		listeners.addPropertyChangeListener(propertyName, listener);
		return true;
	}

	public boolean removePropertyChangeListener(PropertyChangeListener listener) {
		if (listeners == null) {
			listeners.removePropertyChangeListener(listener);
		}
		listeners.removePropertyChangeListener(listener);
		return true;
	}

	public boolean removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		if (listeners != null) {
			listeners.removePropertyChangeListener(propertyName, listener);
		}
		return true;
	}

	@Override
	public String toString() {
		return this.id + " " + this.currentTime + " " + this.getGeoposition();
	}
}
