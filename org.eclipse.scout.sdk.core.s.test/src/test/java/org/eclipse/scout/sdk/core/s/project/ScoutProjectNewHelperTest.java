/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.project;

import static org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper.getSupportedArchetypeVersions;
import static org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper.getSupportedJavaVersions;
import static org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper.limitToLtsOrNewest;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.eclipse.scout.sdk.core.java.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class ScoutProjectNewHelperTest {

  @Test
  public void testGetSupportedJavaVersions() {
    var latest = ScoutApi.create(ApiVersion.LATEST).supportedJavaVersions();
    assertArrayEquals(latest, getSupportedJavaVersions(null));
    assertArrayEquals(latest, getSupportedJavaVersions(IMavenConstants.LATEST));
    assertArrayEquals(latest, getSupportedJavaVersions("latest"));
    assertArrayEquals(latest, getSupportedJavaVersions(""));
    assertArrayEquals(latest, getSupportedJavaVersions("--invalid--version--"));
    assertArrayEquals(new int[]{8, 11}, getSupportedJavaVersions("10"));
    assertArrayEquals(new int[]{8, 11}, getSupportedJavaVersions("11.4.5"));
    assertArrayEquals(new int[]{11}, getSupportedJavaVersions("22.0.2"));
    assertArrayEquals(new int[]{11}, getSupportedJavaVersions("22.0.10"));
    assertArrayEquals(new int[]{11, 17}, getSupportedJavaVersions("22.0.11"));
    assertArrayEquals(new int[]{11, 17}, getSupportedJavaVersions("22"));
  }

  @Test
  @Tag("IntegrationTest")
  public void testGetSupportedArchetypeVersions() throws IOException {
    var javaRelease = getSupportedArchetypeVersions(true, false);
    var javaPreview = getSupportedArchetypeVersions(true, true);
    var javaScriptRelease = getSupportedArchetypeVersions(false, false);
    var javaScriptPreview = getSupportedArchetypeVersions(false, true);
    assertFalse(javaRelease.isEmpty());
    assertFalse(javaScriptPreview.isEmpty());
    assertTrue(javaRelease.size() <= javaPreview.size());
    assertTrue(javaScriptRelease.size() <= javaScriptPreview.size());
    assertTrue(javaRelease.stream().anyMatch(v -> v.startsWith("24.")));
    assertTrue(javaPreview.stream().anyMatch(v -> v.startsWith("25.")));
    assertTrue(javaScriptPreview.stream().anyMatch(v -> v.startsWith("25.")));
  }

  @Test
  public void testLimitToLtsOrNewest() {
    // X.1 is latest stable -> X.2-beta need to be preserved as well
    assertEquals(List.of(ApiVersion.parse("25.2.0-beta.1").orElseThrow(),
        ApiVersion.parse("25.2.0-beta.0").orElseThrow(),  // X.2 are LTS and therefore always preserved
        ApiVersion.parse("25.1.15").orElseThrow(), // newest stable is always preserved (even if X.1)
        ApiVersion.parse("25.1.12").orElseThrow(), // newest stable is always preserved (even if X.1)
        ApiVersion.parse("24.2.16").orElseThrow(), // X.2 are LTS and therefore always preserved
        ApiVersion.parse("24.2.2").orElseThrow()  // X.2 are LTS and therefore always preserved
    ), limitToLtsOrNewest(List.of(ApiVersion.parse("25.2.0-beta.1").orElseThrow(),
        ApiVersion.parse("25.2.0-beta.0").orElseThrow(),
        ApiVersion.parse("25.1.15").orElseThrow(),
        ApiVersion.parse("25.1.12").orElseThrow(),
        ApiVersion.parse("24.2.16").orElseThrow(),
        ApiVersion.parse("24.2.2").orElseThrow(),
        ApiVersion.parse("24.1.4").orElseThrow(),
        ApiVersion.parse("24.1.0-beta.1").orElseThrow()
    )).toList());

    // X.2 is latest stable -> X+1.1-beta need to be preserved as well
    assertEquals(List.of(ApiVersion.parse("26.1.0-beta.1").orElseThrow(), // newest is always preserved (even if X.1)
        ApiVersion.parse("26.1.0-beta.0").orElseThrow(),  // newest is always preserved (even if X.1)
        ApiVersion.parse("25.2.15").orElseThrow(), // X.2 are LTS and therefore always preserved
        ApiVersion.parse("25.2.12").orElseThrow(), // X.2 are LTS and therefore always preserved
        ApiVersion.parse("25.2.0-beta.0").orElseThrow(), // X.2 are LTS and therefore always preserved
        ApiVersion.parse("24.2.16").orElseThrow(), // X.2 are LTS and therefore always preserved
        ApiVersion.parse("24.2.2").orElseThrow()  // X.2 are LTS and therefore always preserved
    ), limitToLtsOrNewest(List.of(ApiVersion.parse("26.1.0-beta.1").orElseThrow(),
        ApiVersion.parse("26.1.0-beta.0").orElseThrow(),
        ApiVersion.parse("25.2.15").orElseThrow(),
        ApiVersion.parse("25.2.12").orElseThrow(),
        ApiVersion.parse("25.2.0-beta.0").orElseThrow(),
        ApiVersion.parse("24.2.16").orElseThrow(),
        ApiVersion.parse("24.2.2").orElseThrow(),
        ApiVersion.parse("24.1.4").orElseThrow(),
        ApiVersion.parse("24.1.0-beta.1").orElseThrow()
    )).toList());
  }
}
