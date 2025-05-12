package com.cleanengine.coin.user.info.application;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.DecimalFormat;

public class PlainDoubleSerializer extends JsonSerializer<Double> {
    private static final DecimalFormat df = new DecimalFormat("0.################");

    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumber(df.format(value));
    }
}