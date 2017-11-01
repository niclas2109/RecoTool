package com.doccuty.radarplus.view.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.model.User;
import com.doccuty.radarplus.persistence.UserDAO;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

public class SavedItemScoresController implements Initializable {

	@FXML
	TableView<File> tv_files;

	@FXML
	TableColumn<File, String> tc_fileName;

	@FXML
	TableColumn<File, String> tc_createdAt;

	@FXML
	TableColumn<File, String> tc_updatedAt;

	ObservableList<File> files = FXCollections.observableArrayList();

	@FXML
	Label lbl_introduction;

	@FXML
	Label lbl_userName;

	private Stage stage;
	private RecoTool app;

	private UserDAO userDao;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		userDao = new UserDAO();

		this.tv_files.setItems(files);

		tc_fileName.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<File, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<File, String> p) {
						return new SimpleStringProperty(p.getValue().getName());
					}
				});

		tc_createdAt.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<File, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<File, String> p) {

						DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String dateFormatted = null;

						BasicFileAttributes attr;
						try {
							attr = Files.readAttributes(p.getValue().toPath(), BasicFileAttributes.class);
							dateFormatted = formatter.format(attr.creationTime().toMillis());
						} catch (IOException e) {
							dateFormatted = "-";
						}

						return new SimpleStringProperty(dateFormatted);
					}
				});
		tc_updatedAt.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<File, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(TableColumn.CellDataFeatures<File, String> p) {
						DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String dateFormatted = formatter.format(p.getValue().lastModified());
						return new SimpleStringProperty(dateFormatted);
					}
				});

	}

	public void init() {
		this.files.addAll(this.app.getResultTracker().readResultDirectory());

		String txt = this.lbl_introduction.getText();
		txt = txt.replaceAll("%path%", this.app.getResultTracker().getPath()) + "/";
		this.lbl_introduction.setText(txt);
	}

	@FXML
	public void close(MouseEvent ev) {
		this.stage.hide();
	}

	@FXML
	public void selectFile(MouseEvent ev) {

		File file = this.tv_files.getSelectionModel().getSelectedItem();

		if (file == null)
			return;

		try {
			long id = Long.parseLong(file.getName().split("-")[0]);

			User user = userDao.findById(id);
			lbl_userName.setText(user.getFirstname() + " " + user.getLastname() + " (" + user.getAge() + ", "
					+ user.getGender().getAttribute() + ")");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void openFile(MouseEvent ev) {

		File selectedFile = this.tv_files.getSelectionModel().getSelectedItem();

		if (selectedFile == null)
			return;

		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(selectedFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// =====================================

	public RecoTool getApp() {
		return this.app;
	}

	public void setApp(RecoTool value) {
		this.app = value;
	}

	public SavedItemScoresController withApp(RecoTool value) {
		this.setApp(value);
		return this;
	}

	// =====================================

	public Stage getStage() {
		return this.stage;
	}

	public void setStage(Stage value) {
		this.stage = value;
	}

	public SavedItemScoresController withStage(Stage value) {
		this.setStage(value);
		return this;
	}

}