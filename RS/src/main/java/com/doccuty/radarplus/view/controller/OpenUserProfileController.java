package com.doccuty.radarplus.view.controller;

import java.util.Date;
import java.util.List;

import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.model.User;
import com.doccuty.radarplus.persistence.UserDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class OpenUserProfileController {

	@FXML
	TableView<User> tv_user;

	@FXML
	TableColumn<User, String> tc_firstname;

	@FXML
	TableColumn<User, String> tc_lastname;

	@FXML
	TableColumn<User, Date> tc_lastUpdated;

	@FXML
	TableColumn<User, Date> tc_createdAt;

	ObservableList<User> userData;

	UserDAO userDao = new UserDAO();

	private Stage stage;

	private RecoTool app;

	private User selectedUser;

	public void initialize() {

		tc_firstname.setCellValueFactory(new PropertyValueFactory<User, String>("firstname"));
		tc_lastname.setCellValueFactory(new PropertyValueFactory<User, String>("lastname"));
		tc_lastUpdated.setCellValueFactory(new PropertyValueFactory<User, Date>("dateOfLastUpdate"));
		tc_createdAt.setCellValueFactory(new PropertyValueFactory<User, Date>("dateOfRegistration"));

		List<User> user = userDao.findAll();

		userData = FXCollections.observableArrayList();
		userData.setAll(user);

		this.tv_user.setItems(userData);
		
		this.selectedUser = null;
	}

	@FXML
	public void setCurrentSelectedUser(Event ev) {
		this.selectedUser = this.tv_user.getSelectionModel().getSelectedItem();
	}
	
	@FXML
	public void openUserProfile(Event ev) {
		if(this.selectedUser != null)
			this.app.setCurrentUser(this.selectedUser);

		this.stage.hide();
	}
	
	@FXML
	public void close(MouseEvent ev) {
		this.stage.hide();
	}

	// ======================================

	public void setStage(Stage value) {
		this.stage = value;
	}

	public OpenUserProfileController withStage(Stage value) {
		this.setStage(value);
		return this;
	}

	// ======================================

	public void setApp(RecoTool value) {
		this.app = value;
	}

	public OpenUserProfileController withApp(RecoTool value) {
		this.setApp(value);
		return this;
	}
}
