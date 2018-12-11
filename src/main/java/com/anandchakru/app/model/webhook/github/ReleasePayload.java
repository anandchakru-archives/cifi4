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
public class ReleasePayload implements Serializable {
	private String action = "";
	private Release release = new Release();
}