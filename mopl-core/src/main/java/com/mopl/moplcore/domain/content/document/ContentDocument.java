package com.mopl.moplcore.domain.content.document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.mopl.moplcore.domain.content.entity.Type;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Document(indexName = "contents")
@Setting(settingPath = "elasticsearch/content-settings.json")
public class ContentDocument {

	@Id
	private String id;

	@Field(type = FieldType.Keyword)
	private Type type;

	@Field(type = FieldType.Text, analyzer = "nori")
	private String title;

	@Field(type = FieldType.Text, analyzer = "nori")
	private String description;

	@Field(type = FieldType.Keyword)
	private String thumbnailUrl;

	@Field(type = FieldType.Keyword)
	private List<String> tags;

	@Field(type = FieldType.Double)
	private Double averageRating;

	@Field(type = FieldType.Integer)
	private Integer reviewCount;

	@Field(type = FieldType.Long)
	private Long watcherCount;

	@Field(type = FieldType.Date)
	private LocalDate createdAt;

	@Field(type = FieldType.Date)
	private LocalDate updatedAt;

	public static ContentDocument from(UUID id, Type type, String title, String description, String thumbnailUrl,
		List<String> tags, Double averageRating, Integer reviewCount, Long watcherCount, Instant createdAt) {
		LocalDate createdAtLocal =
			createdAt != null ? LocalDate.ofInstant(createdAt, ZoneId.systemDefault()) : LocalDate.now();

		return ContentDocument.builder()
			.id(id.toString())
			.type(type)
			.title(title)
			.description(description)
			.thumbnailUrl(thumbnailUrl)
			.tags(tags != null ? tags : List.of())
			.averageRating(averageRating != null ? averageRating : 0.0)
			.reviewCount(reviewCount != null ? reviewCount : 0)
			.watcherCount(watcherCount != null ? watcherCount : 0L)
			.createdAt(createdAtLocal)
			.updatedAt(createdAtLocal)
			.build();
	}
}
