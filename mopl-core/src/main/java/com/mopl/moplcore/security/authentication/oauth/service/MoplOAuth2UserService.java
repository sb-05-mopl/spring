package com.mopl.moplcore.security.authentication.oauth.service;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

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
public class MoplOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oauth2User = super.loadUser(userRequest);

		return processOAuth2User(userRequest, oauth2User);
	}

	private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

		Map<String, Object> attributes = oauth2User.getAttributes();

		OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

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
