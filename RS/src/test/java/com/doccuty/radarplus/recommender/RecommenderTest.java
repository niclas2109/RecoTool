package com.doccuty.radarplus.recommender;

import static org.junit.Assert.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

import com.doccuty.radarplus.model.Attribute;
import com.doccuty.radarplus.model.AttributeTupel;
import com.doccuty.radarplus.model.Disability;
import com.doccuty.radarplus.model.Geoposition;
import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.Rating;
import com.doccuty.radarplus.model.RecoTool;
import com.doccuty.radarplus.model.Setting;
import com.doccuty.radarplus.model.User;
import com.doccuty.radarplus.persistence.UserDAO;
import com.doccuty.radarplus.recommender.ConstraintBasedFilter;
import com.doccuty.radarplus.recommender.ContentBasedFilter;
import com.doccuty.radarplus.trainer.Trainer;

public class RecommenderTest {

	@Test
	public void constraintBasedFilteringTest() {

		Attribute a1 = new Attribute().withAttribute("Gastronomie").withId(generateId());
		Attribute a2 = new Attribute().withAttribute("Café").withId(generateId());
		Attribute a3 = new Attribute().withAttribute("Imbiss").withId(generateId());
		Attribute a4 = new Attribute().withAttribute("Restaurant").withId(generateId());
		Attribute a5 = new Attribute().withAttribute("Kuchen").withId(generateId());
		Attribute a6 = new Attribute().withAttribute("Kaffee").withId(generateId());
		Attribute a7 = new Attribute().withAttribute("Schnitzel").withId(generateId());

		AttributeTupel at1 = new AttributeTupel().withAttribute(a1);

		AttributeTupel at2 = new AttributeTupel().withAttribute(a2).withParent(at1);
		AttributeTupel at3 = new AttributeTupel().withAttribute(a3).withParent(at1);
		AttributeTupel at4 = new AttributeTupel().withAttribute(a4).withParent(at1);

		AttributeTupel at5 = new AttributeTupel().withAttribute(a5).withParent(at2);
		AttributeTupel at6 = new AttributeTupel().withAttribute(a6).withParent(at2);
		AttributeTupel at7 = new AttributeTupel().withAttribute(a7).withParent(at4);

		Calendar dateOfBirth = new GregorianCalendar();
		dateOfBirth.set(1990, 9, 21);

		User user = new User().withFirstname("Bob").withLastname("Bommel").withEmail("bob@bommel.com")
				.withDateOfBirth(dateOfBirth).withBufferToNextConnection(5).withAdaptivityEnabled(true)
				.withConstraint(a1).withDisability(new Disability().withDisability("Gehbehinderung"));

		Item item1 = new Item().withId(generateId()).withName("Item 1").withAttribute(at1)
				.withGeoposition(new Geoposition());
		Item item2 = new Item().withId(generateId()).withName("Item 2").withAttribute(at2, at5)
				.withGeoposition(new Geoposition());
		Item item3 = new Item().withId(generateId()).withName("Item 3").withAttribute(at1, at3)
				.withGeoposition(new Geoposition());
		Item item4 = new Item().withId(generateId()).withName("Item 4").withAttribute(at4)
				.withGeoposition(new Geoposition());
		Item item5 = new Item().withId(generateId()).withName("Item 5").withAttribute(at2, at7)
				.withGeoposition(new Geoposition());
		Item item6 = new Item().withId(generateId()).withName("Item 6").withAttribute(at3)
				.withGeoposition(new Geoposition());
		Item item7 = new Item().withId(generateId()).withName("Item 7").withAttribute(at6, at7)
				.withGeoposition(new Geoposition());

		LinkedHashMap<Item, Double> map = new LinkedHashMap<Item, Double>();
		map.put(item1, 0.0);
		map.put(item2, 0.0);
		map.put(item3, 0.0);
		map.put(item4, 0.0);
		map.put(item5, 0.0);
		map.put(item6, 0.0);
		map.put(item7, 0.0);

		
		ConstraintBasedFilter filter = new ConstraintBasedFilter();

		LinkedHashMap<Item, Double> recommendations = filter.filterItems(user, map);

		assertEquals("Recommended item number", 5, recommendations.values().size());
	}

