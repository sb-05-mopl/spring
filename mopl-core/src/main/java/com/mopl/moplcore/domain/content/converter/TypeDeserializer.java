package com.mopl.moplcore.domain.content.converter;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.mopl.moplcore.domain.content.entity.Type;

public class TypeDeserializer extends JsonDeserializer<Type> {
	@Override
	public Type deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String value = p.getText();
		if (value == null || value.isBlank())
			return null;

		String normalized = value.replaceAll("([a-z])([A-Z])", "$1_$2").replace("-", "_").toUpperCase();

		if (normalized.equals("SPORT") || normalized.equals("SPORTS")) {
			return Type.SPORTS;
		}

		try {
			return Type.valueOf(normalized);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid type: " + value);
		}
	}
}