package com.mopl.moplcore.domain.content.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mopl.moplcore.domain.content.converter.TypeDeserializer;

@JsonDeserialize(using = TypeDeserializer.class)
public enum Type {

	MOVIE("movie"),
	TV_SERIES("tvSeries"),
	SPORTS("sport");

	private final String jsonValue;

	Type(String jsonValue) {
		this.jsonValue = jsonValue;
	}

	@JsonValue
	public String toJson() {
		return jsonValue;
	}
}

