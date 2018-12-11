package com.anandchakru.app.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.anandchakru.app.config.CifiSettings;
import com.anandchakru.app.model.rsp.AppResponse;
import com.anandchakru.app.model.rsp.Details;
import com.anandchakru.app.model.rsp.WebhookRsp;
import com.anandchakru.app.model.webhook.github.Assets;
import com.anandchakru.app.model.webhook.github.ReleasePayload;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@Service
public class DeployerService {
	private static final Integer MAX_RETRY_THRESHOLD = 5;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Gson gson = new Gson();
	private Cache<String, Map<String, Details>> cache = Caffeine.newBuilder().maximumSize(1000)
			.expireAfterWrite(7, TimeUnit.DAYS).recordStats()
			.removalListener(new com.github.benmanes.caffeine.cache.RemovalListener<String, Map<String, Details>>() {
				@Override
				public void onRemoval(String key, Map<String, Details> value, RemovalCause cause) {
					logger.info("Evicted: {} {} for {}", key, gson.toJson(value), cause);
				}
			}).build();

	@Autowired
	private SignVerifyService signVerifyService;
	@Autowired
	private CifiSettings settings;

	/**
	 * Fetch details of previously requests
	 * 
	 * @param key
	 * @return
	 */
	public AppResponse details(String key) {
		return new AppResponse(new WebhookRsp(cache.asMap().get(key)));
	}

