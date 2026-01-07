package com.mopl.moplcore.domain.playlist.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mopl.moplcore.domain.playlist.entity.Playlist;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID>, PlaylistRepositoryCustom {

}
