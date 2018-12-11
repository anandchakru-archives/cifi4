package com.anandchakru.app.model.prop;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
public class Apps implements Serializable {
	/**
	 * OAuthToken used to download asset from private github repo. Setup @
	 * github.com/settings/tokens
	 * 
	 */
	private String githubOauthToken;
	/**
	 * Secret you enter @ Secret field in
	 * https://github.com/<user>/<repo>/settings/hooks/new
	 */
	private String githubWebhookSecret;
	/**
	 * Assets from this url only will be deployed. In case githubWebhookSecret is
	 * compromised, this will restrict unauthorized jars deployed.
	 */
	private String allowedAssetUrlPrefixes;
	/**
	 * Command to execute to shutdown the target application
	 */
	private List<String> shutDownCommand;
	/**
	 * Command to execute to start the target application
	 */
	private List<String> startUpCommand;
}
