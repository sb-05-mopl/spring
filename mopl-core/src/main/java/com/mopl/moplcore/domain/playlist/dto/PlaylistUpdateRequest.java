package com.mopl.moplcore.domain.playlist.dto;

public record PlaylistUpdateRequest(
	String title,
	String description
) {
}