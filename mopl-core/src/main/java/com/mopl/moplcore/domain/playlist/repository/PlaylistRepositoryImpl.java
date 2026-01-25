package com.mopl.moplcore.domain.playlist.repository;

import java.time.Instant;
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
import com.querydsl.core.types.Order;

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
				subscriberIdEqual(request.getSubscriberIdEqual()),
				cursorCondition(request)
			)
			.orderBy(
				getOrderSpecifier(request),
				playlist.id.asc()  // 동점 처리
			)
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

	private BooleanExpression cursorCondition(PlaylistSearchRequest request) {
		String cursor = request.getCursor();
		UUID idAfter = request.getIdAfter();

		if (cursor == null && idAfter == null) {
			return null;
		}

		QPlaylist playlist = QPlaylist.playlist;
		QPlaylistSubscription subscription = QPlaylistSubscription.playlistSubscription;
		boolean isAsc = request.getSortDirection() == PlaylistSearchRequest.SortDirection.ASCENDING;

		return switch (request.getSortBy()) {
			case updatedAt -> {
				if (cursor != null) {
					CursorData data = decodeCursor(cursor);
					Instant cursorUpdatedAt = Instant.parse(data.value);

					if (isAsc) {
						yield playlist.updatedAt.gt(cursorUpdatedAt)
							.or(playlist.updatedAt.eq(cursorUpdatedAt)
								.and(playlist.id.gt(data.id)));
					} else {
						yield playlist.updatedAt.lt(cursorUpdatedAt)
							.or(playlist.updatedAt.eq(cursorUpdatedAt)
								.and(playlist.id.gt(data.id)));
					}
				} else if (idAfter != null) {
					yield playlist.id.gt(idAfter);
				}
				yield null;
			}
			case subscribeCount -> {
				if (cursor != null) {
					CursorData data = decodeCursor(cursor);
					Long cursorSubscribeCount = Long.parseLong(data.value);

					var subscribeCountExpr = JPAExpressions
						.select(subscription.count())
						.from(subscription)
						.where(subscription.playlist.id.eq(playlist.id));

					if (isAsc) {
						yield subscribeCountExpr.gt(cursorSubscribeCount)
							.or(subscribeCountExpr.eq(cursorSubscribeCount)
								.and(playlist.id.gt(data.id)));
					} else {
						yield subscribeCountExpr.lt(cursorSubscribeCount)
							.or(subscribeCountExpr.eq(cursorSubscribeCount)
								.and(playlist.id.gt(data.id)));
					}
				} else if (idAfter != null) {
					yield playlist.id.gt(idAfter);
				}
				yield null;
			}
		};
	}

	private OrderSpecifier<?> getOrderSpecifier(PlaylistSearchRequest request) {
		QPlaylist playlist = QPlaylist.playlist;
		QPlaylistSubscription subscription = QPlaylistSubscription.playlistSubscription;

		boolean isAsc = request.getSortDirection() == PlaylistSearchRequest.SortDirection.ASCENDING;

		return switch (request.getSortBy()) {
			case updatedAt -> isAsc ? playlist.updatedAt.asc() : playlist.updatedAt.desc();
			case subscribeCount -> {
				var subscribeCountExpr = JPAExpressions
					.select(subscription.count())
					.from(subscription)
					.where(subscription.playlist.id.eq(playlist.id));

				yield new OrderSpecifier<>(
					isAsc ? Order.ASC : Order.DESC,
					subscribeCountExpr
				);
			}
		};
	}

	private CursorData decodeCursor(String cursor) {
		try {
			byte[] decoded = java.util.Base64.getUrlDecoder().decode(cursor);
			String raw = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
			String[] parts = raw.split("\\|");

			if (parts.length != 2) {
				throw new IllegalArgumentException("Invalid cursor format");
			}

			return new CursorData(UUID.fromString(parts[0]), parts[1]);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid cursor: " + cursor, e);
		}
	}

	private record CursorData(UUID id, String value) {}
}