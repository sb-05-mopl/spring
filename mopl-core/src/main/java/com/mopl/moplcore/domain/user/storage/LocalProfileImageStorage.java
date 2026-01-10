package com.mopl.moplcore.domain.user.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Profile({"dev", "local"})
public class LocalProfileImageStorage implements ProfileImageStorage {

	private static final String PROFILE_DIR_NAME = "profiles";

	private final ProfileImageConfig config;

	public LocalProfileImageStorage(ProfileImageConfig config) {
		this.config = config;
	}

	@Override
	public String saveProfileImage(UUID userId, MultipartFile file) {
		try {
			Path profileDir = config.getUploadRoot()
				.toAbsolutePath()
				.normalize()
				.resolve(PROFILE_DIR_NAME);

			Files.createDirectories(profileDir);

			String ext = extractExtension(file.getOriginalFilename());
			String filename = userId + "_" + UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

			Path target = profileDir.resolve(filename);
			file.transferTo(target);

			return filename;
		} catch (IOException e) {
			throw new RuntimeException("Failed to save profile image", e);
		}
	}

	@Override
	public String getProfileImageUrl(String filename) {
		return config.getPublicUrlPrefix() + filename;
	}

	private String extractExtension(String originalFilename) {
		if (originalFilename == null)
			return "";
		int dot = originalFilename.lastIndexOf('.');
		if (dot < 0 || dot == originalFilename.length() - 1)
			return "";
		return originalFilename.substring(dot + 1);
	}
}
