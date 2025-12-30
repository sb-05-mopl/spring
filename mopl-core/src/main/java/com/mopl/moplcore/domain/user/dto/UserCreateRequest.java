package com.mopl.moplcore.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
    @NotBlank
    String name,
    @NotBlank
    String email,
    @NotBlank
    String password

) {

}
