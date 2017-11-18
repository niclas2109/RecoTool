package com.doccuty.radarplus.view.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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

public class ImpactEvaluationController implements Initializable {

	private final static Logger LOG = Logger.getLogger(ImpactEvaluationController.class);

	private final static String FILE_SUFFIX = "impact";
	private final static float FIT_HEIGHT = 15;

	private final static float MAP_WIDTH_IN_METERS = 266;

	// Evaluation screen

	@FXML
	VBox vbox_evaluation;

	@FXML
	HBox hbox_currentSelectedItemView;

	@FXML
	HBox hbox_usedItemList;

	@FXML
	Label lbl_timeToDepature;

	@FXML
	Button btn_applyButton;

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

		this.hbox_usedItemList.managedProperty().bind(this.hbox_usedItemList.visibleProperty());
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

		this.iv_map.fitWidthProperty().bind(p_map.widthProperty());
		// this.iv_map.fitHeightProperty().bind(p_map.heightProperty());
	}

	public void init() {
		if (this.app == null)
			return;

		if (this.app.getEvaluationRunning()) {
			this.app.stopEvaluation(false);
		}

		// Add optional handler for realtime user position updates
		if (this.app.getRealtimeUserPositionUpdateAccuracyEvaluationMap()) {
			this.app.getSetting().addPropertyChangeListener(Setting.PROPERTY_UPDATE_USER_POSITION, listener);
		}

		// init time
		this.app.getSetting().withGeoposition(this.app.getStartPosition())
				.withNextDestination(this.app.getEndPosition())
				.withCurrentDepartureTime(new Date(this.app.getStartTime().getTimeInMillis()
						+ this.app.getEvaluationDuration().toMillis() + this.app.getDelayDuration().toMillis()))
				.withEstimatedDepartureTime(new Date(this.app.getStartTime().getTimeInMillis()
						+ this.app.getEvaluationDuration().toMillis() + this.app.getDelayDuration().toMillis()))
				.withCurrentTime(this.app.getStartTime().getTime());

		this.lbl_timeToDepature.setText(Duration.ofMillis(this.app.getSetting().getTimeToDeparture()).toMinutes() + "");

		// User symbol
		this.userSymbol = new ImageView();
		this.userSymbol.setImage(userImage);
		this.userSymbol.setVisible(true);
		this.userSymbol.setFitHeight(FIT_HEIGHT);

		this.userSymbol.setPreserveRatio(true);
		this.userSymbol.setSmooth(true);

		this.userSymbol.xProperty()
				.bind(p_map.widthProperty().subtract(p_map.widthProperty().divide(MAP_WIDTH_IN_METERS)
						.multiply(this.app.getSetting().getGeoposition().getLatitude() + this.offsetX)));

		this.userSymbol.yProperty()
				.bind(p_map.widthProperty()
						.multiply(this.app.getSetting().getGeoposition().getLongitude() - this.offsetY)
						.divide(MAP_WIDTH_IN_METERS));
		this.p_map.getChildren().add(this.userSymbol);

		LinkedHashMap<Item, Double> map = null;

		if (this.app.getNumOfItemsToUse() == 0) {
			map = this.app.getRecommender().getContextBasedPostFilter().filterBySetting(this.app.getCurrentUser(),
					this.app.getSetting(), this.app.getRecommender().getOriginalRecommendations());
		} else {
			map = this.app.getRecommender().getOriginalRecommendations();
		}

		this.addImageViews(map);
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

		if (this.tv_items.getSelectionModel().getSelectedIndex() == this.itemList.size() - 1) {
			this.btn_applyButton.setDisable(false);
		}
	}

	@FXML
	public void applySelection(ActionEvent ev) {

		if (this.tv_items.getSelectionModel().getSelectedItem() == null)
			return;

		Item item = this.tv_items.getSelectionModel().getSelectedItem().getKey();
		this.usedItems.add(item);

		// Add new label to used item overview
		this.addItemToUsedItemList(item);

		// Update time and user position
		Date currentTime = null;
		if (this.app.getNumOfItemsToUse() > 0 && this.usedItems.size() <= this.app.getNumOfItemsToUse()) {
			currentTime = new Date(
					this.app.getSetting().getCurrentTime().getTime() + this.getOptimizedItemUsageDuration());
		} else {
			long walkingTime = ((long) item.getGeoposition().euclideanDistance(this.app.getSetting().getGeoposition()))
					/ this.app.getCurrentUser().getMinWalkingSpeed() * 3600;

			currentTime = new Date(this.app.getSetting().getCurrentTime().getTime()
					+ item.getEstimatedUsageDuration().toMillis() + walkingTime);
		}

		this.app.getSetting().withGeoposition(item.getGeoposition()).setCurrentTime(currentTime);

		this.lbl_timeToDepature.setText(Duration.ofMillis(this.app.getSetting().getTimeToDeparture()).toMinutes() + "");

		// Update item list
		LinkedHashMap<Item, Double> map = null;

		if (this.app.getMaxNumOfItems() == 0)
			map = this.app.updateBySetting(this.app.getCurrentUser());
		else
			map = this.app.getRecommender().getOriginalRecommendations();

		this.addImageViews(map);

		this.selectItem(null);
		if (this.itemList.size() == 0 || this.app.getSetting().getTimeToDeparture() <= 0) {

			String s = this.lbl_infoText.getText().replace("%firstname%", this.app.getCurrentUser().getFirstname());
			s = s.replace("%lastname%", this.app.getCurrentUser().getLastname());

			this.lbl_infoText.setText(s);

			this.vbox_info.setVisible(true);
			this.vbox_evaluation.setVisible(false);
		}

		// Update user position on map
		if (!this.app.getRealtimeUserPositionUpdateAccuracyEvaluationMap()) {
			this.updateUserPosition(this.app.getSetting().getGeoposition());
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
		this.userSymbol.xProperty()
				.bind(p_map.widthProperty().subtract(p_map.widthProperty().divide(MAP_WIDTH_IN_METERS)
						.multiply(this.app.getSetting().getGeoposition().getLatitude() + this.offsetX)));

		this.userSymbol.yProperty()
				.bind(p_map.widthProperty()
						.multiply(this.app.getSetting().getGeoposition().getLongitude() - this.offsetY)
						.divide(MAP_WIDTH_IN_METERS));

		this.userSymbol.toFront();
	}

	/**
	 * Add ImageViews for all items in itemMap.
	 * 
	 * @param itemMap
	 */
	private void addImageViews(LinkedHashMap<Item, Double> itemMap) {
		this.itemList.clear();

		p_map.getChildren().removeAll(imageViewList);
		imageViewList.clear();

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

			imageViewList.add(this.prepareImageView(iv_item, e));
		}

		p_map.getChildren().addAll(imageViewList);
	}

	private ImageView prepareImageView(ImageView iv_item, Entry<Item, Double> e) {
		iv_item.setFitHeight(FIT_HEIGHT);

		iv_item.setPreserveRatio(true);
		iv_item.setSmooth(true);

		iv_item.xProperty().bind(p_map.widthProperty().subtract(p_map.widthProperty().divide(MAP_WIDTH_IN_METERS)
				.multiply(e.getKey().getGeoposition().getLatitude() + this.offsetX)));

		iv_item.yProperty().bind(p_map.widthProperty()
				.multiply(e.getKey().getGeoposition().getLongitude() - this.offsetY).divide(MAP_WIDTH_IN_METERS));

		iv_item.getStyleClass().add("clickable");

		if (e != null) {
			iv_item.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent t) {

					int idx = itemList.indexOf(e);

					LOG.info(e.getKey().getGeoposition());

					if (idx >= 0) {
						tv_items.getSelectionModel().select(idx);
						tv_items.scrollTo(idx);
						selectItem(null);
					}
				}
			});
		}

		return iv_item;
	}

	public long getOptimizedItemUsageDuration() {
		long tUsage = 0;

		if (this.usedItems.size() < this.app.getNumOfItemsToUse()) {
			tUsage = this.app.getSetting().getTimeToDeparture() / (this.app.getNumOfItemsToUse());
			tUsage -= 200;
		} else {
			tUsage = this.app.getSetting().getTimeToDeparture();
		}

		return tUsage;
	}

	private void addItemToUsedItemList(Item item) {
		HBox hbox_usedItem = new HBox();
		hbox_usedItem.setSpacing(10);

		Label lbl_usedItem = new Label(item.getName());

		ImageView iv_removeItem = new ImageView();
		iv_removeItem.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				removeItemFromUsedItemList(item, hbox_usedItem);
			}

		});

		try {
			Image img = new Image(getClass().getClassLoader().getResource("icons/icons8-delete.png").openStream(), 16,
					16, true, false);
			iv_removeItem.setImage(img);
		} catch (IOException e) {
			e.printStackTrace();
		}

		hbox_usedItem.getChildren().addAll(lbl_usedItem, iv_removeItem);
		this.hbox_usedItemList.getChildren().add(hbox_usedItem);

		this.hbox_usedItemList.setVisible(true);
	}

	private void removeItemFromUsedItemList(Item item, HBox hbox_usedItem) {
		hbox_usedItemList.getChildren().remove(hbox_usedItem);
		usedItems.remove(item);

		// Reset usage time
		long time = this.app.getSetting().getCurrentTime().getTime();

		if (this.app.getNumOfItemsToUse() == 0)
			time -= item.getEstimatedUsageDuration().toMillis();
		else
			time -= this.app.getOptimizedItemUsageDuration();

		this.app.getSetting().setCurrentTime(new Date(time));

		if (usedItems.size() == 0) {
			hbox_usedItemList.setVisible(false);
		}
	}

	// ===============================================

	public RecoTool getApp() {
		return this.app;
	}

	public void setApp(RecoTool value) {
		this.app = value;
	}

	public ImpactEvaluationController withApp(RecoTool value) {
		this.setApp(value);
		return this;
	}

	// ===============================================

	public ImpactEvaluationController withParent(RootController value) {
		return this;
	}

	// ======================================

	public Stage getStage() {
		return this.stage;
	}

	public void setStage(Stage value) {
		this.stage = value;
	}

	public ImpactEvaluationController withStage(Stage value) {
		this.setStage(value);
		return this;
	}
}
