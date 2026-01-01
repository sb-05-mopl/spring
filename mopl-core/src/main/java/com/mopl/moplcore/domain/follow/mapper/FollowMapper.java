package com.mopl.moplcore.domain.follow.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mopl.moplcore.domain.follow.dto.FollowDto;
import com.mopl.moplcore.domain.user.entity.Follow;

@Mapper(componentModel = "spring")
public interface FollowMapper {

	@Mapping(source = "follower.id", target = "followerId")
	@Mapping(source = "followee.id", target = "followeeId")
	FollowDto toDto(Follow follow);
}