	/**
	 * Generate key for cache
	 * 
	 * @return
	 */
	public String genKey() {
		String sKey = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 7);
		int i = 0;
		while (MAX_RETRY_THRESHOLD > i++ && cache.asMap().get(sKey) != null) {
			sKey = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 7);
		}
		return sKey;
	}

	/**
	 * <strong>Async</strong> Shutdown target Application represented by
	 * <code>app</code>
	 * 
	 * @param key
	 * @param app
	 * @param opsToken
	 */

	@Async
	public void shutdown(String key, String app, String opsToken) {
		Map<String, Details> details = Maps.newLinkedHashMap();
		Details shut = new Details();
		details.put("shut", shut);
		cache.put(key, details);
		shut.setStartTime(String.valueOf(System.currentTimeMillis()));
		if (isValidOpsToken(opsToken)) {
			shut.setLog(shutdown(app));
		} else {
			shut.setLog("invalid opsToken.");
		}
		shut.setEndTime(String.valueOf(System.currentTimeMillis()));
	}

	/**
	 * <strong>Async</strong> Startup target Application represented by
	 * <code>app</code>
	 * 
	 * @param key
	 * @param app
	 * @param opsToken
	 * @param asset
	 */
	@Async
	public void startup(String key, String app, String opsToken, String asset) {
		Map<String, Details> details = Maps.newLinkedHashMap();
		Details start = new Details();
		details.put("start", start);
		cache.put(key, details);
		start.setStartTime(String.valueOf(System.currentTimeMillis()));
		if (isValidOpsToken(opsToken)) {
			String assetDir = settings.getStageDir() + app + FileSystems.getDefault().getSeparator();
			String assetFullPath = assetDir + asset;
			if (Files.exists(Paths.get(assetFullPath))) {
				start.setLog(startup(app, assetFullPath));
			} else {
				start.setLog("invalid asset: " + assetFullPath);
			}
		} else {
			start.setLog("invalid opsToken.");
		}
		start.setEndTime(String.valueOf(System.currentTimeMillis()));
	}

	/**
	 * <strong>Async</strong> - deploys asset.
	 * <ol>
	 * <li>Prepares directory</li>
	 * <li>Downloads asset</li>
	 * <li>Shutdown</li>
	 * <li>Startup</li>
	 * <li>Cache the log</li>
	 * </ol>
	 * 
	 * @param key
	 * @param app
	 * @param signFromgithub
	 * @param payload
	 */
	@Async
	public void deploy(String key, String app, String signFromgithub, String payload) {
		Map<String, Details> details = Maps.newLinkedHashMap();
		Details dir = new Details();
		Details dl = new Details();
		Details shut = new Details();
		Details start = new Details();
		Details excep = new Details();
		details.put("dir", dir);
		details.put("dl", dl);
		details.put("shut", shut);
		details.put("start", start);
		details.put("excep", excep);
		cache.put(key, details);
		ReleasePayload payloadObj = null;
		try {
			payloadObj = gson.fromJson(payload, ReleasePayload.class);
		} catch (JsonSyntaxException e) {
			logger.warn("error @ payload: {}", payload);
		}
		if (payloadObj == null) {
			details.put("excep", new Details("", "", "Invalid/Empty Payload " + payload));
			return;
		}
		if (!signVerifyService.verify(payload, signFromgithub, settings.getApps().get(app).getGithubWebhookSecret())) {
			details.put("excep", new Details("", "", "Invalid X-Hub-Signature " + signFromgithub));
			return;
		}
		if (payloadObj.getRelease() == null) {
			details.put("excep", new Details("", "", "Invalid/Empty Release info " + payload));
			return;
		}
		if (payloadObj.getRelease().getAssets() == null) {
			details.put("excep", new Details("", "", "Invalid/Empty Assets " + payload));
			return;
		}
		if (payloadObj.getRelease().getAssets().size() == 0) {
			details.put("excep", new Details("", "", "Empty Asset " + payload));
			return;
		}
		Assets asset = payloadObj.getRelease().getAssets().get(0);
		String assetDir = settings.getStageDir() + app + FileSystems.getDefault().getSeparator();
		String assetFullPath = assetDir + asset.getName();

		//
		// Create Dir if required
		dir.setStartTime(String.valueOf(System.currentTimeMillis()));
		dir.setLog(prepDir(assetDir));
		dir.setEndTime(String.valueOf(System.currentTimeMillis()));
		//
		// Download
		dl.setStartTime(String.valueOf(System.currentTimeMillis()));
		dl.setLog(download(asset, assetFullPath, settings.getApps().get(app).getGithubOauthToken()));
		dl.setEndTime(String.valueOf(System.currentTimeMillis()));
		//
		// Shutdown
		shut.setStartTime(String.valueOf(System.currentTimeMillis()));
		shut.setLog(shutdown(app));
		shut.setEndTime(String.valueOf(System.currentTimeMillis()));
		//
		// Startup
		start.setStartTime(String.valueOf(System.currentTimeMillis()));
		if (Files.exists(Paths.get(assetFullPath))) {
			start.setLog(startup(app, assetFullPath));
		} else {
			start.setLog("invalid asset: " + assetFullPath);
		}
		start.setEndTime(String.valueOf(System.currentTimeMillis()));
		logger.debug("deploy complete: {} {}", key, gson.toJson(details));
	}

	/**
	 * Validate opsToken coming from request
	 * 
	 * @param opsToken
	 * @return
	 */
	private boolean isValidOpsToken(String opsToken) {
		if (StringUtils.isEmpty(opsToken)) {
			return false;
		}
		for (String validOpsToken : settings.getOpsOauthToken()) {
			if (StringUtils.equals(validOpsToken, opsToken)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Ensure the target directory exists. If not create them.
	 * 
	 * @param assetDir
	 * @return
	 */
	private String prepDir(String assetDir) {
		try {
			File dir = new File(assetDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			return assetDir;
		} catch (Exception e) {
			logger.warn("error @ prep-dir", e);
			return e.getMessage();
		}
	}

	/**
	 * Download asset from github releases. Handles both public and private repos.
	 * <br/>
	 * <br/>
	 * For public repo, it uses <code>asset.browser_download_url</code> eg: <br/>
	 * 
	 * <code>curl -o a.jar -JL -H 'Accept: application/octet-stream' https://github.com/anandchakru/rason/releases/download/1.0.26/rason-1.0.26.jar</code>
	 * <br/>
	 * <br/>
	 * For private repo, it uses <code>asset.url</code> and
	 * <code>githubOauthToken</code> eg: <br/>
	 * <code>curl -o a.jar -JL -H 'Accept: application/octet-stream' https://api.github.com/repos/anandchakru/<private_repo>/releases/assets/<asset_id>?access_token=<github_token></code>
	 * 
	 * @param asset            - asset to download
	 * @param assetFullPath    - target absolute file name. eg:
	 *                         /apps/rason/rason-1.0.26.jar
	 * @param githubOauthToken - token to use for private repo (get one from
	 *                         <a href=
	 *                         "https://github.com/settings/tokens">settings/tokens</a>).
	 * 
	 * @return
	 */
	private String download(Assets asset, String assetFullPath, String githubOauthToken) {
		File assetFile = new File(assetFullPath);
		if (assetFile.exists()) {
			return "file exists @ " + assetFullPath;
		}
		String url = (StringUtils.isEmpty(githubOauthToken)
				? UriComponentsBuilder.fromHttpUrl(asset.getBrowser_download_url()).build()
				: UriComponentsBuilder.fromHttpUrl(asset.getUrl()).query("access_token=" + githubOauthToken).build())
						.toString();
		List<String> downloadCommand = Lists.newArrayList("curl", "-o", assetFullPath, "-JL", "-H",
				"'Accept: application/octet-stream'", url);
		try {
			return execute(downloadCommand, true);
		} catch (Exception e) {
			logger.warn("error @ download private", e);
			return e.getMessage();
		}
	}

	/**
	 * Shutdown target Application represented by <code>app</code>
	 * 
	 * @param app - target Application
	 * @return
	 */
	private String shutdown(String app) {
		try {
			return execute(settings.getApps().get(app).getShutDownCommand(), true);
		} catch (Exception e) {
			logger.warn("error @ shutdown", e);
			return e.getMessage();
		}
	}

	/**
	 * Startup target Application represented by <code>app</code>
	 * 
	 * @param app           - target Application
	 * @param assetFullPath - asset (eg: /apps/rason/rason-1.0.26.jar) to be
	 *                      deployed
	 * @return
	 */
	private String startup(String app, String assetFullPath) {
		List<String> startUpCommand = Lists.newArrayList(settings.getApps().get(app).getStartUpCommand());
		int indexOf = startUpCommand.indexOf("ASSET");
		if (startUpCommand.size() > 0 && indexOf > 0 && indexOf < startUpCommand.size()) {
			startUpCommand.set(indexOf, assetFullPath);
		}
		try {
			return execute(startUpCommand, false);
		} catch (Exception e) {
			logger.warn("error @ startup", e);
			return e.getMessage();
		}
	}

	/**
	 * executes <code>script</code> in unix based shell. Supports *nix environment
	 * 
	 * @param script - shell script
	 * @param wait   - wait for script to finish or not
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private String execute(List<String> script, boolean wait) throws IOException, InterruptedException {
		logger.info("executing " + script);
		Process process = new ProcessBuilder().command(script).start();
		if (wait) {
			process.waitFor();
		} else {
			return "[async-execute]";
		}
		return "Output:" + IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8) + "\nError:"
				+ IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);
	}
}
