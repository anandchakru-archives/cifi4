package com.anandchakru.app.service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.anandchakru.app.config.CifiSettings;
import com.anandchakru.app.model.prop.Apps;
import com.google.common.collect.Lists;

@Order(2)
@Service
public class PostInitRunner implements CommandLineRunner {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private CifiSettings settings;

	@Override
	public void run(String... args) throws Exception {
		warnGithubOauthToken();
	}

	private void warnGithubOauthToken() {
		Set<String> apps = settings.getApps().keySet();
		Iterator<String> iApps = apps.iterator();
		while (iApps.hasNext()) {
			String appName = iApps.next();
			Apps app = settings.getApps().get(appName);
			if (StringUtils.isEmpty(app.getGithubWebhookSecret())) {
				logger.warn(
						"DANGER: {} is not setup with Webhook Secret. Refers to the Secret field @ https://github.com/<user>/<repo>/settings/hooks/new",
						app);
			}
		}
		if (settings.getOpsOauthToken() == null || settings.getOpsOauthToken().size() == 0) {
			String oauthToken = UUID.randomUUID().toString().replaceAll("-", "");
			if (settings.getOpsOauthToken() == null) {
				List<String> opsOauthToken = Lists.newArrayList(oauthToken);
				settings.setOpsOauthToken(opsOauthToken);
			} else {
				settings.getOpsOauthToken().add(oauthToken);
			}
			logger.warn("OpsOauthToken cannot be empty. Use:" + oauthToken);
		}
	}
}