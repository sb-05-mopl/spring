package com.mopl.moplcore.domain.playlist.repository;

import java.util.List;

import com.mopl.moplcore.domain.playlist.dto.PlaylistSearchRequest;
import com.mopl.moplcore.domain.playlist.entity.Playlist;

public interface PlaylistRepositoryCustom {
	List<Playlist> searchPlaylists(PlaylistSearchRequest request);
	Long countPlaylists(PlaylistSearchRequest request);
}