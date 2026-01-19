package com.mopl.moplcore.global.service;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

	private final AmazonS3 amazonS3;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
		"jpg", "jpeg", "png", "gif", "webp"
	);

	public String upload(MultipartFile file) {
		validateFile(file);

		String originalFilename = file.getOriginalFilename();
		String extension = extractExtension(originalFilename);
		String fileName = "contents/" + UUID.randomUUID() + "." + extension;

		try {
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(file.getContentType());
			metadata.setContentLength(file.getSize());

			PutObjectRequest putObjectRequest = new PutObjectRequest(
				bucket,
				fileName,
				file.getInputStream(),
				metadata
			);

			amazonS3.putObject(putObjectRequest);

			String fileUrl = amazonS3.getUrl(bucket, fileName).toString();
			log.info("S3 파일 업로드 성공: {}", fileUrl);

			return fileUrl;

		} catch (IOException e) {
			log.error("S3 파일 업로드 실패: {}", originalFilename, e);
			throw new RuntimeException("파일 업로드에 실패했습니다.", e);
		}
	}

	public void delete(String fileUrl) {
		if (fileUrl == null || fileUrl.isEmpty()) {
			log.warn("S3 삭제 시도: fileUrl이 null 또는 empty");
			return;
		}

		try {
			String fileName = extractFileNameFromUrl(fileUrl);
			log.info("S3 파일 삭제 시도: bucket={}, fileName={}", bucket, fileName);

			amazonS3.deleteObject(bucket, fileName);

			log.info("S3 파일 삭제 성공: {}", fileName);

		} catch (Exception e) {
			log.error("S3 파일 삭제 실패: url={}, error={}", fileUrl, e.getMessage(), e);
		}
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

	private String extractFileNameFromUrl(String fileUrl) {
		try {
			log.info("URL 파싱 시도: {}", fileUrl);

			URL url = new URL(fileUrl);
			String path = url.getPath();

			log.info("추출된 경로: {}", path);

			if (path.startsWith("/")) {
				path = path.substring(1);
			}

			log.info("최종 파일 key: {}", path);
			return path;

		} catch (Exception e) {
			log.error("URL 파싱 실패: {}", fileUrl, e);
			throw new IllegalArgumentException("잘못된 S3 URL 형식입니다: " + fileUrl, e);
		}
	}
}