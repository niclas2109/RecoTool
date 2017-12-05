package com.doccuty.radarplus.view.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Duration;

import com.doccuty.radarplus.evaluation.ResultTracker;
import com.doccuty.radarplus.model.Geoposition;
import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.model.RecoTool.UsedItem;
import com.doccuty.radarplus.model.Setting;
import com.doccuty.radarplus.model.SystemPrompt;
import com.doccuty.radarplus.view.controller.WoZController;

public class WoZListener implements PropertyChangeListener {

	WoZController controller;

	public WoZListener() {

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_EVALUATION_DURATION) == 0) {
			this.controller.updateEvaluationDuration((Duration) evt.getNewValue());
		} else if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_RECOMMENDATIONS) == 0) {
			this.controller.updateRecommendations();
		} else if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_CURRENT_ITEM) == 0) {
			this.controller.currentItem((Item) evt.getNewValue());
		} else if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_CURRENT_USER) == 0) {
			this.controller.updateUserData();
		} else if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_SWITCH_MODE) == 0) {
			this.controller.switchRecommendationMode((String) evt.getNewValue());
		} else if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_ROUTE_CALCULATED) == 0) {
			this.controller.calculatedRoute((Item) evt.getNewValue());
		} else if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_NAVIGATION_FINISHED) == 0
				&& evt.getNewValue() != null) {
			this.controller.finishedNavigation((Item) evt.getNewValue());
		} else if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_NAVIGATION_FINISHED) == 0) {
			this.controller.canceledNavigation();
		} else if (evt.getPropertyName().compareTo(Setting.PROPERTY_UPDATE_USER_POSITION) == 0) {
			this.updateDistance((Geoposition) evt.getNewValue());
		} else if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_EVALUATION_PREPARED) == 0) {
			this.controller.evaluationPrepared();
		} else if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_HIDE_ALL) == 0) {
			this.controller.toggleHideAllButton();
		} else if (evt.getPropertyName().compareTo(ResultTracker.PROPERTY_SAVED_ITEM_SCORES) == 0) {
			this.controller.scoresSavedToFile(SystemPrompt.SYSTEM_PROMPT_MODE_SUCCESS, (String) evt.getNewValue());
		} else if (evt.getPropertyName().compareTo(ResultTracker.PROPERTY_FAILED_TO_SAVE_ITEM_SCORES) == 0) {
			this.controller.scoresSavedToFile(SystemPrompt.SYSTEM_PROMPT_MODE_ERROR, (String) evt.getNewValue());
		} else if (evt.getPropertyName().compareTo(RecoTool.PROPERTY_ITEM_USAGE_DONE) == 0) {
			this.controller.itemUsageUpdate((UsedItem) evt.getNewValue());
		}
	}

	private void updateDistance(Geoposition currentPosition) {
		if (this.controller.getStudyApp().getNavigationDestination() != null) {
			double distance = 0;

			if (!this.controller.getStudyApp().getUseGeocoordinates()) {
				distance = this.controller.getStudyApp().getNavigationDestination().getGeoposition()
						.euclideanDistance(currentPosition);
			} else {
				distance = this.controller.getStudyApp().getNavigationDestination().getGeoposition()
						.distance(currentPosition);
			}

			this.controller.updateNavigationStatus(distance);
		}

		double distance = 0;

		if (!this.controller.getStudyApp().getUseGeocoordinates()) {
			distance = this.controller.getStudyApp().getEndPosition().euclideanDistance(currentPosition);
		} else {
			distance = this.controller.getStudyApp().getEndPosition().distance(currentPosition);
		}

		this.controller.updateDistanceToEndPosition(distance);
	}

	public WoZListener withController(WoZController value) {
		this.controller = value;
		return this;
	}

}
