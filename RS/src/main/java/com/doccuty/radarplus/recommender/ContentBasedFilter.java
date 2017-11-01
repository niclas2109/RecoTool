package com.doccuty.radarplus.recommender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import com.doccuty.radarplus.model.Attribute;
import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.Rating;
import com.doccuty.radarplus.model.User;

/**
 * This component calculates both similarities between items and items and users
 * and items
 * 
 * @author mac
 *
 */

public class ContentBasedFilter {

	private final static Logger LOG = Logger.getLogger(ContentBasedFilter.class);

	private Recommender recommender;

	private LinkedHashMap<Item, Double> scoredItems;
	private LinkedHashMap<Attribute, Double> userPreferenceProfile;
	private LinkedHashMap<Item, LinkedHashMap<Attribute, Double>> attributeMatrix;

	public ContentBasedFilter() {
		this.scoredItems = new LinkedHashMap<Item, Double>();
		this.userPreferenceProfile = new LinkedHashMap<Attribute, Double>();
	}

	public ContentBasedFilter(Recommender recommender) {
		this.scoredItems = new LinkedHashMap<Item, Double>();
		this.userPreferenceProfile = new LinkedHashMap<Attribute, Double>();

		this.recommender = recommender;
	}

	/**
	 * Calculate relevance score of each item by taking user preference vector into
	 * account. Therefore, user preference vector is calculated based on a user's
	 * ratings.
	 * 
	 * @param user
	 * @param items
	 * @return
	 */
	public LinkedHashMap<Item, Double> calculateRelevance(User user, List<Item> items) {

		List<Attribute> attributeVector = this.buildAttributeVector(items);

		this.attributeMatrix = this.transformToAttributeMap(items, attributeVector);
		this.attributeMatrix = this.normalizeMatrixVectors(attributeMatrix);

		this.userPreferenceProfile = this.calculateUserPreferenceProfile(user.getRatings(), attributeMatrix,
				attributeVector.size());

		this.userPreferenceProfile = this.normalizeVector(userPreferenceProfile);

		user.setPreferences(this.userPreferenceProfile);

		LOG.info("User preference profile: " + userPreferenceProfile);

		// Calculate attribute that might be of high relevance to the user
		for (Item item : items) {
			item.setDomain(this.calculateBestMatchingAttribute(item));
		}

		// Compute weighted relevance for the given user
		HashMap<Long, Integer> df = this.calculateDF(attributeMatrix, this.buildAttributeVector(items));
		HashMap<Long, Double> idf = this.calculateIDFVector(df, items);

		scoredItems = this.calculateRecommendations(this.userPreferenceProfile, attributeMatrix, idf, user);

		return scoredItems;
	}

