package com.anandchakru.app.model.rsp;

import java.io.Serializable;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.event.PushPayload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
public class WebhookReleasePayLoad extends PushPayload implements Serializable {
	private String after;
	private Repository repository;
}
