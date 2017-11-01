package com.doccuty.radarplus.view.controller;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.jboss.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.doccuty.radarplus.evaluation.ResultTracker;
import com.doccuty.radarplus.model.Attribute;
import com.doccuty.radarplus.model.SystemPrompt;
import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.model.Setting;
import com.doccuty.radarplus.network.RecoToolMqttServer;
import com.doccuty.radarplus.recommender.Recommender;
import com.doccuty.radarplus.view.listener.WoZListener;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class WoZController implements Initializable {

	private final static Logger LOG = Logger.getLogger(WoZController.class);

	@FXML
	Button btn_chooseFirstItem;

	@FXML
	Label lbl_chooseFirstItem;

	@FXML
	Button btn_chooseSecondItem;

	@FXML
	Label lbl_chooseSecondItem;

	@FXML
	Button btn_chooseThirdItem;

	@FXML
	Label lbl_chooseThirdItem;

	@FXML
	Button btn_choosePrevItem;

	@FXML
	Button btn_chooseNextItem;

	@FXML
	Button btn_detailFirstItem;

	@FXML
	Label lbl_detailFirstItem;

	@FXML
	Button btn_detailSecondItem;

	@FXML
	Label lbl_detailSecondItem;

	@FXML
	Button btn_detailThirdItem;

	@FXML
	Label lbl_detailThirdItem;

	@FXML
	Button btn_detailPrevItem;

	@FXML
	Button btn_detailNextItem;

	@FXML
	Label lbl_userName;

	@FXML
	Label lbl_selectedItem;

	@FXML
	TableView<Map.Entry<Attribute, Double>> tv_attribute;

	@FXML
	TableColumn<Map.Entry<Attribute, Double>, Long> tc_attributeID;

	@FXML
	TableColumn<Map.Entry<Attribute, Double>, String> tc_attribute;

	@FXML
	TableColumn<Map.Entry<Attribute, Double>, Double> tc_attributeScore;

	@FXML
	TableView<Map.Entry<Item, Double>> tv_item;

	@FXML
	TableColumn<Map.Entry<Item, Double>, Long> tc_itemID;

	@FXML
	TableColumn<Map.Entry<Item, Double>, String> tc_item;

	@FXML
	TableColumn<Map.Entry<Item, Double>, Double> tc_itemScore;

	@FXML
	ImageView iv_currentMode;

	@FXML
	Label lbl_distanceToNextDest;

	@FXML
	Label lbl_lastUsedItem;

	@FXML
	Label lbl_score;

	@FXML
	Label lbl_position;

	@FXML
	Label lbl_remainingTime;

	@FXML
	Button btn_generateNewRecommendations;

	@FXML
	Button btn_startNavigation;

	@FXML
	Button btn_itemUsed;

	@FXML
	Button btn_hideAll;

	// System Prompt

	@FXML
	HBox hbox_systemPrompt;

	@FXML
	ImageView iv_systemPromptIcon;

	@FXML
	Label lbl_systemPrompt;

	ObservableList<Map.Entry<Attribute, Double>> attributeData = FXCollections.observableArrayList();
	ObservableList<Map.Entry<Item, Double>> itemData = FXCollections.observableArrayList();

	private ResourceBundle bundle;

	RecoTool app;
	RootController parent;

	WoZListener listener;

	boolean detailViewEnabled;
	boolean selectionEnabled;

	int idxOfFirstItem;

	// Sets time display to either countdown or clock
	private boolean timeModeClock;

	public WoZController() {

		this.timeModeClock = false;

		this.listener = new WoZListener().withController(this);

		this.detailViewEnabled = false;
		this.selectionEnabled = false;
		this.idxOfFirstItem = 0;
	}

	public void initialize(URL location, ResourceBundle resources) {
		this.bundle = resources;

		this.tv_attribute.setItems(attributeData);
		this.tv_item.setItems(itemData);
	}

	public void init() {

		this.updateUserData();

		this.app.withCurrentItem(null);

		// Set Main App listeners
		this.app.addPropertyChangeListener(RecoTool.PROPERTY_RECOMMENDATIONS, listener);
		this.app.addPropertyChangeListener(RecoTool.PROPERTY_CURRENT_ITEM, listener);
		this.app.addPropertyChangeListener(RecoTool.PROPERTY_CURRENT_USER, listener);
		this.app.addPropertyChangeListener(RecoTool.PROPERTY_EVALUATION_DURATION, listener);

		this.app.addPropertyChangeListener(RecoTool.PROPERTY_ROUTE_CALCULATED, listener);
		this.app.addPropertyChangeListener(RecoTool.PROPERTY_NAVIGATION_FINISHED, listener);
		this.app.addPropertyChangeListener(RecoTool.PROPERTY_EVALUATION_PREPARED, listener);
		this.app.addPropertyChangeListener(RecoTool.PROPERTY_HIDE_ALL, listener);
		
		this.app.addPropertyChangeListener(RecoTool.PROPERTY_ITEM_USAGE_DONE, listener);

		this.app.addPropertyChangeListener(RecoTool.PROPERTY_SWITCH_MODE, listener);

		// ResultTracker listener
		this.app.getResultTracker().addPropertyChangeListener(ResultTracker.PROPERTY_SAVED_ITEM_SCORES, listener);
		this.app.getResultTracker().addPropertyChangeListener(ResultTracker.PROPERTY_FAILED_TO_SAVE_ITEM_SCORES,
				listener);

		// Setting listeners
		this.app.getSetting().addPropertyChangeListener(Setting.PROPERTY_UPDATE_USER_POSITION, listener);

		this.updateEvaluationDuration(this.app.getEvaluationDuration());

		int distance = 0;
		if (this.app.getUseGeocoordinates())
			distance = (int) (this.app.getEndPosition().distance(this.app.getSetting().getGeoposition()) * 1000);
		else
			distance = (int) (this.app.getEndPosition().euclideanDistance(this.app.getSetting().getGeoposition())
					* 1000);

		this.lbl_distanceToNextDest.setText(distance + " m");

		// set property value callbacks

		tc_attributeID.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Map.Entry<Attribute, Double>, Long>, ObservableValue<Long>>() {
					@Override
					public ObservableValue<Long> call(
							TableColumn.CellDataFeatures<Map.Entry<Attribute, Double>, Long> p) {
						return new SimpleLongProperty(p.getValue().getKey().getId()).asObject();
					}
				});

		tc_attribute.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Map.Entry<Attribute, Double>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(
							TableColumn.CellDataFeatures<Map.Entry<Attribute, Double>, String> p) {
						return new SimpleStringProperty(p.getValue().getKey().getAttribute());
					}
				});

		tc_attributeScore.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Map.Entry<Attribute, Double>, Double>, ObservableValue<Double>>() {
					@Override
					public ObservableValue<Double> call(
							TableColumn.CellDataFeatures<Map.Entry<Attribute, Double>, Double> p) {
						return new SimpleDoubleProperty((double) Math.round(p.getValue().getValue() * 1000) / 1000)
								.asObject();
					}
				});

		tc_itemID.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Map.Entry<Item, Double>, Long>, ObservableValue<Long>>() {
					@Override
					public ObservableValue<Long> call(TableColumn.CellDataFeatures<Map.Entry<Item, Double>, Long> p) {
						return new SimpleLongProperty(p.getValue().getKey().getId()).asObject();
					}
				});

		tc_item.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Map.Entry<Item, Double>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(
							TableColumn.CellDataFeatures<Map.Entry<Item, Double>, String> p) {
						return new SimpleStringProperty(p.getValue().getKey().getName());
					}
				});

		tc_itemScore.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Map.Entry<Item, Double>, Double>, ObservableValue<Double>>() {
					@Override
					public ObservableValue<Double> call(
							TableColumn.CellDataFeatures<Map.Entry<Item, Double>, Double> p) {
						return new SimpleDoubleProperty((double) Math.round(p.getValue().getValue() * 1000) / 1000)
								.asObject();
					}
				});

	}

	public void evaluationPrepared() {
		this.hideSystemPrompt(null);

		if (!this.btn_generateNewRecommendations.getStyleClass().contains("button-start"))
			this.btn_generateNewRecommendations.getStyleClass().add("button-start");

		this.lbl_lastUsedItem.setText("-");
		this.lbl_score.setText("-");
		this.lbl_position.setText("-");

		this.toggleStartNavigationButton();

		this.updateEvaluationDuration(this.app.getEvaluationDuration());
	}

	public void updateRecommendations() {

		// insert recommendations into list
		this.itemData.clear();
		for (Iterator<Entry<Item, Double>> it = this.app.getRecommender().getRecommendations().entrySet().iterator(); it
				.hasNext();) {
			Entry<Item, Double> e = it.next();
			this.itemData.add(e);
		}

		// insert calculated user preferences
		this.attributeData.clear();
		for (Iterator<Entry<Attribute, Double>> it = this.app.getCurrentUser().getPreferences().entrySet()
				.iterator(); it.hasNext();) {
			Entry<Attribute, Double> e = it.next();
			this.attributeData.add(e);
		}

		this.updateRecommendationButtons();
		this.app.setCurrentItem(null);

		LOG.info("Recommendations updated");
	}

	// apply buttons to items
	public void updateRecommendationButtons() {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				lbl_chooseFirstItem.setText(null);
				lbl_detailFirstItem.setText(null);
				btn_chooseFirstItem.setUserData(null);

				lbl_chooseSecondItem.setText(null);
				lbl_detailSecondItem.setText(null);
				btn_chooseSecondItem.setUserData(null);

				lbl_chooseThirdItem.setText(null);
				lbl_detailThirdItem.setText(null);
				btn_chooseThirdItem.setUserData(null);

				int i = 0;

				for (Iterator<Entry<Item, Double>> it = app.getRecommender().getRecommendations().entrySet()
						.iterator(); it.hasNext();) {

					Item item = it.next().getKey();

					if (i == idxOfFirstItem) {
						lbl_chooseFirstItem.setText(item.getName());
						lbl_detailFirstItem.setText(item.getName());
						btn_chooseFirstItem.setUserData(!item.equals(app.getCurrentItem()));
					} else if (i == idxOfFirstItem + 1) {
						lbl_chooseSecondItem.setText(item.getName());
						lbl_detailSecondItem.setText(item.getName());
						btn_chooseSecondItem.setUserData(!item.equals(app.getCurrentItem()));
					} else if (i == idxOfFirstItem + 2) {
						lbl_chooseThirdItem.setText(item.getName());
						lbl_detailThirdItem.setText(item.getName());
						btn_chooseThirdItem.setUserData(!item.equals(app.getCurrentItem()));
					} else if (i > idxOfFirstItem + 2) {
						break;
					}

					i++;
				}
			}
		});
	}

	@FXML
	public void setCurrentSelectedItem(Event ev) {

		if (this.tv_item.getSelectionModel().getSelectedItem() == null)
			return;

		Item item = (Item) this.tv_item.getSelectionModel().getSelectedItem().getKey();

		int idx = new ArrayList<Item>(this.app.getRecommender().getRecommendations().keySet()).indexOf(item);

		boolean select = false;
		if (idx < this.idxOfFirstItem || idx > this.idxOfFirstItem + 2) {
			this.idxOfFirstItem = ((int) idx / 3) * 3;
			this.detailViewEnabled = false;
			this.updateRecommendationButtons();
			this.currentItem(null);
			this.app.setCurrentItem(null);
			select = true;
		} else if (!item.equals(this.app.getCurrentItem())) {
			this.app.setCurrentItem(item);
		} else {
			this.app.setCurrentItem(null);
		}

		if (!this.app.getMQTTClient().getClient().isConnected())
			return;

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);

			JSONObject json = new JSONObject();
			json.put("action", (this.app.getCurrentItem() != null || select) ? RecoToolMqttServer.MQTT_CMD_SELECT_ITEM
					: RecoToolMqttServer.MQTT_CMD_UNSELECT_ITEM);
			json.put("item", new JSONObject(mapper.writeValueAsString(item)));

			this.app.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);
		} catch (JsonProcessingException | MqttException e1) {
			e1.printStackTrace();
		}
	}

	@FXML
	public void chooseFirstItem(MouseEvent ev) {
		this.selectionChanged(this.idxOfFirstItem + 0);
	}

	@FXML
	public void chooseSecondItem(MouseEvent ev) {
		this.selectionChanged(this.idxOfFirstItem + 1);
	}

	@FXML
	public void chooseThirdItem(MouseEvent ev) {
		this.selectionChanged(this.idxOfFirstItem + 2);
	}

	@FXML
	public void choosePrevious(MouseEvent ev) {
		int idx = new ArrayList<Item>(this.app.getRecommender().getRecommendations().keySet())
				.indexOf(this.app.getCurrentItem());

		if (idx <= this.idxOfFirstItem)
			return;

		this.selectionChanged(--idx);
	}

	@FXML
	public void chooseNext(MouseEvent ev) {
		int idx = new ArrayList<Item>(this.app.getRecommender().getRecommendations().keySet())
				.indexOf(this.app.getCurrentItem());

		if (idx >= this.idxOfFirstItem + 2 || idx + 1 >= this.app.getRecommender().getRecommendations().size())
			return;

		this.selectionChanged(++idx);
	}

	@FXML
	public void showDetailViewFirstItem(MouseEvent ev) {
		this.showDetailView(this.idxOfFirstItem + 0);
	}

	@FXML
	public void showDetailViewSecondItem(MouseEvent ev) {
		this.showDetailView(this.idxOfFirstItem + 1);
	}

	@FXML
	public void showDetailViewThirdItem(MouseEvent ev) {
		this.showDetailView(this.idxOfFirstItem + 2);
	}

	@FXML
	public void showPreviousDetailView(MouseEvent ev) {
		int idx = new ArrayList<Item>(this.app.getRecommender().getRecommendations().keySet())
				.indexOf(this.app.getCurrentItem());

		if (idx <= this.idxOfFirstItem)
			return;

		this.showDetailView(--idx);
	}

	@FXML
	public void showNextDetailView(MouseEvent ev) {
		int idx = new ArrayList<Item>(this.app.getRecommender().getRecommendations().keySet())
				.indexOf(this.app.getCurrentItem());

		if (idx >= this.idxOfFirstItem + 2 || idx + 1 >= this.app.getRecommender().getRecommendations().size())
			return;

		this.showDetailView(++idx);
	}

	@FXML
	public void startNavigation(MouseEvent ev) {

		if (this.app.getCurrentItem() == null || !this.app.getMQTTClient().getClient().isConnected())
			return;

		try {
			String s = null;
			if (!this.app.getCurrentItem().equals(this.app.getNavigationDestination())) {
				this.app.startNavigation(this.app.getCurrentItem());
				s = bundle.getString("controller.routeCalculation");
			} else {
				this.app.cancelNavigation();
				s = bundle.getString("controller.navigationCanceled");
			}

			s = s.replaceAll("%item%", this.app.getCurrentItem().getName());

			this.showSystemPrompt(SystemPrompt.SYSTEM_PROMPT_MODE_INFO, s);

		} catch (JSONException | MqttException | IOException e) {
			e.printStackTrace();
		}

		this.toggleStartNavigationButton();
	}

	public void calculatedRoute(Item item) {
		try {
			if (item != null) {
				this.showSystemPrompt(SystemPrompt.SYSTEM_PROMPT_MODE_INFO,
						"Die Route zu " + item.getName() + " wurde berechnet!");
			} else {
				this.showSystemPrompt(SystemPrompt.SYSTEM_PROMPT_MODE_ERROR, "Es konnte keine Route gefunden werden!");

				this.btn_startNavigation.setDisable(false);
				this.toggleStartNavigationButton();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// distance in [km]
	public void updateNavigationStatus(double distance) {
		try {
			this.showSystemPrompt(SystemPrompt.SYSTEM_PROMPT_MODE_INFO,
					this.app.getCurrentUser().getFirstname() + " ist noch " + Math.round(distance * 1000)
							+ " m Luftlinie von " + this.app.getNavigationDestination().getName() + " entfernt!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void finishedNavigation(Item item) {
		try {
			this.showSystemPrompt(SystemPrompt.SYSTEM_PROMPT_MODE_INFO,
					"Das Ziel " + item.getName() + " wurde erreicht!");

			this.btn_startNavigation.setDisable(false);
			this.toggleStartNavigationButton();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void generateNewRecommendations(MouseEvent ev) {

		this.btn_generateNewRecommendations.setDisable(true);

		this.selectionEnabled = false;
		this.detailViewEnabled = false;
		this.idxOfFirstItem = 0;

		if (!this.app.getEvaluationRunning()) {
			// start full filter process for recommendation initialization
			try {
				this.app.startEvaluation();
			} catch (MqttException | JsonProcessingException e) {

				try {
					this.showSystemPrompt(SystemPrompt.SYSTEM_PROMPT_MODE_ERROR, e.getMessage());
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				e.printStackTrace();
			}

			this.parent.setEvaluationRunning(true);
			this.btn_generateNewRecommendations.getStyleClass().remove("button-start");
		} else {
			// update recommendations by setting
			this.app.updateBySetting(this.app.getCurrentUser());
		}

		this.btn_generateNewRecommendations.setDisable(false);
	}

	@FXML
	public void itemUsageDone(MouseEvent ev) {
		this.app.itemUsed();
	}

	public void itemUsageDone(Item item) {
		this.lbl_lastUsedItem.setText(item.getName());
		this.lbl_score
				.setText((Math.round(this.app.getRecommender().getRecommendations().get(item) * 1000) / 1000.0) + "");

		this.lbl_position.setText(
				(new ArrayList<Item>(this.app.getRecommender().getRecommendations().keySet()).indexOf(item) + 1) + "");
	}

	@FXML
	public void hideAll(MouseEvent ev) {
		try {
			this.app.hideAll();
			this.toggleHideAllButton();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void toggleHideAllButton() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				btn_hideAll.setText((app.getHideAll()) ? bundle.getString("controller.hideAll")
						: bundle.getString("controller.showAll"));
			}
		});
	}

	public void toggleStartNavigationButton() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (app.getNavigationDestination() == null) {
					btn_startNavigation.setText(bundle.getString("controller.startNavigation"));
					btn_startNavigation.getStyleClass().removeAll("btn-cancel-navigation");
				} else {
					btn_startNavigation.setText(bundle.getString("controller.stopNavigation"));
					btn_startNavigation.getStyleClass().add("btn-cancel-navigation");
				}

			}
		});
	}

	@FXML
	public void sendErrorAlert(MouseEvent ev) {
		this.parent.openSystemPromptController(null);
	}

	// Show system prompt
	public void showSystemPrompt(String mode, String message) throws IOException {

		this.hbox_systemPrompt.getStyleClass().removeAll(SystemPrompt.SYSTEM_PROMPT_MODE_INFO,
				SystemPrompt.SYSTEM_PROMPT_MODE_ERROR, SystemPrompt.SYSTEM_PROMPT_MODE_SUCCESS);
		this.hbox_systemPrompt.setVisible(true);

		if (mode.compareTo(SystemPrompt.SYSTEM_PROMPT_MODE_INFO) == 0) {
			this.hbox_systemPrompt.getStyleClass().add(SystemPrompt.SYSTEM_PROMPT_MODE_INFO);
			Image img = new Image(getClass().getClassLoader().getResource("icons/icons8-Info-26.png").openStream());
			this.iv_systemPromptIcon.setImage(img);
		} else if (mode.compareTo(SystemPrompt.SYSTEM_PROMPT_MODE_ERROR) == 0) {
			this.hbox_systemPrompt.getStyleClass().add(SystemPrompt.SYSTEM_PROMPT_MODE_ERROR);
			Image img = new Image(getClass().getClassLoader().getResource("icons/icons8-Error.png").openStream());
			this.iv_systemPromptIcon.setImage(img);
		} else if (mode.compareTo(SystemPrompt.SYSTEM_PROMPT_MODE_SUCCESS) == 0) {
			this.hbox_systemPrompt.getStyleClass().add(SystemPrompt.SYSTEM_PROMPT_MODE_SUCCESS);
			Image img = new Image(getClass().getClassLoader().getResource("icons/icons8-Info-26.png").openStream());
			this.iv_systemPromptIcon.setImage(img);
		}

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				lbl_systemPrompt.setText(message);
			}
		});
	}

	public void hideSystemPrompt(MouseEvent ev) {
		this.hbox_systemPrompt.setVisible(false);
	}

	// Update user data
	public void updateUserData() {
		StringBuilder s = new StringBuilder();

		if (this.app.getCurrentUser().getFirstname() != null || this.app.getCurrentUser().getLastname() != null) {
			s.append(this.app.getCurrentUser().getFirstname() + " " + this.app.getCurrentUser().getLastname());
		} else {
			lbl_userName.setText("-");
			return;
		}

		if (this.app.getCurrentUser().getAge() > 0)
			s.append(" (" + this.app.getCurrentUser().getAge() + ")");

		lbl_userName.setText(s.toString());
	}

	// distance in [km]
	public void updateDistanceToEndPosition(double distance) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				lbl_distanceToNextDest.setText(Math.round(distance * 1000) + " m");
			}
		});
	}

	// ===============================================

	@FXML
	public void toggleTimerMode(MouseEvent ev) {
		this.timeModeClock = !this.timeModeClock;
	}

	// ===============================================

	public void updateEvaluationDuration(Duration value) {

		if (value == null) {
			LOG.info("Null duration received");
			return;
		}

		long seconds = value.toMillis() / 1000;

		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				DateFormat df = new SimpleDateFormat("HH:mm:ss");
				df.setTimeZone(TimeZone.getDefault());

				String time = (timeModeClock) ? df.format(app.getSetting().getCurrentTime())
						: String.format("%d:%02d:%02d", (seconds / (60 * 60)) % 24, (seconds / 60) % 60,
								(seconds % 60));

				lbl_remainingTime.setText(time);
			}
		});

		if (value.toMillis() <= 0) {
			stopEvaluation();
			return;
		}
	}

	public void stopEvaluation() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				btn_generateNewRecommendations.setDisable(false);

				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Evaluation abgeschlossen");
				alert.setHeaderText(null);
				alert.setContentText(
						"Die Evaluation wurde beendet! Soll das Setting inkl. aller verwendeten Items gelöscht werden?");

				ButtonType cancelButton = new ButtonType("Setting beibehalten", ButtonData.NO);

				alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL, cancelButton);

				Optional<ButtonType> option = alert.showAndWait();
				if (option.get() == ButtonType.OK) {
					app.stopEvaluation(true);
				} else if (option.get() == cancelButton) {
					app.stopEvaluation(false);
				}

			}
		});
	}

	public void switchRecommendationMode(String mode) {
		try {
			String resource = "icons/"
					+ ((mode.compareTo(Recommender.PROPERTY_ABIDANCE_MODE) == 0) ? "abidance-mode.png"
							: "efficiency-mode.png");

			Image img = new Image(getClass().getClassLoader().getResource(resource).openStream());
			this.iv_currentMode.setImage(img);

			String m = (mode.compareTo(Recommender.PROPERTY_ABIDANCE_MODE) == 0) ? "Aufenthalts" : "Effizienz";
			this.showSystemPrompt(SystemPrompt.SYSTEM_PROMPT_MODE_INFO, "Der " + m + "-Modus wurde aktiviert!");
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean disable = mode.compareTo(Recommender.PROPERTY_EFFICIENCY_MODE) == 0;

		this.btn_generateNewRecommendations.setDisable(disable);
		this.btn_startNavigation.setDisable(disable);
		this.btn_itemUsed.setDisable(disable);
		this.btn_hideAll.setDisable(disable);
	}

	// ===============================================

	private void selectionChanged(int idx) {

		if (this.detailViewEnabled) {
			this.showDetailView(idx);
			return;
		}

		Item item = this.getKItemFromRecommendations(idx);

		if (item == null) {
			this.selectionEnabled = false;
			return;
		}

		if (!item.equals(this.app.getCurrentItem())) {
			this.selectionEnabled = true;
		} else {
			this.selectionEnabled = false;
		}

		this.app.setCurrentItem((this.selectionEnabled) ? item : null);

		if (!this.app.getMQTTClient().getClient().isConnected())
			return;

		String action = (this.selectionEnabled) ? RecoToolMqttServer.MQTT_CMD_SELECT_ITEM
				: RecoToolMqttServer.MQTT_CMD_UNSELECT_ITEM;

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);

			JSONObject json = new JSONObject();
			json.put("action", action);
			json.put("item", new JSONObject(mapper.writeValueAsString(item)));

			this.app.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);

		} catch (JsonProcessingException | MqttException e1) {
			e1.printStackTrace();
		}
	}

	public void showDetailView(int idx) {
		Item item = this.getKItemFromRecommendations(idx);

		if (item == null) {
			this.selectionEnabled = false;
			return;
		}

		if (!item.equals(this.app.getCurrentItem())) {
			this.detailViewEnabled = true;
			this.app.setCurrentItem(item);
		} else if (!this.detailViewEnabled) {
			this.detailViewEnabled = true;
			this.currentItem(item);
		} else {
			this.detailViewEnabled = false;
			this.currentItem(item);
		}

		if (!this.app.getMQTTClient().getClient().isConnected())
			return;

		String action = (this.detailViewEnabled) ? RecoToolMqttServer.MQTT_CMD_SHOW_DETAIL_VIEW
				: RecoToolMqttServer.MQTT_CMD_HIDE_DETAIL_VIEW;

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);

			JSONObject json = new JSONObject();
			json.put("action", action);
			json.put("item", new JSONObject(mapper.writeValueAsString(item)));

			this.app.getMQTTClient().send(json.toString(), RecoToolMqttServer.PROPERTY_DATA_GLASSES_TOPIC);

			LOG.info(action + " " + item.toString());

		} catch (JsonProcessingException | MqttException e1) {
			e1.printStackTrace();
		}
	}

	public Item getKItemFromRecommendations(int k) {
		++k;

		Item item = null;

		for (Iterator<Item> it = this.app.getRecommender().getRecommendations().keySet().iterator(); it.hasNext()
				&& k > 0; k--) {
			item = (Item) it.next();
		}

		return item;
	}

	// update view by currently selected item
	public void currentItem(Item value) {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				String s = "-";
				if (value != null) {
					s = value.getName();
					btn_startNavigation.setDisable(false);
					btn_itemUsed.setDisable(false);

					Item dest = app.getNavigationDestination();
					if (dest == null || value.equals(dest)) {
						btn_startNavigation.setDisable(false);
						btn_itemUsed.setDisable(false);
					} else {
						btn_startNavigation.setDisable(true);
						btn_itemUsed.setDisable(true);
					}

				} else {
					btn_startNavigation.setDisable(true);
					btn_itemUsed.setDisable(true);
				}

				btn_hideAll.setDisable(false);
				lbl_selectedItem.setText(s);

				int idx = new ArrayList<Item>(app.getRecommender().getRecommendations().keySet()).indexOf(value);

				btn_chooseFirstItem.setUserData(idx == idxOfFirstItem);
				btn_chooseSecondItem.setUserData(idx == idxOfFirstItem + 1);
				btn_chooseThirdItem.setUserData(idx == idxOfFirstItem + 2);

				btn_detailFirstItem.setUserData((boolean) btn_chooseFirstItem.getUserData() && detailViewEnabled);
				btn_detailSecondItem.setUserData((boolean) btn_chooseSecondItem.getUserData() && detailViewEnabled);
				btn_detailThirdItem.setUserData((boolean) btn_chooseThirdItem.getUserData() && detailViewEnabled);

				highlightButton(btn_detailFirstItem, btn_chooseFirstItem, btn_detailSecondItem, btn_chooseSecondItem,
						btn_detailThirdItem, btn_chooseThirdItem);
			}
		});
	}

	private void highlightButton(Button... button) {
		for (Button btn : button) {
			if ((boolean) btn.getUserData()) {
				btn.getStyleClass().add("button-active");
			} else {
				btn.getStyleClass().removeAll("button-active");
			}
		}
	}

	public void scoresSavedToFile(String mode, String value) {
		String prompt = this.bundle.getString("fileSaved");
		prompt = prompt.replaceAll("%filename%", value);

		try {
			this.showSystemPrompt(mode, prompt);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ===============================================

	public RecoTool getStudyApp() {
		return this.app;
	}

	public void setStudyApp(RecoTool value) {
		this.app = value;
	}

	public WoZController withStudyApp(RecoTool value) {
		this.setStudyApp(value);
		return this;
	}

	// ===============================================

	public WoZController withParent(RootController value) {
		this.parent = value;
		return this;
	}
}
