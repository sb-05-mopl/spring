package com.mopl.moplcore.domain.user.controller;

import com.mopl.moplcore.domain.user.dto.UserCreateRequest;
import com.mopl.moplcore.domain.user.dto.UserDto;
import com.mopl.moplcore.domain.user.entity.User;
import com.mopl.moplcore.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> singUp(@RequestBody @Valid UserCreateRequest userCreateRequest) {
    User signUpUser = userService.signUp(userCreateRequest.name(), userCreateRequest.email(),
        userCreateRequest.password());
    UserDto userDto = new UserDto(
        signUpUser.getId(),
        signUpUser.getCreatedAt(),
        signUpUser.getEmail(),
        signUpUser.getName(),
        signUpUser.getProfileImageUrl(),
        signUpUser.getRole(),
        signUpUser.isLocked()
    );
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(userDto);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> findById(@PathVariable("userId") UUID userId) {
    return ResponseEntity.ok(userService.findById(userId));
  }
}
