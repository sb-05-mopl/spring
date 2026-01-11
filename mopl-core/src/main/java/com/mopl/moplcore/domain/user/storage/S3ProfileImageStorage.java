package com.mopl.moplcore.domain.user.storage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile({"prod", "dev", "local"})
@RequiredArgsConstructor
public class S3ProfileImageStorage implements ProfileImageStorage {

	private final AmazonS3 amazonS3;

	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
		"jpg", "jpeg", "png", "gif", "webp"
	);

	private static final String PROFILE_IMAGE_DIR = "profile-images/";

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	@Override
	public String saveProfileImage(UUID userId, MultipartFile file) {
		validateFile(file);

		deleteExistingProfileImages(userId);

		String filename = generateFilename(userId, file);
		String key = PROFILE_IMAGE_DIR + filename;

		try {
			ObjectMetadata metadata = createMetadata(file);

			PutObjectRequest request = new PutObjectRequest(
				bucket,
				key,
				file.getInputStream(),
				metadata
			);

			amazonS3.putObject(request);

			return filename;

		} catch (IOException e) {
			log.error("Failed to upload profile image: userId={}, filename={}", userId, filename, e);
			throw new RuntimeException("Failed to upload file.", e);
		}
	}

	@Override
	public String getProfileImageUrl(String filename) {
		String key = PROFILE_IMAGE_DIR + filename;
		return amazonS3.getUrl(bucket, key).toString();
	}

	private void deleteExistingProfileImages(UUID userId) {
		String prefix = PROFILE_IMAGE_DIR + userId.toString();

		try {
			amazonS3.listObjects(bucket, prefix)
				.getObjectSummaries()
				.forEach(s3Object -> {
					try {
						amazonS3.deleteObject(bucket, s3Object.getKey());
					} catch (Exception e) {
						log.warn("Failed to delete profile image: key={}", s3Object.getKey(), e);
					}
				});
		} catch (Exception e) {
			log.warn("Failed to list existing profile images: userId={}", userId, e);
		}
	}

	private String generateFilename(UUID userId, MultipartFile file) {
		String extension = extractExtension(file.getOriginalFilename());
		return String.format("%s.%s", userId.toString(), extension);
	}

	private ObjectMetadata createMetadata(MultipartFile file) {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(file.getContentType());
		metadata.setContentLength(file.getSize());
		return metadata;
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("파일이 비어있습니다.");
		}

		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || !originalFilename.contains(".")) {
			throw new IllegalArgumentException("올바른 파일 형식이 아닙니다.");
		}

		String extension = extractExtension(originalFilename);
		if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new IllegalArgumentException(
				"지원하지 않는 파일 형식입니다. (지원: jpg, jpeg, png, gif, webp)"
			);
		}

		long maxSize = 10 * 1024 * 1024;
		if (file.getSize() > maxSize) {
			throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
		}
	}

	private String extractExtension(String filename) {
		int lastDotIndex = filename.lastIndexOf(".");
		if (lastDotIndex == -1) {
			throw new IllegalArgumentException("파일 확장자가 없습니다.");
		}
		return filename.substring(lastDotIndex + 1);
	}
}