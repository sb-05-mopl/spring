package com.mopl.moplcore.domain.user.storage;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalProfileImageConfig implements ProfileImageConfig {

  private final Path uploadRoot;

  public LocalProfileImageConfig(@Value("${app.upload-dir:uploads}") Path uploadRoot) {
    this.uploadRoot = uploadRoot;
  }


  @Override
  public Path getUploadRoot() {
    return uploadRoot;
  }

  @Override
  public String getPublicUrlPrefix() {
    return "/uploads/profiles/";
  }
}
