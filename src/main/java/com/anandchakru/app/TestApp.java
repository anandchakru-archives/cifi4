package com.anandchakru.app;

import java.util.List;

import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.collect.Lists;

public class TestApp {
	public static void main(String[] args) {
		List<String> a = Lists.newArrayList("A", "B", "ASSET", "C", "D");
		a.set(a.indexOf("ASSET"), "B+");
		System.out.println(a.toString());

		System.out.println(UriComponentsBuilder.fromHttpUrl("https://someu.com/cpath/fpath?foo=bar").query("a=p")
				.query("b=q").build().toString());
	}
}
