package com.doccuty.radarplus.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "attribute_tupel_tree")
@Inheritance(strategy = InheritanceType.JOINED)
public class AttributeTree {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected long id;

	@JsonIgnore
	// @JsonManagedReference(value="attribute-tree")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "attributeTree", fetch = FetchType.EAGER)
	protected List<AttributeTupel> attribute;

	public AttributeTree() {
		attribute = new ArrayList<AttributeTupel>();
	}

	// ================================================
	public static final String PROPERTY_ID = "id";

	public long getId() {
		return this.id;
	}

	public void setId(long value) {
		if (this.id != value) {
			long oldValue = this.id;
			this.id = value;

			firePropertyChange(PROPERTY_ID, oldValue, value);
		}
	}

	public AttributeTree withId(long value) {
		this.setId(value);
		return this;
	}

	// ================================================

	@JsonIgnore
	public List<AttributeTupel> getAttribute() {
		return attribute;
	}

	public void setAttribute(AttributeTupel... values) {
		for (AttributeTupel value : values) {
			if (!this.attribute.contains(value)) {
				this.attribute.add(value);
				value.setAttributeTree(this);
			}
		}
	}

	public AttributeTree withAttribute(AttributeTupel... values) {
		this.setAttribute(values);
		return this;
	}

	/**
	 * Count occurrences of an attribute in this items Therefore, the whole
	 * attribute tree must be traversed
	 * 
	 * @param value
	 * @return
	 */

	public int countAttributeOccurence(Attribute value) {
		return this.traverseFullTree(this.attribute, value);
	}

	public int traverseFullTree(List<AttributeTupel> value, Attribute target) {
		int cntr = 0;
		for (AttributeTupel attribute : value) {

			if (attribute.getAttribute().getId() == target.getId()) {
				cntr++;
			}

			if (!attribute.getChildren().isEmpty())
				cntr += this.traverseFullTree(attribute.getChildren(), target);
		}

		return cntr;
	}

	/**
	 * Check, if an attribute is contained by the item
	 * 
	 * @param value
	 * @return
	 */

	public boolean hasAttribute(Attribute value) {
		boolean found = false;

		found = this.traverseTree(this.attribute, value);

		return found;
	}

	public boolean removeAttribute(Attribute value) {
		AttributeTupel aT = null;
		for (Iterator<AttributeTupel> it = this.attribute.iterator(); it.hasNext();) {

			AttributeTupel attribute = it.next();

			if (attribute.getAttribute().compareTo(value) == 0) {
				aT = attribute;
				aT.setAttributeTree(null);
				it.remove();
				break;
			}
		}

		return this.attribute.remove(aT);
	}

	public boolean traverseTree(List<AttributeTupel> value, Attribute target) {

		boolean found = false;

		if (target == null)
			return found;

		for (AttributeTupel attribute : value) {

			if (attribute.getAttribute().compareTo(target) == 0) {
				found = true;
				break;
			}

			if (found == true)
				break;

			if (!attribute.getChildren().isEmpty())
				found = this.traverseTree(attribute.getChildren(), target);
		}

		return found;
	}

	/**
	 * Count occurrence of each attribute
	 * 
	 * @return
	 */

	@JsonIgnore
	@Transient
	public LinkedHashMap<Attribute, Integer> getAttributeHistogram(List<AttributeTupel> values,
			LinkedHashMap<Attribute, Integer> histo) {

		for (AttributeTupel attribute : values) {

			if (histo.containsKey(attribute.getAttribute())) {
				histo.put(attribute.getAttribute(), histo.get(attribute.getAttribute()) + 1);
			} else {
				histo.put(attribute.getAttribute(), 1);
			}

			if (!attribute.getChildren().isEmpty())
				histo = this.getAttributeHistogram(attribute.getChildren(), histo);
		}

		return histo;
	}

	/**
	 * Creates a list of Attributes included in the tree
	 * 
	 * @param values
	 * @param histo
	 * @return
	 */
	@JsonIgnore
	@Transient
	public List<Attribute> getAttributeList(List<AttributeTupel> values, List<Attribute> list) {

		for (AttributeTupel attribute : values) {

			if (!list.contains(attribute.getAttribute())) {
				list.add(attribute.getAttribute());
			}

			if (!attribute.getChildren().isEmpty())
				list = this.getAttributeList(attribute.getChildren(), list);
		}

		return list;
	}

	@JsonIgnore
	@Transient
	public List<Attribute> getAttributeList() {
		return this.getAttributeList(this.attribute, new ArrayList<Attribute>());
	}

	@JsonIgnore
	public Duration getEstimatedUsageDuration() {
		Duration max = Duration.ofMillis(0);

		for (Attribute a : this.getAttributeList()) {
			if (a.getMinDuration() != null && a.getMinDuration().toMillis() > max.toMillis()) {
				max = a.getMinDuration();
			}
		}

		return max;
	}

	@Override
	public String toString() {
		return "id: " + this.id + " " + this.attribute;
	}

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

}
