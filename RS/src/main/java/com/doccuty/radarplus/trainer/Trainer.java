package com.doccuty.radarplus.trainer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.Rating;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.model.User;
import com.doccuty.radarplus.persistence.ItemDAO;

public class Trainer {

	User user;

	HashMap<Item, HashSet<Item>> ratings;

	List<Item> trainingItems;
	Stack<HashSet<Item>> trainingsList;

	ItemDAO itemDao;

	private RecoTool app;

	public Trainer() {
		this.ratings = new HashMap<Item, HashSet<Item>>();
		this.itemDao = new ItemDAO();
	}

	/**
	 * check if there are further combinations of items to compare Therefore,
	 * Gaussian sum formula is used
	 */

	public boolean isDone() {
		if (!this.trainingsList.isEmpty())
			return false;

		return true;
	}

	/**
	 * create a list of all item pairs systematically shuffle after creation
	 */

	public void createTrainingPairs() {

		trainingsList = new Stack<HashSet<Item>>();

		List<Item> list = this.trainingItems;

		Collections.shuffle(list);

		for (Item item : list) {
			for (int i = this.trainingItems.indexOf(item) + 1; i < this.trainingItems.size(); i++) {
				HashSet<Item> set = new HashSet<Item>();
				set.add(item);
				set.add(this.trainingItems.get(i));
				trainingsList.push(set);
			}
		}

		Collections.shuffle(trainingsList);
	}

	/**
	 * get new pair of items to compare
	 */

	public static final String PROPERTY_NEXT_PAIR = "nextPair";

	public List<Item> nextPair() {

		List<Item> items = new ArrayList<Item>();

		if (this.trainingsList.size() > 0) {

			HashSet<Item> set = this.trainingsList.pop();

			for (Iterator<Item> it = set.iterator(); it.hasNext();)
				items.add(it.next());

		} else {
			this.app.getCurrentUser().setRatings(this.calculateTrainingResults());
		}

		this.firePropertyChange(PROPERTY_NEXT_PAIR, null, items);
		return items;
	}

	public void ratedForItem(Item item1, Item item2) {
		if (item1 == null || item2 == null)
			return;

		if (!this.ratings.containsKey(item1)) {
			ratings.put(item1, new HashSet<Item>());
		}

		ratings.get(item1).add(item2);
	}

	/**
	 * Calculate rating for training item based on training results Therefore, votes
	 * are compared and ordered by the items' numbers of wins against other items.
	 * Afterwards the win vector is normalized to enable comparison with future
	 * votes.
	 * 
	 * @return
	 */

	public Rating[] calculateTrainingResults() {

		Rating[] pref = new Rating[this.trainingItems.size()];

		for (int i = 0; i < this.trainingItems.size(); i++) {

			Item item = this.trainingItems.get(i);

			if (this.ratings.containsKey(item)) {
				double r = this.ratings.get(item).size();
				double norm = r / this.trainingItems.size();

				pref[i] = new Rating().withItem(item).withRating(norm);

				LOG.info(item.getName() + ": n-rating:" + norm + " c-rating: " + r + " "
						+ Math.sqrt(this.trainingItems.size()));
			} else {
				pref[i] = new Rating().withItem(item).withRating(0);
			}
		}

		return pref;
	}

	// ===================================================

	public HashMap<Item, HashSet<Item>> getRatings() {
		return ratings;
	}

	public void setRatings(HashMap<Item, HashSet<Item>> value) {
		this.ratings = value;
	}

	public Trainer withRatings(HashMap<Item, HashSet<Item>> value) {
		this.setRatings(value);
		return this;
	}

	// ===================================================

	public List<Item> getTrainingItems() {
		return this.trainingItems;
	}

	public void setTrainingItems(Item... values) {

		if (this.trainingItems == null)
			this.trainingItems = new ArrayList<Item>();

		for (Item item : values) {
			if (!this.trainingItems.contains(item)) {
				this.trainingItems.add(item);
			}
		}
	}

	public void setTrainingItems(List<Item> values) {

		if (this.trainingItems == null)
			this.trainingItems = new ArrayList<Item>();

		for (Item item : values) {
			if (!this.trainingItems.contains(item)) {
				this.trainingItems.add(item);
			}
		}
	}

	public Trainer withTrainingItems(Item... values) {
		this.setTrainingItems(values);
		return this;
	}

	public void retreiveTrainingItemsFromDB() {
		this.trainingItems = itemDao.findAll();
	}

	// ===================================================

	public User getUser() {
		return this.user;
	}

	public void setUser(User value) {
		this.user = value;
	}

	public Trainer withUser(User value) {
		this.setUser(value);
		return this;
	}

	// ===================================================

	public RecoTool getStudyApp() {
		return this.app;
	}

	public void setStudyApp(RecoTool value) {
		this.app = value;
	}

	public Trainer withStudyApp(RecoTool value) {
		setStudyApp(value);
		return this;
	}

	/**
	 * Event Handling for controller
	 */

	private final static Logger LOG = Logger.getLogger(Trainer.class.getName());

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
