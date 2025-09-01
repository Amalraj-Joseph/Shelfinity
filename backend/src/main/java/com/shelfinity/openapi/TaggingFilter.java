/*
 * Copyright (c) 2025 Shadow-Codex
 * Licensed under the MIT License.
 */
package com.shelfinity.openapi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;


public class TaggingFilter implements OASFilter {

	public void filterOpenAPI(OpenAPI model) {
		if (model == null || model.getPaths() == null) {
			return;
		}
		


		Map<String, String> prefixToTag = new HashMap<>();
		prefixToTag.put("/books", "Books");
		prefixToTag.put("/users", "Users");
		prefixToTag.put("/queues", "Queue");
		prefixToTag.put("/health", "Health");

		model.getPaths().getPathItems().forEach((path, pathItem) -> {
			String tagName = prefixToTag.entrySet().stream()
					.filter(e -> path.startsWith(e.getKey()))
					.map(Map.Entry::getValue)
					.findFirst()
					.orElse(null);
			if (tagName == null) {
				return;
			}

			addTag(pathItem.getGET(), tagName);
			addTag(pathItem.getPOST(), tagName);
			addTag(pathItem.getPUT(), tagName);
			addTag(pathItem.getDELETE(), tagName);
			addTag(pathItem.getPATCH(), tagName);
			addTag(pathItem.getHEAD(), tagName);
			addTag(pathItem.getOPTIONS(), tagName);
			addTag(pathItem.getTRACE(), tagName);
		});
	}

	private void addTag(Operation operation, String tag) {
		if (operation == null) {
			return;
		}
		java.util.List<String> tags = operation.getTags();
		if (tags == null || tags.isEmpty()) {
			operation.setTags(new java.util.ArrayList<>(Arrays.asList(tag)));
			return;
		}
		if (!tags.contains(tag)) {
			tags.add(tag);
		}
	}
}


