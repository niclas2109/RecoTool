package com.doccuty.radarplus.model.util;

import java.io.IOException;

import com.doccuty.radarplus.model.Image;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * This serialzer is used to serialize an image from byte[] to Base64.
 * @author Niclas Kannengießer
 *
 */

public class ImageSerializer extends StdSerializer<Image> {

    private static final long serialVersionUID = -5510353102817291511L;

    public ImageSerializer() {
        super(Image.class);
    }

    @Override
    public void serialize(Image value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", value.getId());
        gen.writeStringField("filename", value.getFilename());
        gen.writeStringField("filetype", value.getFiletype());
        gen.writeObjectField("uploadedAt", value.getUploadedAt().getTime());
        gen.writeStringField("image", Base64.encode(value.getImage()));
        gen.writeEndObject();
    }
}