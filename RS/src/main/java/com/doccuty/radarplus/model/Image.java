package com.doccuty.radarplus.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import javax.activation.MimetypesFileTypeMap;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;

import com.doccuty.radarplus.model.util.ImageDeserializer;
import com.doccuty.radarplus.model.util.ImageSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "image")
@JsonSerialize(using = ImageSerializer.class)
@JsonDeserialize(using = ImageDeserializer.class)
public class Image implements Serializable {

	private static final long serialVersionUID = 1L;

	public void removeYou() {
		firePropertyChange("REMOVE_YOU", this, null);
	}

	// ==========================================================================

	public static final String PROPERTY_ID = "id";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	public long getId() {
		return this.id;
	}

	public void setId(long value) {
		if (this.id != value) {

			long oldValue = this.id;
			this.id = value;
			this.firePropertyChange(PROPERTY_ID, oldValue, value);
		}
	}

	public Image withId(long value) {
		setId(value);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append(" ").append(this.getId());
		result.append(" ").append(this.getFilename());
		result.append(" ").append(this.getFiletype());
		result.append(" ").append(this.getImage());
		result.append(" ").append(this.getUploadedAt());
		return result.substring(1);
	}

	// ==========================================================================

	public static final String PROPERTY_FILENAME = "filename";

	private String filename;

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String value) {
		if (this.filename != null && !this.filename.equals(value)) {

			String oldValue = this.filename;
			this.filename = value;
			this.firePropertyChange(PROPERTY_FILENAME, oldValue, value);
		}
	}

	public Image withFilename(String value) {
		setFilename(value);
		return this;
	}

	// ==========================================================================

	public static final String PROPERTY_FILETYPE = "filetype";

	private String filetype;

	public String getFiletype() {
		return this.filetype;
	}

	public void setFiletype(String value) {
		if (this.filetype != null && this.filetype.equals(value)) {

			String oldValue = this.filetype;
			this.filetype = value;
			this.firePropertyChange(PROPERTY_FILETYPE, oldValue, value);
		}
	}

	public Image withFiltype(String value) {
		setFiletype(value);
		return this;
	}

	// ==========================================================================

	public static final String PROPERTY_IMAGE = "image";

	@Lob
	private byte[] image;

	public byte[] getImage() {
		return this.image;
	}

	public void setImage(byte[] value) {
		if (this.image != value) {
			byte[] oldValue = this.image;
			this.image = value;
			this.firePropertyChange(PROPERTY_IMAGE, oldValue, value);
		}
	}

	public void setImagePath(String path) throws IOException {
		File file = new File(path);

		byte[] byteStream = new byte[(int) file.length()];
		FileInputStream fileInputStream = new FileInputStream(file);
		fileInputStream.read(byteStream);
		fileInputStream.close();

		this.setImage(byteStream);

		this.setFiletype(new MimetypesFileTypeMap().getContentType(file));

		this.setFilename(file.getName());
	}

	public Image withImage(byte[] value) {
		setImage(value);
		return this;
	}

	public Image withImagePath(String value) throws IOException {
		setImagePath(value);
		return this;
	}

	// ==========================================================================

	public static final String PROPERTY_UPLOADEDATE = "uploadedAt";

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Date uploadedAt;

	public Date getUploadedAt() {
		return this.uploadedAt;
	}

	public void setUploadedAt(Date value) {
		if (this.uploadedAt != value) {
			this.uploadedAt = value;
		}
	}

	public Image withUploadedAt(Date value) {
		setUploadedAt(value);
		return this;
	}

	// ==========================================================================

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
