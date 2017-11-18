package com.doccuty.radarplus.recommender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.jboss.logging.Logger;
import com.doccuty.radarplus.model.Item;

import java.util.Iterator;

public class SerendipityMechanism {

	private final static Logger LOG = Logger.getLogger(SerendipityMechanism.class);

	Recommender recommender;

	public SerendipityMechanism(Recommender recommender) {
		this.recommender = recommender;
	}

	public LinkedHashMap<Item, Double> serendipityProcessing(LinkedHashMap<Item, Double> itemMap) {

		LOG.info("SerendipityMechanism");

		if (itemMap.size() < 3)
			return itemMap;

		List<Item> kFurthestNeighbors = new ArrayList<Item>();

		// Get top recommendation
		LinkedHashMap<Item, Double> sim = this.recommender.getSimilarityMap().get(itemMap.keySet().iterator().next());

		// Number of serendipity items
		int k = (int) (this.recommender.getApp().getMaxNumOfItems() / 0.2);

		for (Iterator<Entry<Item, Double>> it = sim.entrySet().iterator(); it.hasNext();) {
			Entry<Item, Double> e = it.next();
			Item item = e.getKey();

			if (item.getIsTrainingItem() || this.recommender.getRecommendations().get(item) == null
					|| this.recommender.getApp().getSetting().getUsedItem().containsKey(item))
				continue;

			kFurthestNeighbors.add(item);

			// Remove most similar Item
			if (kFurthestNeighbors.size() > k) {
				Item rmItem = null;
				double maxSim = 0;
				for (Item i : kFurthestNeighbors) {
					double s = sim.get(i).doubleValue();
					if (s > maxSim || s == 0 && kFurthestNeighbors.size() == k) {
						rmItem = i;
						maxSim = s;
					}
				}

				kFurthestNeighbors.remove(rmItem);

				Collections.shuffle(kFurthestNeighbors);
			}
		}

		// Increase scores
		double maxScore = itemMap.values().iterator().next();

		for (Item item : kFurthestNeighbors) {

			double min = itemMap.get(item).doubleValue();

			if (min < 0.1)
				min = 0.1;

			double newScore = (Math.random() * (maxScore - min)) + min;
			itemMap.put(item, newScore);
		}

		LOG.info(kFurthestNeighbors);

		return itemMap;
	}

}
