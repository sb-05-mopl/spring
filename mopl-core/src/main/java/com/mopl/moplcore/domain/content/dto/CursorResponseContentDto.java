package com.mopl.moplcore.domain.content.dto;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
public class CursorResponseContentDto {

	private List<ContentDto> data;
	private String nextCursor;
	private UUID nextIdAfter;
	private boolean hasNext;
	private long totalCount;
	private String sortBy;
	private String sortDirection;

	public record Cursor(
		UUID id,
		Instant createdAt,
		Double averageRating,
		Long watcherCount
	) {
	}

	public static String encodeCursor(Cursor cursor) {
		String raw = cursor.id() + "|" + cursor.createdAt() + "|" +
			(cursor.averageRating() != null ? cursor.averageRating() : "") + "|" +
			(cursor.watcherCount() != null ? cursor.watcherCount() : "");

		return Base64.getUrlEncoder()
			.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
	}

	public static Cursor decodeCursor(String cursorStr) {
		try {
			String decoded = new String(Base64.getUrlDecoder().decode(cursorStr), StandardCharsets.UTF_8);
			String[] parts = decoded.split("\\|", -1);
			return new Cursor(
				UUID.fromString(parts[0]),
				Instant.parse(parts[1]),
				parts[2].isEmpty() ? null : Double.parseDouble(parts[2]),
				parts[3].isEmpty() ? null : Long.parseLong(parts[3])
			);
		} catch (Exception e) {
			log.error("Cursor decoding failed: {}", cursorStr);
			throw new IllegalArgumentException("Invalid cursor format");
		}
	}
}