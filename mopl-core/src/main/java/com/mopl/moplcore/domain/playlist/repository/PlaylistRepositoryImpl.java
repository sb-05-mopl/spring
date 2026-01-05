package com.mopl.moplcore.domain.playlist.repository;

import java.util.List;
import java.util.UUID;

import com.mopl.moplcore.domain.playlist.dto.PlaylistSearchRequest;
import com.mopl.moplcore.domain.playlist.entity.Playlist;
import com.mopl.moplcore.domain.playlist.entity.QPlaylist;
import com.mopl.moplcore.domain.playlist.entity.QPlaylistSubscription;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlaylistRepositoryImpl implements PlaylistRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Playlist> searchPlaylists(PlaylistSearchRequest request) {
		QPlaylist playlist = QPlaylist.playlist;

		return queryFactory
			.selectFrom(playlist)
			.leftJoin(playlist.owner).fetchJoin()
			.where(
				keywordLike(request.getKeywordLike()),
				ownerIdEqual(request.getOwnerIdEqual()),
				subscriberIdEqual(request.getSubscriberIdEqual())
			)
			.orderBy(getOrderSpecifier(request))
			.limit(request.getLimit() + 1)
			.fetch();
	}

	@Override
	public Long countPlaylists(PlaylistSearchRequest request) {
		QPlaylist playlist = QPlaylist.playlist;

		return queryFactory
			.select(playlist.count())
			.from(playlist)
			.where(
				keywordLike(request.getKeywordLike()),
				ownerIdEqual(request.getOwnerIdEqual()),
				subscriberIdEqual(request.getSubscriberIdEqual())
			)
			.fetchOne();
	}

	private BooleanExpression keywordLike(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return null;
		}
		QPlaylist playlist = QPlaylist.playlist;
		return playlist.title.containsIgnoreCase(keyword)
			.or(playlist.description.containsIgnoreCase(keyword));
	}

	private BooleanExpression ownerIdEqual(UUID ownerId) {
		if (ownerId == null) {
			return null;
		}
		return QPlaylist.playlist.owner.id.eq(ownerId);
	}

	private BooleanExpression subscriberIdEqual(UUID subscriberId) {
		if (subscriberId == null) {
			return null;
		}

		QPlaylist playlist = QPlaylist.playlist;
		QPlaylistSubscription subscription = QPlaylistSubscription.playlistSubscription;

		return playlist.id.in(
			JPAExpressions
				.select(subscription.playlist.id)
				.from(subscription)
				.where(subscription.user.id.eq(subscriberId))
		);
	}

	private OrderSpecifier<?> getOrderSpecifier(PlaylistSearchRequest request) {
		QPlaylist playlist = QPlaylist.playlist;

		boolean isAsc = request.getSortDirection() == PlaylistSearchRequest.SortDirection.ASCENDING;

		return switch (request.getSortBy()) {
			case updatedAt -> isAsc ? playlist.updatedAt.asc() : playlist.updatedAt.desc();
			case subscribeCount -> throw new UnsupportedOperationException("subscribeCount 정렬은 아직 미구현");
		};
	}
}