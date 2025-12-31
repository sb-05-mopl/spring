package com.mopl.moplcore.domain.user.dto;

import com.mopl.moplcore.domain.user.entity.Role;
import java.time.Instant;
import java.util.UUID;

public record UserDto(
    UUID id,
    Instant createdAt,
    String email,
    String name,
    String profileImageUrl,
    Role role,
    boolean locked
) {

}
