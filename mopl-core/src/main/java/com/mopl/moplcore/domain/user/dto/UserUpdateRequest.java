package com.mopl.moplcore.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
    @NotBlank
    String name
) {

}
