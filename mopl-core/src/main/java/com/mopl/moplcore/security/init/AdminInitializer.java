package com.mopl.moplcore.security.init;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

	private final InitService initService;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		initService.initAdmin();
		initService.initDefaultUser();
	}
}
