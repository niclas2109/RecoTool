package com.doccuty.radarplus.recommender;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.doccuty.radarplus.model.Attribute;
import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.User;

public class ConstraintBasedFilter {

	private final static Logger LOG = Logger.getLogger(ConstraintBasedFilter.class.getName());

	private Recommender recommender;

	public ConstraintBasedFilter() {

	}

	public ConstraintBasedFilter(Recommender recommender) {
		this.recommender = recommender;
	}

	/**
	 * This filter removes Items that do not fulfill a user's explicit constraints
	 * 
	 * @param items
	 * @param user
	 */

	public LinkedHashMap<Item, Double> filterItems(User user, LinkedHashMap<Item, Double> items) {

		for (Iterator<Item> it = items.keySet().iterator(); it.hasNext();) {
			Item item = it.next();

			if (this.recommender != null && (item.getTrafficJunction() == null || !item.getTrafficJunction()
					.equals(this.recommender.getApp().getSetting().getTrafficJunction()))) {
				it.remove();
				continue;
			}

			int age = user.getAge();

			if (age < item.getMinAge() || item.getGeoposition() == null) {
				it.remove();
				continue;
			}

			boolean remove = false;

			for (Attribute attribute : user.getConstraint()) {
				if (item.hasAttribute(attribute)) {
					remove = true;
					LOG.info("remove item " + item.getName() + " because of constraint " + attribute.getAttribute());
					break;
				}
			}

			if (remove) {
				it.remove();
			}
		}

		return items;
	}

}