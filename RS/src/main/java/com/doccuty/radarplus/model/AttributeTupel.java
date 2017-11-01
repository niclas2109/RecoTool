package com.doccuty.radarplus.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "attribute_tupel")
public class AttributeTupel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected long id;

	@JsonBackReference(value = "attribute-tree")
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "idattribute_tupel_tree")
	AttributeTree attributeTree;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "idattribute")
	Attribute attribute;

	@JsonIgnore
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "attribute_tupel_parent", joinColumns = @JoinColumn(name = "idattribute_tupel"), inverseJoinColumns = @JoinColumn(name = "idparent"))
	private List<AttributeTupel> parent = new ArrayList<AttributeTupel>();

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "attribute_tupel_children", joinColumns = @JoinColumn(name = "idattribute_tupel"), inverseJoinColumns = @JoinColumn(name = "idchild"))
	private List<AttributeTupel> children = new ArrayList<AttributeTupel>();

	// ================================================

	public long getId() {
		return this.id;
	}

	public void setId(long value) {
		this.id = value;
	}

	public AttributeTupel withId(long value) {
		this.setId(value);
		return this;
	}

	// ================================================

	public Attribute getAttribute() {
		return this.attribute;
	}

	public void setAttribute(Attribute value) {
		this.attribute = value;
	}

	public AttributeTupel withAttribute(Attribute value) {
		this.setAttribute(value);
		return this;
	}

	// ================================================

	public List<AttributeTupel> getParent() {
		return this.parent;
	}

	public void setParent(AttributeTupel... values) {
		for (AttributeTupel value : values) {
			if (!this.parent.contains(value)) {
				this.parent.add(value);
				value.setChildren(this);
			}
		}
	}

	public AttributeTupel withParent(AttributeTupel... values) {
		this.setParent(values);
		return this;
	}

	// ================================================

	public List<AttributeTupel> getChildren() {
		return this.children;
	}

	public void setChildren(AttributeTupel... values) {
		for (AttributeTupel value : values) {
			if (!this.children.contains(value)) {
				this.children.add(value);
				value.setParent(this);
			}
		}
	}

	public AttributeTupel withChildren(AttributeTupel... values) {
		this.setChildren(values);
		return this;
	}

	// ================================================

	public AttributeTree getAttributeTree() {
		return this.attributeTree;
	}

	public void setAttributeTree(AttributeTree value) {
		this.attributeTree = value;
	}

	public AttributeTupel withAttributeTree(AttributeTree value) {
		this.setAttributeTree(value);
		return this;
	}

	@Override
	public String toString() {
		return this.id + " " + this.attribute + " " + this.children;
	}
}
