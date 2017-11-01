package com.doccuty.radarplus.evaluation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.Setting;
import com.doccuty.radarplus.model.User;

public class ResultTracker {

	public static final String PROPERTY_SAVED_ITEM_SCORES = "savedItemScores";
	public static final String PROPERTY_FAILED_TO_SAVE_ITEM_SCORES = "failedToSaveItemScores";

	private static String path;
	private static String eol;

	public ResultTracker() {
		ResultTracker.path = getClass().getClassLoader().getResource("evaluation").getPath().trim();
		ResultTracker.eol = System.getProperty("line.separator");
	}

	/**
	 * Save current recommendation scores to *.csv
	 * 
	 * @param map
	 * @param user
	 * @param s
	 */

	public void writeToCSV(LinkedHashMap<Item, Double> map, User user, Setting s) {

		String filename = user.getId() + "-" + s.getUsedItem().size() + ".csv";

		this.writeCurrentItemScoreToCSV(map, user, s, filename);
	}

	/**
	 * Save current recommendation scores to *.csv
	 * 
	 * @param map
	 * @param user
	 * @param s
	 * @param filename
	 */

	public void writeCurrentItemScoreToCSV(LinkedHashMap<Item, Double> map, User user, Setting s, String filename) {

		if (this.getFileSuffix(filename).compareTo("csv") != 0) {
			filename += ".csv";
		}

		try (Writer writer = new FileWriter(ResultTracker.path + "/" + filename)) {

			writer.append(String.valueOf("ID")).append(',').append(String.valueOf("name")).append(',')
					.append(String.valueOf("latitude")).append(',').append(String.valueOf("longitude")).append(',')
					.append(String.valueOf("score")).append(eol);

			for (Map.Entry<Item, Double> entry : map.entrySet()) {
				writer.append(String.valueOf(entry.getKey().getId())).append(',')
						.append(String.valueOf(entry.getKey().getName())).append(',')
						.append(String.valueOf(entry.getKey().getGeoposition().getLatitude())).append(',')
						.append(String.valueOf(entry.getKey().getGeoposition().getLongitude())).append(',')
						.append(String.valueOf(entry.getValue())).append(eol);
			}

			this.firePropertyChange(PROPERTY_SAVED_ITEM_SCORES, null, filename);
		} catch (IOException ex) {
			this.firePropertyChange(PROPERTY_FAILED_TO_SAVE_ITEM_SCORES, null, filename);
			ex.printStackTrace(System.err);
		}
	}

	/**
	 * Write a given list into *.csv
	 * 
	 * @param items
	 * @param user
	 */

	public void writeItemListToCSV(List<Item> items, String filename) {

		if (this.getFileSuffix(filename).compareTo("csv") != 0) {
			filename += ".csv";
		}

		try (Writer writer = new FileWriter(ResultTracker.path + "/" + filename)) {

			writer.append(String.valueOf("ID")).append(',').append(String.valueOf("name")).append(',')
					.append(String.valueOf("latitude")).append(',').append(String.valueOf("longitude")).append(',')
					.append(eol);

			for (Item item : items) {
				writer.append(String.valueOf(item.getId())).append(',').append(String.valueOf(item.getName()))
						.append(',').append(String.valueOf(item.getGeoposition().getLatitude())).append(',')
						.append(String.valueOf(item.getGeoposition().getLongitude())).append(',').append(eol);
			}

			this.firePropertyChange(PROPERTY_SAVED_ITEM_SCORES, null, filename);
		} catch (IOException ex) {
			this.firePropertyChange(PROPERTY_FAILED_TO_SAVE_ITEM_SCORES, null, filename);
			ex.printStackTrace(System.err);
		}
	}

	/**
	 * Get file suffix
	 * 
	 * @param filename
	 * @return
	 */

	private String getFileSuffix(String filename) {
		String extension = "";

		int i = filename.lastIndexOf('.');
		if (i > 0) {
			extension = filename.substring(i + 1);
		}

		return extension;
	}

	/**
	 * Read files from directory
	 * 
	 * @return
	 */

	public List<File> readResultDirectory() {
		List<File> files = new ArrayList<File>();

		File folder = new File(path);

		for (File f : folder.listFiles()) {
			if (!f.isHidden())
				files.add(f);
		}

		return files;
	}

	/**
	 * Count number of passed accuracy evaluations of a given user
	 * 
	 * @return
	 */

	public int getNumOfAccuracyEvaluationsOfUser(User user, String charSeq) {
		int cntr = 1;

		File folder = new File(path);

		for (File f : folder.listFiles()) {
			if (f.getName().contains(user.getId() + "-" + charSeq))
				cntr++;
		}

		return cntr;
	}

	// ================================

	public String getPath() {
		return ResultTracker.path;
	}

	/**
	 * Event handling
	 */

	protected PropertyChangeSupport listeners = null;

	public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		if (listeners != null) {
			listeners.firePropertyChange(propertyName, oldValue, newValue);
			return true;
		}
		return false;
	}

	public boolean addPropertyChangeListener(PropertyChangeListener listener) {
		if (listeners == null) {
			listeners = new PropertyChangeSupport(this);
		}
		listeners.addPropertyChangeListener(listener);
		return true;
	}

	public boolean addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		if (listeners == null) {
			listeners = new PropertyChangeSupport(this);
		}
		listeners.addPropertyChangeListener(propertyName, listener);
		return true;
	}

	public boolean removePropertyChangeListener(PropertyChangeListener listener) {
		if (listeners == null) {
			listeners.removePropertyChangeListener(listener);
		}
		listeners.removePropertyChangeListener(listener);
		return true;
	}

	public boolean removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		if (listeners != null) {
			listeners.removePropertyChangeListener(propertyName, listener);
		}
		return true;
	}
}