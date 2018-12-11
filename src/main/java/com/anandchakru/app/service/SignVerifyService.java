package com.anandchakru.app.service;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SignVerifyService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public boolean verify(String source, String signFromGithub, String token) {
		if (token == null || token.length() == 0 || source == null || source.length() == 0) {
			return false;
		}
		if (StringUtils.equals(sign(source, token), signFromGithub)) {
			return true;
		}
		logger.warn("No match signFromGithub:" + signFromGithub);
		return false;
	}

	public String sign(String source, String token) {
		return "sha1=" + new HmacUtils(HmacAlgorithms.HMAC_SHA_1, token).hmacHex(source);
	}
}