package com.anandchakru.app.model.webhook.github;

import java.io.Serializable;
import java.util.List;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
public class Release implements Serializable {
	private List<Assets> assets = Lists.newArrayList();
}