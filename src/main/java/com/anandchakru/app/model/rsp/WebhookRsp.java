package com.anandchakru.app.model.rsp;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
public class WebhookRsp implements Response {
	private Map<String, Details> value = Maps.newLinkedHashMap();
}