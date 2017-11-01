package com.doccuty.radarplus.view.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.trainer.Trainer;
import com.doccuty.radarplus.view.controller.TrainingController;

public class TrainingListener implements PropertyChangeListener {

	TrainingController controller;
	
	public TrainingListener(TrainingController controller) {
		this.controller = controller;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(Trainer.PROPERTY_NEXT_PAIR)) {
			this.controller.updateButtons((List<Item>) evt.getNewValue());
		}
	}

}
