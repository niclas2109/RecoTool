package com.doccuty.radarplus.recommender.training;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

import com.doccuty.radarplus.model.Attribute;
import com.doccuty.radarplus.model.AttributeTupel;
import com.doccuty.radarplus.model.Disability;
import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.User;
import com.doccuty.radarplus.trainer.Trainer;

public class TrainingTest {

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
				.withConstraint(a1).withDisability(new Disability().withDisability("Gehbehinderung"));

		Trainer trainer = new Trainer().withUser(user).withTrainingItems(item1, item2, item3, item4, item5, item6,
				item7);
		trainer.createTrainingPairs();

		do {
			List<Item> pair = trainer.nextPair();
			trainer.ratedForItem(pair.get(0), pair.get(1));
		} while (!trainer.isDone());

		trainer.getUser().setRatings(trainer.calculateTrainingResults());
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
