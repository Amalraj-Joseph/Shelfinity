/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.openapi;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.junit.jupiter.api.Test;

class TaggingFilterTest {

    private final TaggingFilter filter = new TaggingFilter();

    @Test
    void filterOpenAPI_nullModel_doesNothing() {
        filter.filterOpenAPI(null);
    }

    @Test
    void filterOpenAPI_nullPaths_doesNothing() {
        OpenAPI model = mock(OpenAPI.class);
        when(model.getPaths()).thenReturn(null);

        filter.filterOpenAPI(model);
    }

    @Test
    void filterOpenAPI_tagsKnownPrefixOperations() {
        OpenAPI model = mock(OpenAPI.class);
        Paths paths = mock(Paths.class);
        PathItem pathItem = mock(PathItem.class);
        Operation getOp = mock(Operation.class);
        Operation postOp = mock(Operation.class);

        Map<String, PathItem> pathItems = new LinkedHashMap<>();
        pathItems.put("/books/{id}", pathItem);

        when(model.getPaths()).thenReturn(paths);
        when(paths.getPathItems()).thenReturn(pathItems);
        when(pathItem.getGET()).thenReturn(getOp);
        when(pathItem.getPOST()).thenReturn(postOp);
        when(getOp.getTags()).thenReturn(null);
        when(postOp.getTags()).thenReturn(List.of("Books"));

        filter.filterOpenAPI(model);

        verify(getOp).setTags(List.of("Books"));
        verify(postOp, never()).setTags(anyList());
    }

    @Test
    void filterOpenAPI_leavesUnrecognizedPathsUntagged() {
        OpenAPI model = mock(OpenAPI.class);
        Paths paths = mock(Paths.class);
        PathItem pathItem = mock(PathItem.class);

        Map<String, PathItem> pathItems = new LinkedHashMap<>();
        pathItems.put("/unmapped-resource", pathItem);

        when(model.getPaths()).thenReturn(paths);
        when(paths.getPathItems()).thenReturn(pathItems);

        filter.filterOpenAPI(model);

        verify(pathItem, never()).getGET();
    }

    @Test
    void filterOpenAPI_skipsNullOperationsOnAPathItem() {
        OpenAPI model = mock(OpenAPI.class);
        Paths paths = mock(Paths.class);
        PathItem pathItem = mock(PathItem.class);

        Map<String, PathItem> pathItems = new LinkedHashMap<>();
        pathItems.put("/health", pathItem);

        when(model.getPaths()).thenReturn(paths);
        when(paths.getPathItems()).thenReturn(pathItems);
        when(pathItem.getGET()).thenReturn(null);

        filter.filterOpenAPI(model);
    }
}
