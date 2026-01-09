package com.mopl.moplcore.domain.user.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mopl.moplcore.domain.user.dto.AdminUserSearchRequest;
import com.mopl.moplcore.domain.user.dto.CursorResponseUserDto;
import com.mopl.moplcore.domain.user.dto.UpdateLockRequest;
import com.mopl.moplcore.domain.user.dto.UpdatePasswordRequest;
import com.mopl.moplcore.domain.user.dto.UpdateRoleRequest;
import com.mopl.moplcore.domain.user.dto.UserCreateRequest;
import com.mopl.moplcore.domain.user.dto.UserDto;
import com.mopl.moplcore.domain.user.dto.UserUpdateRequest;
import com.mopl.moplcore.domain.user.service.UserService;
import com.mopl.moplcore.security.jwt.registry.JwtRegistry;
import com.mopl.moplcore.security.principal.MoplUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;
	private final JwtRegistry jwtRegistry;

	@PostMapping
	public ResponseEntity<UserDto> singUp(
		@RequestBody @Valid UserCreateRequest dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.signUp(dto));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<UserDto> findById(@PathVariable("userId") UUID userId) {
		return ResponseEntity.ok(userService.findById(userId));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CursorResponseUserDto> findAll(
		@Valid @ModelAttribute AdminUserSearchRequest request
	) {
		return ResponseEntity.ok(userService.findUsers(request));
	}

	@PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UserDto> updateProfile(
		@PathVariable UUID userId,
		@RequestPart("request") @Valid UserUpdateRequest request,
		@RequestPart(value = "image", required = false) MultipartFile image,
		@AuthenticationPrincipal MoplUserDetails userDetails
	) {
		return ResponseEntity.ok(
			userService.updateProfile(userId, userDetails.getUserDto().getId(), request, image)
		);
	}

	@PatchMapping(value = "/{userId}/password")
	public ResponseEntity<Void> updatePassword(
		@PathVariable UUID userId,
		@RequestBody UpdatePasswordRequest dto
	) {
		userService.updatePassword(userId, dto.getPassword());
		jwtRegistry.invalidateJwtInformationByUserId(userId);
		return ResponseEntity.ok().build();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping(value = "/{userId}/role")
	public ResponseEntity<Void> updateRole(
		@PathVariable UUID userId,
		@RequestBody UpdateRoleRequest dto
	) {
		userService.updateRole(dto.getRole(), userId);
		jwtRegistry.invalidateJwtInformationByUserId(userId);
		return ResponseEntity.ok().build();
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping(value = "/{userId}/locked")
	public ResponseEntity<Void> updateLock(
		@PathVariable UUID userId,
		@RequestBody UpdateLockRequest dto
	) {
		userService.updateLock(dto.isLocked(), userId);
		jwtRegistry.invalidateJwtInformationByUserId(userId);
		return ResponseEntity.ok().build();
	}
}
