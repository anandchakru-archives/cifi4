package com.anandchakru.app.model.webhook.github;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
public class Assets implements Serializable {
	private String name;
	private String url;
	private String browser_download_url;
}