	@Test
	public void contentBasedFilteringTest() {

		Attribute a1 = new Attribute().withAttribute("Gastronomie").withId(generateId());
		Attribute a2 = new Attribute().withAttribute("Café").withId(generateId());
		Attribute a3 = new Attribute().withAttribute("Imbiss").withId(generateId());
		Attribute a4 = new Attribute().withAttribute("Restaurant").withId(generateId());
		Attribute a5 = new Attribute().withAttribute("Kuchen").withId(generateId());
		Attribute a6 = new Attribute().withAttribute("Kaffee").withId(generateId());
		Attribute a7 = new Attribute().withAttribute("Schnitzel").withId(generateId());

		AttributeTupel at1 = new AttributeTupel().withAttribute(a1);

		AttributeTupel at2 = new AttributeTupel().withAttribute(a2).withParent(at1);
		AttributeTupel at3 = new AttributeTupel().withAttribute(a3).withParent(at1);
		AttributeTupel at4 = new AttributeTupel().withAttribute(a4).withParent(at1);

		AttributeTupel at5 = new AttributeTupel().withAttribute(a5).withParent(at2);
		AttributeTupel at6 = new AttributeTupel().withAttribute(a6).withParent(at2);
		AttributeTupel at7 = new AttributeTupel().withAttribute(a7).withParent(at4);

		Item item1 = new Item().withId(generateId()).withName("Item 1").withAttribute(at1);
		Item item2 = new Item().withId(generateId()).withName("Item 2").withAttribute(at2, at5);
		Item item3 = new Item().withId(generateId()).withName("Item 3").withAttribute(at1, at3);
		Item item4 = new Item().withId(generateId()).withName("Item 4").withAttribute(at4);
		Item item5 = new Item().withId(generateId()).withName("Item 5").withAttribute(at2, at7);
		Item item6 = new Item().withId(generateId()).withName("Item 6").withAttribute(at3);
		Item item7 = new Item().withId(generateId()).withName("Item 7").withAttribute(at6, at7);

		Calendar dateOfBirth = new GregorianCalendar();
		dateOfBirth.set(1990, 9, 21);

		User user = new User().withFirstname("Bob").withLastname("Bommel").withEmail("bob@bommel.com")
				.withDateOfBirth(dateOfBirth).withBufferToNextConnection(5).withAdaptivityEnabled(true)
				.withDisability(new Disability().withDisability("Gehbehinderung"))
				.withRatings(new Rating().withItem(item1).withRating(1), new Rating().withItem(item4).withRating(-1),
						new Rating().withItem(item2).withRating(-0.2), new Rating().withItem(item3).withRating(1));

		List<Item> itemList = new ArrayList<Item>();

		itemList.add(item1);
		itemList.add(item2);
		itemList.add(item3);
		itemList.add(item4);
		itemList.add(item5);
		itemList.add(item6);

		itemList.add(item7);

		ContentBasedFilter filter = new ContentBasedFilter();

		LinkedHashMap<Item, Double> recommendations = filter.calculateRelevance(user, itemList);

		System.out.println("Recommendations:\n" + recommendations);
	}