	/**
	 * Calculate weighted relevance score and return sorted relevance scores
	 * 
	 * @param preferenceProfile
	 * @param attributeMatrix
	 * @param weight
	 * @return
	 */
	private LinkedHashMap<Item, Double> calculateRecommendations(LinkedHashMap<Attribute, Double> preferenceProfile,
			LinkedHashMap<Item, LinkedHashMap<Attribute, Double>> attributeMatrix, HashMap<Long, Double> weight,
			User user) {

		LinkedHashMap<Item, Double> recommendations = new LinkedHashMap<Item, Double>();

		for (Iterator<Entry<Item, LinkedHashMap<Attribute, Double>>> it = attributeMatrix.entrySet().iterator(); it
				.hasNext();) {

			Entry<Item, LinkedHashMap<Attribute, Double>> e = it.next();

			double score = 0;
			for (Iterator<Entry<Attribute, Double>> it2 = preferenceProfile.entrySet().iterator(); it2.hasNext();) {
				Entry<Attribute, Double> e2 = it2.next();

				double w = (weight.containsKey(e2.getKey().getId())) ? weight.get(e2.getKey().getId()) : 1;
				LOG.debug("weight for " + e2.getKey().getAttribute() + ": " + w);

				score += e2.getValue() * e.getValue().get(e2.getKey());

				if (this.recommender != null && this.recommender.getWeightingEnabled())
					score *= w;
			}

			recommendations.put(e.getKey(), score);
		}
		return recommendations.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> {
					throw new AssertionError();
				}, LinkedHashMap::new));
	}

	/**
	 * Compare each item with each other to compute similarity between items
	 * 
	 * @param map
	 * @return
	 */

	public LinkedHashMap<Item, LinkedHashMap<Item, Double>> calculateSimilarityMatrix(
			LinkedHashMap<Item, LinkedHashMap<Attribute, Double>> map) {
		LinkedHashMap<Item, LinkedHashMap<Item, Double>> s = new LinkedHashMap<Item, LinkedHashMap<Item, Double>>();

		for (Iterator<Entry<Item, LinkedHashMap<Attribute, Double>>> it = map.entrySet().iterator(); it.hasNext();) {
			Entry<Item, LinkedHashMap<Attribute, Double>> e = it.next();

			LinkedHashMap<Item, Double> sim = new LinkedHashMap<Item, Double>();

			for (Iterator<Entry<Item, LinkedHashMap<Attribute, Double>>> itc = map.entrySet().iterator(); itc
					.hasNext();) {
				Entry<Item, LinkedHashMap<Attribute, Double>> ec = itc.next();

				if (ec.getKey().equals(e.getKey())) {
					sim.put(ec.getKey(), 1.0);
				} else {
					sim.put(ec.getKey(), this.calculateCosine(new ArrayList<Double>(e.getValue().values()),
							new ArrayList<Double>(ec.getValue().values())));
				}
			}

			s.put(e.getKey(), sim);

		}

		return s;
	}

	/**
	 * Calculation of cosine similarity between two vectors SUMPRODUCT(v1, v2)
	 * 
	 * @param items
	 * @return
	 */

	public Double calculateCosine(List<Double> v1, List<Double> v2) {

		if (v1.size() != v2.size()) {
			LOG.error("Vectors are not of the same length");
			return 0d;
		}

		double cos = 0;

		for (int i = 0; i < v1.size(); i++) {
			cos += v1.get(i) * v2.get(i);
		}

		return cos;
	}

	/**
	 * calculate term frequency
	 * 
	 * @param items
	 * @return
	 */

	public List<Integer> calculateTF(List<Item> items, List<Attribute> attributeVector) {

		List<Integer> tf = new ArrayList<Integer>();

		for (Attribute attribute : attributeVector) {
			int cntr = 0;
			for (Item item : items) {
				cntr += item.countAttributeOccurence(attribute);
			}

			tf.add(cntr);
		}

		return tf;
	}

	/**
	 * calculate document frequency
	 * 
	 * @param attributeVector
	 * @param items
	 * @return
	 */

	public HashMap<Long, Integer> calculateDF(LinkedHashMap<Item, LinkedHashMap<Attribute, Double>> attributeMatrix,
			List<Attribute> attributeVector) {

		HashMap<Long, Integer> df = new HashMap<Long, Integer>();

		for (Attribute attribute : attributeVector) {
			df.put(attribute.getId(), 0);

			for (Iterator<Entry<Item, LinkedHashMap<Attribute, Double>>> it = attributeMatrix.entrySet().iterator(); it
					.hasNext();) {

				Entry<Item, LinkedHashMap<Attribute, Double>> e = it.next();

				if (e.getValue().containsKey(attribute) && e.getValue().get(attribute) > 0) {
					df.put(attribute.getId(), df.get(attribute.getId()) + 1);
				}
			}
		}

		return df;
	}

	/**
	 * calculate inverse document frequency log10((|D|/(1+df)))
	 * 
	 * @param items
	 * @return
	 */

	public HashMap<Long, Double> calculateIDFVector(HashMap<Long, Integer> df, List<Item> items) {

		HashMap<Long, Double> idfVector = new HashMap<Long, Double>();

		int totalNumOfItems = items.size();

		for (Iterator<Entry<Long, Integer>> it = df.entrySet().iterator(); it.hasNext();) {
			Entry<Long, Integer> e = it.next();

			double idf = Math.log10(totalNumOfItems / (1 + e.getValue()));

			idfVector.put(e.getKey(), idf);
		}

		return idfVector;
	}

	/**
	 * collect all attributes in a vector with respect to user preferences in order
	 * to represent relevance of attributes
	 * 
	 * @param s
	 * @param items
	 * @return
	 */

	public LinkedHashMap<Attribute, Double> calculateUserPreferenceProfile(Set<Rating> ratings,
			LinkedHashMap<Item, LinkedHashMap<Attribute, Double>> attributeMatrix, int s) {

		LinkedHashMap<Attribute, Double> v = new LinkedHashMap<Attribute, Double>();

		for (Rating rating : ratings) {

			Item item = rating.getItem();

			LinkedHashMap<Attribute, Double> attributeList = attributeMatrix.get(item);

			if (attributeList == null) {
				continue;
			}

			for (Iterator<Entry<Attribute, Double>> it = attributeList.entrySet().iterator(); it.hasNext();) {

				Entry<Attribute, Double> e = it.next();

				double oldScore = (v.containsKey(e.getKey())) ? v.get(e.getKey()) : 0.0;

				Double score = attributeList.get(e.getKey()) * rating.getRating() + oldScore;

				v.put(e.getKey(), score);
			}
		}

		return v.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> {
					throw new AssertionError();
				}, LinkedHashMap::new));
	}

	/**
	 * Collect all attributes of all items in the system in a vector these items are
	 * at least all already rated items
	 * 
	 * @param items
	 * @param user
	 * @return
	 */

	public List<Attribute> buildAttributeVector(List<Item> items) {

		List<Attribute> v = new ArrayList<Attribute>();

		for (Item item : items) {
			for (Attribute attribute : item.getAttributeList()) {
				if (!v.contains(attribute)) {
					v.add(attribute);
				}
			}
		}

		return v;
	}

	/**
	 * Create matrix, which contains the same attributes for all items
	 * 
	 * @param items
	 * @return
	 */

	public LinkedHashMap<Item, LinkedHashMap<Attribute, Double>> transformToAttributeMap(List<Item> items,
			List<Attribute> attributeVector) {

		LinkedHashMap<Item, LinkedHashMap<Attribute, Double>> map = new LinkedHashMap<Item, LinkedHashMap<Attribute, Double>>();

		for (Item item : items) {

			LinkedHashMap<Attribute, Double> v = new LinkedHashMap<Attribute, Double>();

			for (Attribute attribute : attributeVector) {
				v.put(attribute, (double) item.countAttributeOccurence(attribute));
			}

			map.put(item, v);
		}

		return map;
	}

	/**
	 * vector normalization per item in the matrix
	 * 
	 * @param attributeMatrix
	 * @return
	 */

	public LinkedHashMap<Item, LinkedHashMap<Attribute, Double>> normalizeMatrixVectors(
			LinkedHashMap<Item, LinkedHashMap<Attribute, Double>> attributeMatrix) {

		for (Iterator<Entry<Item, LinkedHashMap<Attribute, Double>>> it = attributeMatrix.entrySet().iterator(); it
				.hasNext();) {
			Entry<Item, LinkedHashMap<Attribute, Double>> e = it.next();

			attributeMatrix.put(e.getKey(), this.normalizeVector(e.getValue()));
		}

		return attributeMatrix;
	}

	/**
	 * Basic vector normalization
	 * 
	 * @param v
	 * @return
	 */

	public LinkedHashMap<Attribute, Double> normalizeVector(LinkedHashMap<Attribute, Double> v) {
		double l = this.calculateVetorlength(v);

		for (Iterator<Entry<Attribute, Double>> it = v.entrySet().iterator(); it.hasNext();) {
			Entry<Attribute, Double> e = it.next();

			double value = (l > 0) ? e.getValue() / l : 0;
			e.setValue(value);
		}

		return v;
	}

	public List<Double> normalizeVector(List<Double> v) {
		double l = this.calculateVetorlength(v);

		for (int i = 0; i < v.size(); i++) {
			double value = (l > 0) ? v.get(i) / l : 0;
			v.set(i, value);
		}

		return v;
	}

	public double calculateVetorlength(LinkedHashMap<Attribute, Double> v) {
		double l = 0;

		for (Iterator<Entry<Attribute, Double>> it = v.entrySet().iterator(); it.hasNext();) {
			Entry<Attribute, Double> e = it.next();

			l += e.getValue() * e.getValue();
		}

		return Math.sqrt(l);
	}

	public double calculateVetorlength(List<Double> v) {
		double l = 0;

		for (Double d : v) {
			l += d * d;
		}

		return Math.sqrt(l);
	}

	// ====================================

	/**
	 * calculate best fitting attribute with regard to user preference vector
	 * 
	 * @param item
	 * @return
	 */

	public Attribute calculateBestMatchingAttribute(Item item) {

		if (this.userPreferenceProfile == null)
			return null;

		Attribute a = null;
		double rating = -1;

		for (Attribute attribute : item.getAttributeList()) {
			if (this.userPreferenceProfile.containsKey(attribute)
					&& this.userPreferenceProfile.get(attribute) > rating) {
				rating = this.userPreferenceProfile.get(attribute);
				a = attribute;
			}
		}

		return a;
	}

	// ====================================

	public LinkedHashMap<Item, Double> getScoredItems() {
		return this.scoredItems;
	}

	// ====================================

	public LinkedHashMap<Item, LinkedHashMap<Attribute, Double>> getAttributeMatrix() {
		return this.attributeMatrix;
	}
}
