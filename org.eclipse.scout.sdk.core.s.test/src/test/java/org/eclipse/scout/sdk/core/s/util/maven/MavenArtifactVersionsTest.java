/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.util.maven;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Paths;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.internal.JavaEnvironmentImplementor;
import org.eclipse.scout.sdk.core.model.ecj.FileSystemWithOverride;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.RunningJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcj;
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class)
public class MavenArtifactVersionsTest {

  @Test
  public void testGetFromJar(IJavaEnvironment env) {
    assertFalse(MavenArtifactVersions.usedIn(null, null).isPresent());
    assertNotNull(MavenArtifactVersions.usedIn(ScoutApi.SCOUT_RT_PLATFORM_NAME, env).orElseThrow());
  }

  @Test
  @Tag("IntegrationTest")
  public void testAllOnCentral() throws IOException {
    assertTrue(MavenArtifactVersions.allOnCentral(ScoutProjectNewHelper.SCOUT_ARCHETYPES_GROUP_ID, ScoutProjectNewHelper.SCOUT_ARCHETYPES_HELLOWORLD_ARTIFACT_ID).findAny().isPresent());
    assertFalse(MavenArtifactVersions.allOnCentral(ScoutProjectNewHelper.SCOUT_ARCHETYPES_GROUP_ID, "not-existing").findAny().isPresent());
  }

  @Test
  public void testGetFromSourceFolder() {
    var modulePath = Paths.get("").toAbsolutePath();
    var version = new RunningJavaEnvironmentFactory().get()
        .excludeIfContains("scout")
        .withSourceFolder(modulePath.resolve(ISourceFolders.MAIN_JAVA_SOURCE_FOLDER).toString())
        .withSourcesIncluded(false)
        .call(e -> MavenArtifactVersions.usedIn(modulePath.getFileName().toString(), e));
    assertNotNull(version.orElseThrow());
  }

  @Test
  public void testNameEnvironmentAndCompilerNotCreated() {
    var spy = Mockito.spy(new JavaEnvForSpy());

    // use try to ensure the environment is closed. This is necessary to also test that the close does not call getNameEnvironment()
    try (spy) {
      IJavaEnvironment env = new JavaEnvironmentImplementor(spy);
      assertFalse(MavenArtifactVersions.usedIn(ScoutApi.SCOUT_RT_PLATFORM_NAME, env).isPresent());
    }
    verify(spy, never()).getNameEnvironment();
  }

  protected static class JavaEnvForSpy extends JavaEnvironmentWithEcj {

    protected JavaEnvForSpy() {
      super(null /* running jre */, null /* only JRE */, null /* use default */);
    }

    @Override
    public FileSystemWithOverride getNameEnvironment() {
      // make method public
      return super.getNameEnvironment();
    }
  }
}
