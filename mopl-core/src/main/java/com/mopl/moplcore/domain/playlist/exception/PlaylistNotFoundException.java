package com.mopl.moplcore.domain.playlist.exception;

import java.util.UUID;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class PlaylistNotFoundException extends BaseException {
	public PlaylistNotFoundException(UUID id) {
		super(ErrorCode.PLAYLIST_NOT_FOUND);
		addDetail("playlistId", id);
	}
}