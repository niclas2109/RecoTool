package com.doccuty.radarplus.view.controller;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.jboss.logging.Logger;

import com.doccuty.radarplus.model.Geoposition;
import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.model.Setting;
import com.doccuty.radarplus.view.listener.AccuracyEvaluationListener;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class AccuracyEvaluationController implements Initializable {

	private final static Logger LOG = Logger.getLogger(AccuracyEvaluationController.class);

	private final static String FILE_SUFFIX = "accurarcy";
	private final static float FIT_HEIGHT = 15;

	// Evaluation screen

	@FXML
	VBox vbox_evaluation;

	@FXML
	HBox hbox_currentSelectedItemView;

	@FXML
	Label lbl_timeToDepature;

	@FXML
	ImageView iv_itemImage;

	@FXML
	Label lbl_itemName;

	@FXML
	Label lbl_itemDescription;

	@FXML
	TableView<Map.Entry<Item, Double>> tv_items;

	@FXML
	TableColumn<Map.Entry<Item, Double>, String> tc_itemName;

	@FXML
	TableColumn<Map.Entry<Item, Double>, Double> tc_itemDistance;

	ObservableList<Map.Entry<Item, Double>> itemList = FXCollections.observableArrayList();

	ObservableList<Item> usedItems = FXCollections.observableArrayList();

	// Info screen

	@FXML
	VBox vbox_info;

	@FXML
	Label lbl_infoText;

	@FXML
	ListView<Item> lv_usedItems;

	@FXML
	Pane p_map;

	@FXML
	ImageView iv_map;

	AccuracyEvaluationListener listener;

	private RecoTool app;
	private Stage stage;

	List<ImageView> imageViewList;
	ImageView userSymbol;

	// Item symbols
	Image itemSymbol;
	Image selectedItemImage;
	Image userImage;

	double scale;
	double offsetX;
	double offsetY;

	public void initialize(URL location, ResourceBundle resources) {

		this.listener = new AccuracyEvaluationListener(this);

		this.imageViewList = new ArrayList<ImageView>();
		itemSymbol = new Image(getClass().getClassLoader().getResource("images/item-symbol.png").toString());
		userImage = new Image(getClass().getClassLoader().getResource("images/user-symbol.png").toString());
		selectedItemImage = new Image(
				getClass().getClassLoader().getResource("images/active-item-symbol.png").toString());

		offsetX = 3;
		offsetY = -2.5;

		this.tv_items.managedProperty().bind(this.tv_items.visibleProperty());
		this.iv_itemImage.managedProperty().bind(this.iv_itemImage.visibleProperty());

		this.vbox_info.managedProperty().bind(this.vbox_info.visibleProperty());
		this.vbox_evaluation.managedProperty().bind(this.vbox_evaluation.visibleProperty());

		this.hbox_currentSelectedItemView.managedProperty().bind(this.hbox_currentSelectedItemView.visibleProperty());
		this.hbox_currentSelectedItemView.setVisible(false);

		this.tc_itemName.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Map.Entry<Item, Double>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(
							TableColumn.CellDataFeatures<Map.Entry<Item, Double>, String> p) {
						return new SimpleStringProperty(p.getValue().getKey().getName());
					}
				});

		this.tc_itemDistance.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Map.Entry<Item, Double>, Double>, ObservableValue<Double>>() {
					@Override
					public ObservableValue<Double> call(
							TableColumn.CellDataFeatures<Map.Entry<Item, Double>, Double> p) {
						return new SimpleDoubleProperty(Math.round(p.getValue().getValue() * 1000)).asObject();
					}
				});

		this.tv_items.setItems(itemList);
		this.lv_usedItems.setItems(this.usedItems);

		// Calculate pixel per meter
		scale = this.iv_map.getFitWidth() / 266.0;
	}

	public void init() {
		if (this.app == null)
			return;

		if (this.app.getRecommender().getOriginalRecommendations().size() == 0) {
			this.app.generateRecommendations();
		}

		// Add optional handler for realtime user position updates
		if (this.app.getRealtimeUserPositionUpdateAccuracyEvaluationMap()) {
			this.app.getSetting().addPropertyChangeListener(Setting.PROPERTY_UPDATE_USER_POSITION, listener);
		}

		// init time
		this.app.getSetting().withGeoposition(this.app.getStartPosition())
				.withNextDestination(this.app.getEndPosition())
				.withCurrentDepartureTime(new Date(
						this.app.getStartTime().getTimeInMillis() + this.app.getEvaluationDuration().toMillis()))
				.withEstimatedDepartureTime(this.app.getSetting().getCurrentDepartureTime());

		this.lbl_timeToDepature.setText(Duration.ofMillis(this.app.getSetting().getTimeToDeparture()).toMinutes() + "");

		LinkedHashMap<Item, Double> map = this.app.getRecommender().getContextBasedPostFilter().filterBySetting(
				this.app.getCurrentUser(), this.app.getSetting(),
				this.app.getRecommender().getOriginalRecommendations());

		this.addImageViews(map);

		// User symbol
		this.userSymbol = new ImageView();
		this.userSymbol.setImage(userImage);
		this.userSymbol.setVisible(true);
		this.userSymbol.setFitHeight(FIT_HEIGHT);

		this.userSymbol.setPreserveRatio(true);
		this.userSymbol.setSmooth(true);

		this.userSymbol.setX(this.iv_map.getFitWidth() - this.app.getSetting().getGeoposition().getLatitude() * scale
				- this.offsetX);
		this.userSymbol.setY(this.app.getSetting().getGeoposition().getLongitude() * scale - this.offsetY * scale);

		imageViewList.add(this.userSymbol);

		// Add all images to map
		p_map.getChildren().addAll(imageViewList);
	}

	@FXML
	public void selectItem(Event ev) {

		if (this.tv_items.getSelectionModel().getSelectedItem() == null) {
			this.hbox_currentSelectedItemView.setVisible(false);
			return;
		}

		Item item = this.tv_items.getSelectionModel().getSelectedItem().getKey();
		this.lbl_itemName.setText(item.getName());
		this.lbl_itemDescription.setText(item.getDescription());

		this.hbox_currentSelectedItemView.setVisible(true);

		if (item.getImage() != null && item.getImage().getImage() != null) {
			this.iv_itemImage.setImage(new Image(new ByteArrayInputStream(item.getImage().getImage())));
			this.iv_itemImage.setVisible(true);
		} else {
			this.iv_itemImage.setVisible(false);
			LOG.info("No image available for " + item);
		}

		int i = 0;
		for (ImageView iv : imageViewList) {
			if (!iv.equals(this.userSymbol) && i == this.tv_items.getSelectionModel().getSelectedIndex())
				iv.setImage(this.selectedItemImage);
			else if (!iv.equals(this.userSymbol))
				iv.setImage(this.itemSymbol);

			i++;
		}

	}

	@FXML
	public void applySelection(ActionEvent ev) {

		Item item = this.tv_items.getSelectionModel().getSelectedItem().getKey();

		long walkingTime = ((long) item.getGeoposition().euclideanDistance(this.app.getSetting().getGeoposition()))
				/ this.app.getCurrentUser().getAvgWalkingSpeed() * 3600;

		this.usedItems.add(item);

		Date currentTime = null;

		if (this.app.getMaxNumOfItemsToUse() > 0 && this.usedItems.size() <= this.app.getMaxNumOfItemsToUse()) {

			currentTime = new Date(this.app.getSetting().getCurrentDepartureTime().getTime()
					+ (this.app.getEvaluationDuration().toMillis() / this.app.getMaxNumOfItemsToUse()));

		} else if (this.app.getMaxNumOfItemsToUse() > 0 && this.usedItems.size() == this.app.getMaxNumOfItemsToUse()) {
			currentTime = this.app.getSetting().getCurrentDepartureTime();
		} else {
			currentTime = new Date(this.app.getSetting().getCurrentTime().getTime()
					+ item.getEstimatedUsageDuration().toMillis() + walkingTime);
		}

		this.app.getSetting().withGeoposition(item.getGeoposition()).setCurrentTime(currentTime);

		// Update user position on map
		if (!this.app.getRealtimeUserPositionUpdateAccuracyEvaluationMap()) {
			this.updateUserPosition(this.app.getSetting().getGeoposition());
		}

		this.lbl_timeToDepature.setText(Duration.ofMillis(this.app.getSetting().getTimeToDeparture()).toMinutes() + "");

		// Update item list
		this.itemList.clear();

		LinkedHashMap<Item, Double> map = this.app.updateBySetting(this.app.getCurrentUser());

		this.addImageViews(map);

		this.selectItem(null);

		if (this.itemList.size() == 0 || this.app.getSetting().getTimeToDeparture() <= 0) {

			String s = this.lbl_infoText.getText().replace("%firstname%", this.app.getCurrentUser().getFirstname());
			s = s.replace("%lastname%", this.app.getCurrentUser().getLastname());

			this.lbl_infoText.setText(s);

			this.vbox_info.setVisible(true);
			this.vbox_evaluation.setVisible(false);
		}
	}

	@FXML
	public void close(MouseEvent ev) {

		String filename = FILE_SUFFIX + "-"
				+ this.app.getResultTracker().getNumOfAccuracyEvaluationsOfUser(this.app.getCurrentUser(), FILE_SUFFIX)
				+ ".csv";
		filename = this.app.getCurrentUser().getId() + "-" + filename;

		this.app.getResultTracker().writeItemListToCSV(this.usedItems, filename);

		this.stage.hide();
	}

	public void updateUserPosition(Geoposition value) {
		this.userSymbol.setX(this.iv_map.getFitWidth() - value.getLatitude() * scale - this.offsetX * scale);
		this.userSymbol.setY(value.getLongitude() * scale - this.offsetY * scale);
	}

	private void addImageViews(LinkedHashMap<Item, Double> itemMap) {

		// Add Item Symbol
		for (Iterator<Entry<Item, Double>> it = itemMap.entrySet().iterator(); it.hasNext();) {
			Entry<Item, Double> e = it.next();

			// Replace score with distance in [m]
			if (this.app.getUseGeocoordinates())
				e.setValue(e.getKey().getGeoposition().distance(this.app.getSetting().getGeoposition()));
			else
				e.setValue(e.getKey().getGeoposition().euclideanDistance(this.app.getSetting().getGeoposition()));

			this.itemList.add(e);

			ImageView iv_item = new ImageView();
			iv_item.setImage(itemSymbol);
			iv_item.setFitHeight(FIT_HEIGHT);

			iv_item.setPreserveRatio(true);
			iv_item.setSmooth(true);

			iv_item.setX(this.iv_map.getFitWidth() - e.getKey().getGeoposition().getLatitude() * scale
					- this.offsetX * scale);
			iv_item.setY(e.getKey().getGeoposition().getLongitude() * scale - this.offsetY * scale);

			imageViewList.add(iv_item);
		}
	}

	// ===============================================

	public RecoTool getApp() {
		return this.app;
	}

	public void setApp(RecoTool value) {
		this.app = value;
	}

	public AccuracyEvaluationController withApp(RecoTool value) {
		this.setApp(value);
		return this;
	}

	// ===============================================

	public AccuracyEvaluationController withParent(RootController value) {
		return this;
	}

	// ======================================

	public Stage getStage() {
		return this.stage;
	}

	public void setStage(Stage value) {
		this.stage = value;
	}

	public AccuracyEvaluationController withStage(Stage value) {
		this.setStage(value);
		return this;
	}
}
