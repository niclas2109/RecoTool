package com.doccuty.radarplus.recommender;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.logging.Logger;

import com.doccuty.radarplus.model.Attribute;
import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.Setting;
import com.doccuty.radarplus.model.User;

/**
 * This is the last step when filtering This component takes the current Setting
 * into account to weight relations based on previous content-based filtering
 * with.
 * 
 * @author mac
 *
 */

public class ContextBasedPostFilter {

	private final static Logger LOG = Logger.getLogger(ContextBasedPostFilter.class);

	private double timeMaximizer;

	Recommender recommender;

	Map<Attribute, Date> usedAttributes;

	public ContextBasedPostFilter(Recommender recommender) {
		this.recommender = recommender;
		this.usedAttributes = new HashMap<Attribute, Date>();

		this.timeMaximizer = 20000;
	}

	public LinkedHashMap<Item, Double> filterBySetting(User user, Setting setting,
			LinkedHashMap<Item, Double> itemMap) {

		LOG.debug("Started contextual filtering...");

		for (Iterator<Entry<Item, Double>> it = itemMap.entrySet().iterator(); it.hasNext();) {

			Entry<Item, Double> pair = it.next();

			/**
			 * Check, if item usage is possible due to time and distance factors
			 */
			if (!this.checkReachability(pair.getKey(), setting, user)) {
				it.remove();
				continue;
			}

			double score = recommender.getOriginalRecommendations().get(pair.getKey());
			double simScore = recommender.getOriginalRecommendations().get(pair.getKey());

			// check if item was already used and calculate new temporary score
			if (setting.getUsedItem().containsKey(pair.getKey())) {

				// Determine timestamp of last usage
				Date lastUsage = this.getLastUsageOfItem(pair.getKey(), setting);

				/**
				 * Calculate temporary score by time passed since last usage conceptual example
				 */
				score = this.calculateNewTempScore(score, lastUsage, setting);

			}

			// check if similar items were already used and calculate new temporary score
			if (setting.getUsedItem().size() > 0 && this.recommender.getSimilarityMap().size() > 0) {

				/**
				 * Check for similarities between items
				 */

				LinkedHashMap<Item, Double> similarities = this.recommender.getSimilarityMap().get(pair.getKey());

				double maxSimilarity = 0;
				Item compareItem = null;
				for (Iterator<Entry<Item, Double>> simIterator = similarities.entrySet().iterator(); simIterator
						.hasNext();) {
					Entry<Item, Double> entry = simIterator.next();

					if (entry.getKey().equals(pair.getKey()) || !setting.getUsedItem().containsKey(entry.getKey()))
						continue;

					if (maxSimilarity < entry.getValue()) {
						maxSimilarity = entry.getValue();
						compareItem = entry.getKey();

						if (maxSimilarity == 1.0)
							break;
					}
				}

				/**
				 * Lower item scores of current item, if it is similar to already used ones
				 */

				if (compareItem != null && maxSimilarity > 0.4) {
					Date lastUsage = this.getLastUsageOfItem(compareItem, setting);

					/**
					 * Calculate temporary score by time passed since last usage and similarity
					 */
					simScore = this.calculateNewTempScoreOfSimilarItem(score, lastUsage, maxSimilarity, setting);
				}
			}

			// apply lower score
			itemMap.put(pair.getKey(), (simScore < score) ? simScore : score);
		}

		LOG.debug("Contextual filtering done");

		return itemMap;
	}

	/**
	 * Filter Productivity Items
	 * 
	 * @param user
	 * @param setting
	 * @param items
	 * @return
	 */
	public LinkedHashMap<Item, Double> filterBySetting(User user, Setting setting, List<Item> items) {

		LinkedHashMap<Item, Double> map = new LinkedHashMap<Item, Double>();

		for (Iterator<Item> it = items.iterator(); it.hasNext();) {
			Item item = it.next();
			if (this.checkReachability(item, setting, user)) {
				map.put(item, 1.0);
			}
		}

		return map;
	}

