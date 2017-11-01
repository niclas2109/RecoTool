package com.doccuty.radarplus.persistence;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.transaction.Transactional;

import org.junit.Test;

import com.doccuty.radarplus.model.Attribute;
import com.doccuty.radarplus.model.Disability;
import com.doccuty.radarplus.model.Item;
import com.doccuty.radarplus.model.Rating;
import com.doccuty.radarplus.model.Setting;
import com.doccuty.radarplus.model.User;
import com.doccuty.radarplus.persistence.UserDAO;

public class UserPersistenceTest {

	@Test
	@Transactional
	public void saveUser() {

		Calendar dateOfBirth = new GregorianCalendar();
		dateOfBirth.set(1990, 9, 21);

		User user = new User().withFirstname("Bob").withLastname("Bommel").withEmail("bob@bommel1.com")
				.withDateOfBirth(dateOfBirth)
				.withBufferToNextConnection(5).withAdaptivityEnabled(true)
				.withConstraint(new Attribute().withAttribute("Schnitzel"));

		new Disability().withDisability("Gehbehinderung").withUser(user);

		UserDAO ud = new UserDAO();

		int oldSize = ud.findAll().size();

		ud.save(user);

		assertEquals("saved", oldSize + 1, ud.findAll().size());
		assertTrue("has id", user.getId() > 0);
	}

	@Test
	@Transactional
	public void saveUserAndRatings() {

		Setting setting = new Setting();
		Item item1 = new Item();
		Item item2 = new Item();
		Item item3 = new Item();

		Calendar dateOfBirth = new GregorianCalendar();
		dateOfBirth.set(1990, 9, 21);

		UserDAO ud = new UserDAO();

		User user = new User();
		user.withFirstname("Bob").withEmail("bob@bobby.com").withDateOfBirth(dateOfBirth);
		user.withRatings(new Rating().withRating(1).withSetting(setting).withItem(item1),
				new Rating().withRating(0.2).withSetting(setting).withItem(item2),
				new Rating().withRating(0.2).withSetting(setting).withItem(item3));

		ud.save(user);
	}
}
