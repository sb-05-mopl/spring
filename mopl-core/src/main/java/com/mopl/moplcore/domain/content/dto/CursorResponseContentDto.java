package com.mopl.moplcore.domain.content.dto;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

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

	public record Cursor(UUID id, Instant createdAt) {}

	public static String encodeCursor(UUID id, Instant createdAt) {
		String raw = id.toString() + "|" + createdAt.toString();
		return Base64.getUrlEncoder()
			.withoutPadding()
			.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
	}

	public static Cursor decodeCursor(String cursor) {
		try {
			String decoded = new String(
				Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8
			);
			String[] parts = decoded.split("\\|");
			return new Cursor(UUID.fromString(parts[0]), Instant.parse(parts[1]));
		} catch (Exception e) {
			throw new IllegalArgumentException("유효하지 않은 커서 형식", e);
		}
	}
}