	/**
	 * Check, if item usage is possible due to time and distance factors
	 */
	private boolean checkReachability(Item item, Setting setting, User user) {

		if (this.recommender.getApp() == null || this.recommender.getApp().getNewEvaluation()
				&& this.recommender.getApp().getDelayDuration().toMillis() > 0)
			return true;

		if (item.getGeoposition() != null && setting.getGeoposition() != null) {

			// calculate current distance to item
			double distance = 0;

			if (this.recommender.getApp().getUseGeocoordinates()) {
				distance = item.getGeoposition().distance(setting.getGeoposition());
				distance += item.getGeoposition().distance(setting.getNextDeparture());
			} else {
				distance = item.getGeoposition().euclideanDistance(setting.getGeoposition());

				if (setting.getNextDeparture() != null)
					distance += item.getGeoposition().euclideanDistance(setting.getNextDeparture());
			}

			// time to reach the item in [ms]
			long speed = this.recommender.getApp().getCurrentUser().getCurrentWalkingSpeed();

			if (speed >= user.getAvgWalkingSpeed()) {
				if (speed >= user.getMaxWalkingSpeed()) {
					speed = user.getMaxWalkingSpeed();
				} else {
					speed = user.getAvgWalkingSpeed();
				}
			} else {
				speed = user.getMinWalkingSpeed();
			}

			double tRoute = distance * 3600 * 1000 / user.getAvgWalkingSpeed();

			// estimated time for item usage in [ms]
			long tUsage = 0;

			if (this.recommender.getApp().getNumOfItemsToUse() == 0) {
				tUsage = item.getEstimatedUsageDuration().toMillis();
			} else {
				tUsage = this.recommender.getApp().getOptimizedItemUsageDuration();
			}

			// remove items that can not be used
			if (setting.getTimeToDeparture() < tRoute + tUsage) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Set current relevance score -1 and increment by time to original score. Used
	 * items are assumed as known and do not need to be recommended for the same
	 * session.
	 * 
	 * @param score
	 * @param lastUsage
	 * @return
	 */
	private double calculateNewTempScore(double score, Date lastUsage, Setting setting) {

		double x = (setting.getCurrentTime().getTime() - lastUsage.getTime()) / this.timeMaximizer;

		if (x >= Math.PI)
			return score;

		double tmpScore = score - (Math.cos(x) + 1) * 0.5 * (score + 1);

		return tmpScore;
	}

	/**
	 * Calculate current relevance score by duration since last usage of used item
	 * and respective similarity
	 * 
	 * @param score
	 * @param lastUsage
	 * @return
	 */
	private double calculateNewTempScoreOfSimilarItem(double score, Date lastUsage, double similarity,
			Setting setting) {

		double x = (setting.getCurrentTime().getTime() - lastUsage.getTime()) / this.timeMaximizer;

		if (x >= Math.PI)
			return score;

		double tmpScore = score - (Math.cos(x) + 1) * 0.5 * (score + 1) * similarity;

		LOG.debug(tmpScore + "\n" + ((Math.cos(x) + 1) * 0.5));

		return tmpScore;
	}

	private Date getLastUsageOfItem(Item item, Setting setting) {

		Date lastUsage = setting.getUsedItem().get(item).getCurrentTime();

		/*
		 * // Calculate probable reason for item usage Attribute a =
		 * this.recommender.getContentBasedFilter().calculateBestMatchingAttribute(item)
		 * ;
		 * 
		 * if (a == null) return null;
		 * 
		 * // Determine timestamp of the attribute's last usage Date lastUsage; if
		 * (this.usedAttributes.containsKey(a) && this.usedAttributes.get(a).getTime() <
		 * setting.getUsedItem() .get(item).getCurrentTime().getTime() ||
		 * !this.usedAttributes.containsKey(a)) { lastUsage =
		 * setting.getUsedItem().get(item).getCurrentTime(); this.usedAttributes.put(a,
		 * lastUsage); } else { lastUsage = this.usedAttributes.get(a); }
		 */

		return lastUsage;
	}

	// ==================================

	public double getTimeMaximizer() {
		return this.timeMaximizer;
	}

	public void setTimeMaximizer(double value) {
		this.timeMaximizer = value;
	}

	public ContextBasedPostFilter withTimeMaximizer(double value) {
		this.setTimeMaximizer(value);
		return this;
	}
}
