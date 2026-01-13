package com.mopl.moplcore.domain.content.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.mopl.moplcore.domain.content.entity.Type;

@Component
public class StringToTypeConverter implements Converter<String, Type> {
	@Override
	public Type convert(String source) {
		if (source == null || source.isBlank()) return null;
		try {
			String normalized = source
				.replaceAll("([a-z])([A-Z])", "$1_$2")
				.replace("-", "_")
				.toUpperCase();

			if (normalized.equals("SPORT") || normalized.equals("SPORTS")) {
				return Type.SPORTS;
			}

			return Type.valueOf(normalized);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
