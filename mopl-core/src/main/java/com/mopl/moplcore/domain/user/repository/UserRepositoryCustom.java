package com.mopl.moplcore.domain.user.repository;

import java.util.List;
import java.util.UUID;

import com.mopl.moplcore.domain.user.dto.SortBy;
import com.mopl.moplcore.domain.user.dto.SortDirection;
import com.mopl.moplcore.domain.user.entity.Role;
import com.mopl.moplcore.domain.user.entity.User;

public interface UserRepositoryCustom {

	long countUsers(String emailLike, Role roleEqual, Boolean isLocked);

	List<User> findUsersWithCursor(
		String emailLike,
		Role roleEqual,
		Boolean isLocked,
		String cursor,
		UUID idAfter,
		int limit,
		SortBy sortBy,
		SortDirection sortDirection
	);
}
