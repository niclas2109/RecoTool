package com.doccuty.radarplus.view.controller;

import java.io.ByteArrayInputStream;
import java.util.List;

import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.trainer.Trainer;
import com.doccuty.radarplus.view.listener.TrainingListener;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class TrainingController {

	@FXML
	ImageView iv_leftItem;

	@FXML
	Button btn_left;

	@FXML
	Label lbl_left;

	@FXML
	Label text_descriptionLeft;

	@FXML
	ImageView iv_rightItem;

	@FXML
	Button btn_right;

	@FXML
	Label lbl_right;

	@FXML
	Label text_descriptionRight;

	TrainingListener listener;

	RecoTool app;

	private RootController parentController;

	public TrainingController() {

	}

	public void initialize() {
		listener = new TrainingListener(this);

		// little hack to disable default focus
		Platform.runLater(() -> lbl_right.requestFocus());
	}

	// ===================================================

	public void setListeners() {
		this.app.getTrainer().addPropertyChangeListener(Trainer.PROPERTY_NEXT_PAIR, listener);
		this.app.getTrainer().createTrainingPairs();
	}

	/**
	 * handle click events on buttons
	 * 
	 * @param ev
	 */

	@FXML
	public void clickedLeft(MouseEvent ev) {
		this.app.getTrainer().ratedForItem((Item) btn_left.getUserData(), (Item) btn_right.getUserData());
		this.app.getTrainer().nextPair();
	}

	@FXML
	public void clickedRight(MouseEvent ev) {
		this.app.getTrainer().ratedForItem((Item) btn_right.getUserData(), (Item) btn_left.getUserData());
		this.app.getTrainer().nextPair();
	}

	/**
	 * Update buttons for next training round with given items
	 * 
	 * @param items
	 */

	public void updateButtons(List<Item> items) {

		if (items == null || items.size() == 0) {
			this.trainingIsDone();
			return;
		}

		try {
			btn_left.setUserData(items.get(0));
			btn_right.setUserData(items.get(1));

			lbl_left.setText(items.get(0).getName());
			lbl_right.setText(items.get(1).getName());

			text_descriptionLeft.setText(items.get(0).getDescription());
			text_descriptionRight.setText(items.get(1).getDescription());

			Image imgLeft = new Image(new ByteArrayInputStream(items.get(0).getImage().getImage()));
			iv_leftItem.setImage(imgLeft);

			Image imgRight = new Image(new ByteArrayInputStream(items.get(1).getImage().getImage()));
			iv_rightItem.setImage(imgRight);

			Platform.runLater(() -> lbl_right.requestFocus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Training is done
	 */

	public void trainingIsDone() {
		this.parentController.showInfoScreen().personalizeText(this.app.getCurrentUser());
	}

	// ===================================================

	public RecoTool getStudyApp() {
		return this.app;
	}

	public void setStudyApp(RecoTool value) {
		this.app = value;
	}

	public TrainingController withStudyApp(RecoTool value) {
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

	public TrainingController withParent(RootController value) {
		setParent(value);
		return this;
	}
}