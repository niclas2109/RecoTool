package com.doccuty.radarplus.model.util;

import java.io.IOException;
import java.util.Date;

import com.doccuty.radarplus.model.Image;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * This deserialzer is used to deserialize an image from Base64 to byte[].
 * @author Niclas Kannengießer
 *
 */

public class ImageDeserializer extends StdDeserializer<Image> {

	private static final long serialVersionUID = -5510353102817291511L;

	public ImageDeserializer() {
		super(Image.class);
	}

	@Override
	public Image deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode node = p.getCodec().readTree(p);

		long id = 0;
		if (node.has("id"))
			id = node.get("id").asLong();

		String base64 = null;
		if (node.has("image"))
			base64 = node.get("image").asText();

		String filename = null;
		if (node.has("filename"))
			filename = node.get("filename").asText();

		String filetype = null;
		if (node.has("filetype"))
			filetype = node.get("filetype").asText();

		Date uploadedAt = null;
		if (node.has("uploadedAt"))
			uploadedAt = new Date(node.get("uploadedAt").asLong());

		Image img = new Image().withId(id).withFilename(filename).withFiltype(filetype).withImage(Base64.decode(base64))
				.withUploadedAt(uploadedAt);

		return img;
	}
}