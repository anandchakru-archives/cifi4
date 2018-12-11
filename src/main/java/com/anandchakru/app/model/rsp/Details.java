package com.anandchakru.app.model.rsp;

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
public class Details implements Serializable {
	private String startTime;
	private String endTime;
	private String log;
}