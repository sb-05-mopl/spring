package com.mopl.moplcore.security.authentication.oauth.service;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mopl.moplcore.domain.user.dto.UserDto;
import com.mopl.moplcore.domain.user.entity.AuthProvider;
import com.mopl.moplcore.domain.user.entity.User;
import com.mopl.moplcore.domain.user.repository.UserRepository;
import com.mopl.moplcore.security.authentication.oauth.dto.OAuth2UserInfo;
import com.mopl.moplcore.security.core.principal.MoplUserDetails;
import com.mopl.moplcore.security.authentication.oauth.OAuth2UserInfoFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MoplOidcUserService extends OidcUserService {

	private final UserRepository userRepository;

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OidcUser oidcUser = super.loadUser(userRequest);

		try {
			return processOidcUser(userRequest, oidcUser);
		} catch (Exception ex) {
			throw new OAuth2AuthenticationException(ex.getMessage());
		}
	}

	private OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) {
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

		OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
			registrationId,
			oidcUser.getAttributes()
		);

		if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
			throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
		}

		User user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
			.map(existingUser -> updateExistingUser(existingUser, oAuth2UserInfo))
			.orElseGet(() -> registerNewUser(provider, oAuth2UserInfo));

		UserDto userDto = UserDto.from(user);

		return new MoplUserDetails(userDto, null);
	}

	private User registerNewUser(AuthProvider provider, OAuth2UserInfo oAuth2UserInfo) {
		User user = new User(
			oAuth2UserInfo.getName(),
			oAuth2UserInfo.getEmail(),
			null,
			provider
		);
		user.updateProfileImageUrl(oAuth2UserInfo.getPicture());
		return userRepository.save(user);
	}

	private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
		existingUser.updateName(oAuth2UserInfo.getName());
		existingUser.updateProfileImageUrl(oAuth2UserInfo.getPicture());
		return userRepository.save(existingUser);
	}
}