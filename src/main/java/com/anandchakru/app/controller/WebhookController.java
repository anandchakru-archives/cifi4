package com.anandchakru.app.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.anandchakru.app.model.rsp.AppResponse;
import com.anandchakru.app.model.rsp.StringRsp;
import com.anandchakru.app.service.DeployerService;

@RestController
public class WebhookController {
	private static final String GH_EVENT = "release";
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private DeployerService deployer;

	@PostMapping("/wh/gh/{app}")
	public @ResponseBody AppResponse scmWebhook(@PathVariable String app, @RequestBody String payload) {
		if (StringUtils.equals(request.getHeader("X-GitHub-Event"), GH_EVENT)) {
			String key = deployer.genKey();
			deployer.deploy(key, app, request.getHeader("X-Hub-Signature"), payload);
			return new AppResponse(new StringRsp(key));
		} else {
			return new AppResponse(new StringRsp("NOT " + GH_EVENT));
		}
	}

	@GetMapping("/ops/status/{id}")
	public @ResponseBody AppResponse details(@PathVariable String id) {
		return deployer.details(id);
	}

	@GetMapping("/ops/shutdown/{app}")
	public @ResponseBody AppResponse shutdown(@PathVariable String app) {
		String opsToken = request.getHeader("x-cifi-token");
		String key = deployer.genKey();
		deployer.shutdown(key, app, opsToken);
		return new AppResponse(new StringRsp(key));
	}

	@GetMapping("/ops/startup/{app}/{asset}")
	public @ResponseBody AppResponse startup(@PathVariable String app, @PathVariable String asset) {
		String opsToken = request.getHeader("x-cifi-token");
		String key = deployer.genKey();
		deployer.startup(key, app, opsToken, asset);
		return new AppResponse(new StringRsp(key));
	}
}