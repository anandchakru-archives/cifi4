package com.anandchakru.app.config;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.anandchakru.app.model.prop.Apps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Component
@SuppressWarnings("serial")
@ConfigurationProperties(prefix = "cifi", ignoreUnknownFields = true)
public class CifiSettings implements Serializable {
	/**
	 * Secret used to shutdown/startup targetApp. If blank, a random value is
	 * generated on startup.
	 */
	private List<String> opsOauthToken;
	/**
	 * location where assets will be staged. eg: /var/www
	 */
	private String stageDir;
	/**
	 * Each apps with individual appname as key and their respective settings in
	 * value
	 */
	private Map<String, Apps> apps;

}