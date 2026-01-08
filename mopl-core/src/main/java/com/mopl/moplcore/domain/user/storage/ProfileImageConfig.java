package com.mopl.moplcore.domain.user.storage;

import java.nio.file.Path;

public interface ProfileImageConfig {
	Path getUploadRoot();

	String getPublicUrlPrefix();
}
