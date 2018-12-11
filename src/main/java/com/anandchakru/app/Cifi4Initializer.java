package com.anandchakru.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

import com.anandchakru.app.config.Cifi4Config;

@Import(value = { Cifi4Config.class })
@SpringBootApplication
public class Cifi4Initializer {
	private static final Logger logger = LoggerFactory.getLogger("com.anandchakru.app.Cifi4Initializer");

	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplicationBuilder(Cifi4Initializer.class).build(args);
		app.run();
		logger.debug("Initialized Cifi4Initializer.");
	}
}