	@Test
	public void fullFilteringTest() {

		Attribute a1 = new Attribute().withAttribute("Gastronomie").withId(generateId())
				.withMinDuration(Duration.ofMinutes(20));
		Attribute a2 = new Attribute().withAttribute("Café").withId(generateId());
		Attribute a3 = new Attribute().withAttribute("Imbiss").withId(generateId());
		Attribute a4 = new Attribute().withAttribute("Restaurant").withId(generateId());
		Attribute a5 = new Attribute().withAttribute("Kuchen").withId(generateId());
		Attribute a6 = new Attribute().withAttribute("Kaffee").withId(generateId());
		Attribute a7 = new Attribute().withAttribute("Schnitzel").withId(generateId());

		AttributeTupel at1 = new AttributeTupel().withAttribute(a1);

		AttributeTupel at2 = new AttributeTupel().withAttribute(a2).withParent(at1);
		AttributeTupel at3 = new AttributeTupel().withAttribute(a3).withParent(at1);
		AttributeTupel at4 = new AttributeTupel().withAttribute(a4).withParent(at1);

		AttributeTupel at5 = new AttributeTupel().withAttribute(a5).withParent(at2);
		AttributeTupel at6 = new AttributeTupel().withAttribute(a6).withParent(at2);
		AttributeTupel at7 = new AttributeTupel().withAttribute(a7).withParent(at4);

		Item item1 = new Item().withId(generateId()).withName("Item 1").withAttribute(at1)
				.withGeoposition(new Geoposition().withLatitude(9.222891313).withLatitude(9.916721313));
		Item item2 = new Item().withId(generateId()).withName("Item 2").withAttribute(at2, at5)
				.withGeoposition(new Geoposition().withLatitude(9.6121313).withLatitude(9.9121313));
		Item item3 = new Item().withId(generateId()).withName("Item 3").withAttribute(at1, at3)
				.withGeoposition(new Geoposition().withLatitude(9.9121313090909).withLatitude(9.9121313090909));
		Item item4 = new Item().withId(generateId()).withName("Item 4").withAttribute(at4)
				.withGeoposition(new Geoposition().withLatitude(9.9121313090909).withLatitude(9.9121313090909));
		Item item5 = new Item().withId(generateId()).withName("Item 5").withAttribute(at2, at7)
				.withGeoposition(new Geoposition().withLatitude(9.012136913).withLatitude(9.502138713));
		Item item6 = new Item().withId(generateId()).withName("Item 6").withAttribute(at3)
				.withGeoposition(new Geoposition().withLatitude(9.1121313).withLatitude(9.7101313));
		Item item7 = new Item().withId(generateId()).withName("Item 7").withAttribute(at6, at7)
				.withGeoposition(new Geoposition().withLatitude(9.9121313).withLatitude(9.0021313));

		Calendar dateOfBirth = new GregorianCalendar();
		dateOfBirth.set(1990, 9, 21);

		User user = new User().withFirstname("Bob").withLastname("Bommel").withEmail("bob@bommel.com")
				.withDateOfBirth(dateOfBirth).withBufferToNextConnection(5).withAdaptivityEnabled(true)
				.withCurrentWalkingSpeed(4).withDisability(new Disability().withDisability("Gehbehinderung"))
				.withRatings(new Rating().withItem(item1).withRating(1), new Rating().withItem(item2).withRating(1),
						new Rating().withItem(item3).withRating(1), new Rating().withItem(item4).withRating(1));

		List<Item> itemList = new ArrayList<Item>();
		itemList.add(item1);
		itemList.add(item2);
		itemList.add(item3);
		itemList.add(item4);
		itemList.add(item5);
		itemList.add(item6);
		itemList.add(item7);

		Setting setting = new Setting().withEstimatedDepartureTime(new Date(new Date().getTime() + 900000))
				.withGeoposition(new Geoposition().withLatitude(9.9121313090909).withLatitude(9.9121313090909));

		Recommender recommender = new Recommender();

		ContentBasedFilter cbf = new ContentBasedFilter();
		ConstraintBasedFilter cnbf = new ConstraintBasedFilter();
		ContextBasedPostFilter cbpf = new ContextBasedPostFilter(recommender);

		LinkedHashMap<Item, Double> recommendations = cbf.calculateRelevance(user, itemList);
		System.out.println("Relevance:\n" + recommendations);

		LinkedHashMap<Item, Double> filteredItems = cnbf.filterItems(user, recommendations);
		System.out.println("Filtered:\n" + filteredItems);

		recommendations = cbpf.filterBySetting(user, setting, recommendations);
		System.out.println("\nFinal Relevance:\n" + recommendations);
	}

