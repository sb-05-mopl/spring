package com.mopl.moplcore.domain.review.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.mopl.moplcore.domain.review.dto.ReviewDto;
import com.mopl.moplcore.domain.review.entity.Review;

@Mapper(
	componentModel = "spring",
	unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ReviewMapper {

	@Mapping(source = "content.id", target = "contentId")
	@Mapping(source = "author.id", target = "author.userId")
	@Mapping(source = "author.name", target = "author.name")
	@Mapping(source = "author.profileImageUrl", target = "author.profileImageUrl")
	ReviewDto toDto(Review review);
}