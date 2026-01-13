package com.mopl.moplcore.domain.content.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mopl.moplcore.domain.content.converter.TypeDeserializer;

@JsonDeserialize(using = TypeDeserializer.class)
public enum Type {
	MOVIE,
	TV_SERIES,
	SPORTS,
}
