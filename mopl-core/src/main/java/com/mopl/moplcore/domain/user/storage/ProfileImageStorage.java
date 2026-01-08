package com.mopl.moplcore.domain.user.storage;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStorage {

  String saveProfileImage(UUID userId, MultipartFile file);
  String getProfileImageUrl(String filename);

}
