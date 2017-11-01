package com.doccuty.radarplus.json;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import com.doccuty.radarplus.model.RecoTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class JSONTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public JSONTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(JSONTest.class);
	}

	public void testApp() {

		RecoTool app = new RecoTool();
		
		Duration d = Duration.ofMinutes(60);
		
		app.setEvaluationDuration(d);
		app.setMaxNumOfItems(23);
		app.setRandomizeItemGeoposition(true);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		
		long version = 0;
		boolean randomize = false;
		int maxNumOfItems = 0;
		long evaluationDuration = 0;
		
		try {
			File f = new File(getClass().getClassLoader().getResource("settings/appSettings.json").getPath());
			
			JsonNode json = mapper.createObjectNode();
			((ObjectNode) json).put("version", 1);
			((ObjectNode) json).put("randomizeItemGeoposition", true);
			((ObjectNode) json).put("maxNumOfItems", 20);
			((ObjectNode) json).put("evaluationDuration", 20);
			
			mapper.writeValue(f, json);
			
			
			JsonNode jsonNode = mapper.readTree(f);

			version = jsonNode.get("version").asLong();
			randomize = jsonNode.get("randomizeItemGeoposition").asBoolean();
			maxNumOfItems = jsonNode.get("maxNumOfItems").asInt();
			evaluationDuration = jsonNode.get("evaluationDuration").asLong();

		} catch (IOException e) {
			e.printStackTrace();
		}

		assertEquals("", version, 1);
		assertEquals("", randomize, true);
		assertEquals("", maxNumOfItems, 20);
		assertEquals("", evaluationDuration, 20);
	}
}
