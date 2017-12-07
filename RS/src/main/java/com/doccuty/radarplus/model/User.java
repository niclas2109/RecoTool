package com.doccuty.radarplus.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * This class represents users in RecoTool.
 * @author Niclas Kannengießer
 *
 */

@Entity
@Table(name = "user")
@PrimaryKeyJoinColumn(name = "id")
public class User extends AttributeTree {

	private String firstname;
	private String lastname;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "idgender")
	private Attribute gender;

	@Temporal(TemporalType.DATE)
	@Column(length = 10)
	private Calendar dateOfBirth;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false, length = 10, updatable = false)
	private Date dateOfRegistration = new Date();

	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dateOfLastUpdate", nullable = false)
	private Date dateOfLastUpdate = new Date();

	@Column(unique = true)
	private String username;

	@Transient
	private Language language;

	@Transient
	private Address<User> address;

	@Column(unique = true)
	private String email;

	private boolean adaptivityEnabled;

	@Transient
	private UserGroup userGroup;

	@Transient
	private long currentWalkingSpeed;

	@Column(columnDefinition = "double default 1")
	private long minWalkingSpeed;

	@Column(columnDefinition = "double default 1")
	private long avgWalkingSpeed;

	@Column(columnDefinition = "double default 1")
	private long maxWalkingSpeed;

	private int bufferToNextConnection;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "user_disability", joinColumns = @JoinColumn(name = "iduser"), inverseJoinColumns = @JoinColumn(name = "iddisability"))
	private List<Disability> disability = new ArrayList<Disability>();

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "user_constraint", joinColumns = @JoinColumn(name = "iduser"), inverseJoinColumns = @JoinColumn(name = "idattribute"))
	private Set<Attribute> constraint = new HashSet<Attribute>();

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.EAGER)
	private Set<Rating> rating;

	// calculated preference vector
	@Transient
	private LinkedHashMap<Attribute, Double> preferences;

	// ================================================

	public User withId(long value) {
		super.setId(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_FIRSTNAME = "firstname";

	public String getFirstname() {
		return this.firstname;
	}

	public void setFirstname(String value) {
		if (this.firstname == null || !this.firstname.equals(value)) {
			String oldValue = this.firstname;
			this.firstname = value;

			firePropertyChange(PROPERTY_FIRSTNAME, oldValue, value);
		}
	}

	public User withFirstname(String value) {
		this.setFirstname(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_LASTNAME = "lastname";

	public String getLastname() {
		return this.lastname;
	}

	public void setLastname(String value) {
		if (this.lastname == null || !this.lastname.equals(value)) {
			String oldValue = this.lastname;
			this.lastname = value;

			firePropertyChange(PROPERTY_LASTNAME, oldValue, value);
		}
	}

	public User withLastname(String value) {
		this.setLastname(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_GENDER = "gender";

	public Attribute getGender() {
		return this.gender;
	}

	public void setGender(Attribute value) {
		if (this.gender != null)
			this.removeAttribute(this.gender);

		this.gender = value;
		
		this.setAttribute(value);
	}

	public User withGender(Attribute value) {
		this.setGender(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_ADDRESS = "address";

	public Address<User> getAddress() {
		return this.address;
	}

	public void setAddress(Address<User> value) {
		this.address = value;
	}

	public User withAddress(Address<User> value) {
		this.setAddress(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_EMAIL = "email";

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String value) {
		if (this.email == null || !this.email.equals(value)) {
			String oldValue = this.email;
			this.email = value;

			firePropertyChange(PROPERTY_EMAIL, oldValue, value);
		}
	}

	public User withEmail(String value) {
		this.setEmail(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_DATE_OF_BIRTH = "dateOfBirth";

	public Calendar getDateOfBirth() {
		return this.dateOfBirth;
	}

	public void setDateOfBirth(Calendar value) {
		this.dateOfBirth = value;
	}

	public User withDateOfBirth(Calendar value) {
		this.setDateOfBirth(value);
		return this;
	}

	public int getAge() {

		if (this.dateOfBirth == null) {
			return 0;
		}
		Calendar cal = this.dateOfBirth;

		// Conversion from Calendar to LocalDate requires month + 1
		LocalDate b = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
		long age = b.until(LocalDate.now(), ChronoUnit.YEARS);

		return (int) age;
	}

	// ================================================

	public static final String PROPERTY_DATE_OF_REGISTRATION = "dateOfRegistration";

	public Date getDateOfRegistration() {
		return this.dateOfRegistration;
	}

	public void setDateOfRegistration(Date value) {

		if (this.dateOfRegistration == null || !this.dateOfRegistration.equals(value)) {
			Date oldValue = this.dateOfRegistration;
			this.dateOfRegistration = value;

			firePropertyChange(PROPERTY_DATE_OF_REGISTRATION, oldValue, value);
		}
	}

	public User withDateOfRegistration(Date value) {
		this.setDateOfRegistration(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_DATE_OF_LAST_UPDATE = "dateOfLastUpdate";

	public Date getDateOfLastUpdate() {
		return this.dateOfLastUpdate;
	}

	public void setDateOfLastUpdate(Date value) {

		if (this.dateOfLastUpdate == null || !this.dateOfLastUpdate.equals(value)) {
			Date oldValue = this.dateOfLastUpdate;
			this.dateOfLastUpdate = value;

			firePropertyChange(PROPERTY_DATE_OF_LAST_UPDATE, oldValue, value);
		}
	}

	public User withDateOfLastUpdate(Date value) {
		this.setDateOfLastUpdate(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_USERNAME = "username";

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String value) {
		if (this.username == null || !this.username.equals(value)) {
			String oldValue = this.username;
			this.username = value;

			firePropertyChange(PROPERTY_USERNAME, oldValue, value);
		}
	}

	public User withUsername(String value) {
		this.setUsername(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_LANGUAGE = "language";

	public Language getLanguage() {
		return this.language;
	}

	public void setLanguage(Language value) {
		this.language = value;
	}

	public User withLaguage(Language value) {
		this.setLanguage(value);
		return this;
	}

	// ================================================

	public boolean getAdaptivityEnabled() {
		return this.adaptivityEnabled;
	}

	public void setAdaptivityEnabled(boolean value) {
		this.adaptivityEnabled = value;
	}

	public User withAdaptivityEnabled(boolean value) {
		this.setAdaptivityEnabled(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_USER_GROUP = "userGroup";

	public UserGroup getUserGroup() {
		return this.userGroup;
	}

	public void setUserGroup(UserGroup value) {
		this.userGroup = value;
	}

	public User withUserGroup(UserGroup value) {
		this.setUserGroup(value);
		return this;
	}

	// ================================================

	public static final String PROPERTY_CURRENT_WALKING_SPEED = "currentWalkingSpeed";

	// walking speed in [km/h]

	public long getCurrentWalkingSpeed() {
		if (this.currentWalkingSpeed == 0) {
			this.currentWalkingSpeed = 1;
		}

		return this.currentWalkingSpeed;
	}

	public void setCurrentWalkingSpeed(long value) {
		this.currentWalkingSpeed = value;
		this.firePropertyChange(PROPERTY_CURRENT_WALKING_SPEED, null, this.currentWalkingSpeed);

	}

	public User withCurrentWalkingSpeed(long value) {
		this.setCurrentWalkingSpeed(value);
		return this;
	}

	// ================================================

	// walking speed in [km/h]

	public long getMinWalkingSpeed() {
		if (this.minWalkingSpeed == 0) {
			this.minWalkingSpeed = 1;
		}

		return this.minWalkingSpeed;
	}

	public void setMinWalkingSpeed(long value) {
		this.minWalkingSpeed = value;
	}

	public User withMinWalkingSpeed(long value) {
		this.setMinWalkingSpeed(value);
		return this;
	}

	// ================================================

	// walking speed in [km/h]

	public long getAvgWalkingSpeed() {
		if (this.avgWalkingSpeed == 0) {
			this.avgWalkingSpeed = 1;
		}

		return this.avgWalkingSpeed;
	}

	public void setAvgWalkingSpeed(long value) {
		this.avgWalkingSpeed = value;
	}

	public User withAvgWalkingSpeed(long value) {
		this.setAvgWalkingSpeed(value);
		return this;
	}

	// ================================================

	// walking speed in [km/h]

	public long getMaxWalkingSpeed() {
		if (this.maxWalkingSpeed == 0) {
			this.maxWalkingSpeed = 1;
		}

		return this.maxWalkingSpeed;
	}

	public void setMaxWalkingSpeed(long value) {
		this.maxWalkingSpeed = value;
	}

	public User withMaxWalkingSpeed(long value) {
		this.setMaxWalkingSpeed(value);
		return this;
	}

	// ================================================

	public int getBufferToNextConnection() {
		return this.bufferToNextConnection;
	}

	public void setBufferToNextConnection(int value) {
		this.bufferToNextConnection = value;
	}

	public User withBufferToNextConnection(int value) {
		this.setBufferToNextConnection(value);
		return this;
	}

	// ================================================

	public Set<Attribute> getConstraint() {
		return this.constraint;
	}

	public void setConstraint(Attribute... values) {

		for (Attribute value : values) {
			if (!this.constraint.contains(value))
				this.constraint.add(value);
		}
	}

	public User withConstraint(Attribute... values) {
		this.setConstraint(values);
		return this;
	}

	// ================================================

	public List<Disability> getDisability() {
		return this.disability;
	}

	public void setDisability(Disability... values) {

		for (Disability value : values) {
			if (!this.disability.contains(value))
				this.disability.add(value);
		}
	}

	public User withDisability(Disability... values) {
		this.setDisability(values);
		return this;
	}

	// ================================================

	public Set<Rating> getRatings() {
		if (this.rating == null)
			this.rating = new HashSet<Rating>();

		return this.rating;
	}

	public void setRatings(Rating... values) {
		if (this.rating == null)
			this.rating = new HashSet<Rating>();

		for (Rating value : values) {
			if (!this.rating.contains(value)) {
				this.rating.add(value);
				value.setUser(this);
			}
		}
	}

	public User withRatings(Rating... values) {
		this.setRatings(values);
		return this;
	}

	// ================================================

	public LinkedHashMap<Attribute, Double> getPreferences() {
		if (this.preferences == null)
			this.preferences = new LinkedHashMap<Attribute, Double>();

		return this.preferences;
	}

	public void setPreferences(LinkedHashMap<Attribute, Double> value) {
		this.preferences = value;
	}

	public User withPreferences(LinkedHashMap<Attribute, Double> value) {
		this.setPreferences(value);
		return this;
	}

	// ================================================

	public void setAttribute(Attribute... values) {
		if(values == null)
			return;
			
		for (Attribute value : values) {
			if (!super.hasAttribute(value))
				super.withAttribute(new AttributeTupel().withAttribute(value));
		}
		
	}

	public User withAttribute(Attribute... values) {
		this.setAttribute(values);
		return this;
	}

	public void removeAttribute(Attribute... values) {
		for (Attribute value : values) {
			if (!super.hasAttribute(value))
				super.removeAttribute(value);
		}
	}

	// ================================================

	@Override
	public String toString() {
		return this.id + " " + this.firstname + " " + this.lastname + " " + this.gender + " " + this.email + " "
				+ this.language;
	}
}