package com.doccuty.radarplus.view.controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;

import com.doccuty.radarplus.model.Attribute;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.model.User;
import com.doccuty.radarplus.persistence.AttributeDAO;
import com.doccuty.radarplus.view.listener.RegisterListener;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class RegisterController implements Initializable {

	@FXML
	TextField tf_firstname;

	@FXML
	TextField tf_lastname;

	@FXML
	DatePicker dp_dateOfBirth;

	@FXML
	ToggleGroup tgroup_gender;

	@FXML
	RadioButton rbtn_male;

	@FXML
	RadioButton rbtn_female;

	@FXML
	RadioButton rbtn_vegetarian;

	@FXML
	RadioButton rbtn_nonvegetarian;

	@FXML
	RadioButton rbtn_smoker;

	@FXML
	RadioButton rbtn_nonsmoker;

	@FXML
	Button btn_register;

	@FXML
	HBox hbox_systemPrompt;

	@FXML
	Label lbl_systemPrompt;

	private RecoTool app;

	AttributeDAO attributeDao;

	private RootController parentController;
	private RegisterListener listener;

	private ResourceBundle bundle;

	public RegisterController() {
		listener = new RegisterListener(this);
		attributeDao = new AttributeDAO();
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle) {

		this.bundle = bundle;

		this.hbox_systemPrompt.managedProperty().bind(this.hbox_systemPrompt.visibleProperty());

		// set default date for datepicker
		Calendar date = new GregorianCalendar();
		int year = date.get(Calendar.YEAR) - 18;
		int month = date.get(Calendar.MONTH);
		int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);

		dp_dateOfBirth.setValue(LocalDate.of(year, month, dayOfMonth));

		// set values for gender radio buttons
		Attribute male = this.attributeDao.findById(72);
		Attribute female = this.attributeDao.findById(73);
		rbtn_male.setUserData(male);
		rbtn_female.setUserData(female);

		Attribute smoker = this.attributeDao.findById(65);
		this.rbtn_smoker.setUserData(smoker);

		Attribute vegetarian = this.attributeDao.findById(32);
		this.rbtn_vegetarian.setUserData(vegetarian);
	}

	// ===================================================

	public void setListeners() {
		this.app.getCurrentUser().addPropertyChangeListener(User.PROPERTY_ID, listener);
		this.app.getCurrentUser().addPropertyChangeListener(User.PROPERTY_FIRSTNAME, listener);
		this.app.getCurrentUser().addPropertyChangeListener(User.PROPERTY_LASTNAME, listener);

		this.app.addPropertyChangeListener(RecoTool.PROPERTY_CURRENT_USER, listener);

		if (this.app.getCurrentUser() != null)
			this.fillWithUserData(this.app.getCurrentUser());
	}

	// ===================================================

	public RecoTool getStudyApp() {
		return this.app;
	}

	public void setStudyApp(RecoTool value) {
		this.app = value;
	}

	public RegisterController withStudyApp(RecoTool value) {
		this.setStudyApp(value);
		return this;
	}

	// ===================================================

	public RootController setParent() {
		return this.parentController;
	}

	public void setParent(RootController value) {
		this.parentController = value;
	}

	public RegisterController withParent(RootController value) {
		setParent(value);
		return this;
	}

	// ===================================================

	@FXML
	protected void changeFirstname(KeyEvent keyEvent) {
		this.app.getCurrentUser().setFirstname(tf_firstname.getText());
	}

	@FXML
	protected void changeLastname(KeyEvent keyEvent) {
		this.app.getCurrentUser().setLastname(tf_lastname.getText());
	}

	@FXML
	protected void changeDateOfBirth(ActionEvent e) {

		Calendar c = new GregorianCalendar();
		c.set(this.dp_dateOfBirth.getValue().getYear(), this.dp_dateOfBirth.getValue().getMonthValue(),
				this.dp_dateOfBirth.getValue().getDayOfMonth());

		this.app.getCurrentUser().setDateOfBirth(c);
	}

	@FXML
	protected void register(MouseEvent ev) {

		if (this.app.getCurrentUser().getFirstname() == null || this.app.getCurrentUser().getFirstname().length() < 1) {
			tf_firstname.requestFocus();
			hbox_systemPrompt.setVisible(true);
			lbl_systemPrompt.setText(this.bundle.getString("register.firstnameMissing"));
			return;
		}

		if (this.app.getCurrentUser().getLastname() == null || this.app.getCurrentUser().getLastname().length() < 1) {
			tf_lastname.requestFocus();
			hbox_systemPrompt.setVisible(true);
			lbl_systemPrompt.setText(this.bundle.getString("register.lastnameMissing"));
			return;
		}

		if (this.app.getCurrentUser().getDateOfBirth() == null) {
			Calendar c = new GregorianCalendar();
			c.set(this.dp_dateOfBirth.getValue().getYear(), this.dp_dateOfBirth.getValue().getMonthValue(),
					this.dp_dateOfBirth.getValue().getDayOfMonth());

			this.app.getCurrentUser().setDateOfBirth(c);
			hbox_systemPrompt.setVisible(true);
			lbl_systemPrompt.setText(this.bundle.getString("register.dateOfBirthMissing"));
		}

		/*
		if (!rbtn_female.isSelected() && !rbtn_male.isSelected()) {
			hbox_systemPrompt.setVisible(true);
			lbl_systemPrompt.setText(this.bundle.getString("register.genderMissing"));
			return;
		}
		 */
		
		if (rbtn_female.isSelected()) {
			app.getCurrentUser().setGender((Attribute) rbtn_female.getUserData());
		} else {
			app.getCurrentUser().setGender((Attribute) rbtn_male.getUserData());
		}

		if (rbtn_smoker.isSelected()) {
			this.app.getCurrentUser().withAttribute((Attribute) rbtn_smoker.getUserData());
		} else {
			this.app.getCurrentUser().removeAttribute((Attribute) rbtn_smoker.getUserData());
		}

		if (rbtn_vegetarian.isSelected()) {
			this.app.getCurrentUser().withAttribute((Attribute) rbtn_vegetarian.getUserData());
		} else {
			this.app.getCurrentUser().removeAttribute((Attribute) rbtn_vegetarian.getUserData());
		}

		hbox_systemPrompt.setVisible(true);
		this.app.saveUser();

		this.app.firePropertyChange(RecoTool.PROPERTY_CURRENT_USER, null, this.app.getCurrentUser());

		// change screen
		if (this.parentController != null) {
			hbox_systemPrompt.setVisible(false);
			this.parentController.showTrainingScreen();
		}
	}

	@FXML
	public void hideSystemPrompt(MouseEvent ev) {
		this.hbox_systemPrompt.setVisible(false);
	}

	public void fillWithUserData(User value) {

		this.tf_firstname.setText(value.getFirstname());
		this.tf_lastname.setText(value.getLastname());

		if (value.getDateOfBirth() != null) {
			Calendar date = value.getDateOfBirth();
			dp_dateOfBirth.setValue(
					LocalDate.of(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)));
		}

		if (value.getGender() != null) {
			if (value.getGender().compareTo((Attribute) this.rbtn_male.getUserData()) == 0) {
				this.rbtn_male.fire();
			} else if (value.getGender().compareTo((Attribute) this.rbtn_female.getUserData()) == 0) {
				this.rbtn_female.fire();
			}
		}

		if (value.hasAttribute((Attribute) this.rbtn_smoker.getUserData())) {
			this.rbtn_smoker.fire();
		} else {
			this.rbtn_nonsmoker.fire();
		}

		if (value.hasAttribute((Attribute) this.rbtn_vegetarian.getUserData())) {
			this.rbtn_vegetarian.fire();
		} else {
			this.rbtn_nonvegetarian.fire();
		}

		if(this.app.getCurrentUser().getId() > 0)
			this.btn_register.setText(this.bundle.getString("update"));
		else
			this.btn_register.setText(this.bundle.getString("register"));
	}
}
