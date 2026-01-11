package com.mopl.moplcore.domain.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mopl.moplcore.domain.user.dto.UserResponse;
import com.mopl.moplcore.domain.user.dto.UserSummary;
import com.mopl.moplcore.domain.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	UserResponse toDto(User user);

	@Mapping(target = "userId", source = "id")
	UserSummary toSummary(User user);
}