	@Test
	public void fullTestIncludingTraining() {

		Attribute a1 = new Attribute().withAttribute("Gastronomie").withId(generateId())
				.withMinDuration(Duration.ofMinutes(20));
		Attribute a2 = new Attribute().withAttribute("Café").withId(generateId());
		Attribute a3 = new Attribute().withAttribute("Imbiss").withId(generateId());
		Attribute a4 = new Attribute().withAttribute("Restaurant").withId(generateId());
		Attribute a5 = new Attribute().withAttribute("Kuchen").withId(generateId());
		Attribute a6 = new Attribute().withAttribute("Kaffee").withId(generateId());
		Attribute a7 = new Attribute().withAttribute("Schnitzel").withId(generateId());
		Attribute a8 = new Attribute().withAttribute("Bier").withId(generateId());
		Attribute a9 = new Attribute().withAttribute("Wein").withId(generateId());

		AttributeTupel at1 = new AttributeTupel().withAttribute(a1);

		AttributeTupel at2 = new AttributeTupel().withAttribute(a2).withParent(at1);
		AttributeTupel at3 = new AttributeTupel().withAttribute(a3).withParent(at1);
		AttributeTupel at4 = new AttributeTupel().withAttribute(a4).withParent(at1);

		AttributeTupel at5 = new AttributeTupel().withAttribute(a5).withParent(at2);
		AttributeTupel at6 = new AttributeTupel().withAttribute(a6).withParent(at2);
		AttributeTupel at7 = new AttributeTupel().withAttribute(a7).withParent(at4);
		AttributeTupel at8 = new AttributeTupel().withAttribute(a8).withParent(at4);
		AttributeTupel at9 = new AttributeTupel().withAttribute(a9).withParent(at4);

		// Training Items
		Item item1 = new Item().withId(generateId()).withName("Item 1").withAttribute(at6).withIsTrainingItem(true);
		Item item2 = new Item().withId(generateId()).withName("Item 2").withAttribute(at2).withIsTrainingItem(true);
		Item item3 = new Item().withId(generateId()).withName("Item 3").withAttribute(at3).withIsTrainingItem(true);
		Item item4 = new Item().withId(generateId()).withName("Item 4").withAttribute(at4).withIsTrainingItem(true);
		Item item5 = new Item().withId(generateId()).withName("Item 5").withAttribute(at2).withIsTrainingItem(true);
		Item item6 = new Item().withId(generateId()).withName("Item 6").withAttribute(at3).withIsTrainingItem(true);
		Item item7 = new Item().withId(generateId()).withName("Item 7").withAttribute(at7, at8)
				.withIsTrainingItem(true);

		Calendar dateOfBirth = new GregorianCalendar();
		dateOfBirth.set(1990, 9, 21);

		User user = new User().withFirstname("Bob").withLastname("Bommel").withEmail("bob@bommel.com")
				.withDateOfBirth(dateOfBirth).withBufferToNextConnection(5).withAdaptivityEnabled(true)
				.withConstraint(a1).withDisability(new Disability().withDisability("Gehbehinderung"));

		// Training phase

		Trainer trainer = new Trainer().withUser(user).withTrainingItems(item1, item2, item3, item4, item5);
		trainer.createTrainingPairs();

		do {
			List<Item> pair = trainer.nextPair();
			trainer.ratedForItem(pair.get(0), pair.get(1));
		} while (!trainer.isDone());

		trainer.getUser().setRatings(trainer.calculateTrainingResults());

		// Pool of unknown items to recommend
		Item item8 = new Item().withId(generateId()).withName("Item 8").withAttribute(at1)
				.withGeoposition(new Geoposition().withLatitude(9.222891313).withLatitude(9.916721313));
		Item item9 = new Item().withId(generateId()).withName("Item 9").withAttribute(at2, at5)
				.withGeoposition(new Geoposition().withLatitude(9.6121313).withLatitude(9.9121313));
		Item item10 = new Item().withId(generateId()).withName("Item 10").withAttribute(at1, at3)
				.withGeoposition(new Geoposition().withLatitude(9.9121313090909).withLatitude(9.9121313090909));
		Item item11 = new Item().withId(generateId()).withName("Item 11").withAttribute(at4)
				.withGeoposition(new Geoposition().withLatitude(9.9121313090909).withLatitude(9.9121313090909));
		Item item12 = new Item().withId(generateId()).withName("Item 12").withAttribute(at2, at7)
				.withGeoposition(new Geoposition().withLatitude(9.012136913).withLatitude(9.502138713));
		Item item13 = new Item().withId(generateId()).withName("Item 13").withAttribute(at3)
				.withGeoposition(new Geoposition().withLatitude(9.1121313).withLatitude(9.7101313));
		Item item14 = new Item().withId(generateId()).withName("Item 14").withAttribute(at6, at7, at8, at9)
				.withGeoposition(new Geoposition().withLatitude(9.9121313).withLatitude(9.0021313));

		List<Item> itemList = new ArrayList<Item>();
		itemList.add(item1);
		itemList.add(item2);
		itemList.add(item3);
		itemList.add(item4);
		itemList.add(item5);
		itemList.add(item6);
		itemList.add(item7);
		itemList.add(item8);
		itemList.add(item9);
		itemList.add(item10);
		itemList.add(item11);
		itemList.add(item12);
		itemList.add(item13);
		itemList.add(item14);

		// Filtering process

		Recommender recommender = new Recommender();

		ContentBasedFilter cbf = new ContentBasedFilter();
		ConstraintBasedFilter cnbf = new ConstraintBasedFilter();
		ContextBasedPostFilter cbpf = new ContextBasedPostFilter(recommender);

		Setting setting = new Setting().withEstimatedDepartureTime(new Date(new Date().getTime() + 900000))
				.withGeoposition(new Geoposition().withLatitude(9.9121313090909).withLatitude(9.9121313090909));

		LinkedHashMap<Item, Double> recommendations = cbf.calculateRelevance(user, itemList);
		System.out.println("Relevance:\n" + recommendations);

		LinkedHashMap<Item, Double> filteredItems = cnbf.filterItems(user, recommendations);
		System.out.println("Filtered:\n" + filteredItems);

		recommendations = cbpf.filterBySetting(user, setting, recommendations);
		System.out.println("\nFinal Relevance:\n" + recommendations);
	}

	@Test
	public void fullTestWithUserFromDatabase() {
		UserDAO daoUser = new UserDAO();
		User user = daoUser.findById(1);

		RecoTool app = new RecoTool();
		app.init();

		app.setCurrentUser(user);
		app.generateRecommendations();
	}

	/**
	 * generating random unique id
	 * 
	 * @return
	 */

	public long generateId() {
		return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
	}
}
