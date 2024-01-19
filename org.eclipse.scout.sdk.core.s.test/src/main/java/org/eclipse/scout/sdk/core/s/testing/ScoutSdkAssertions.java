/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.testing;

import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.versionedResourcesFolders;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions;
import org.eclipse.scout.sdk.core.testing.SdkAssertions;

public final class ScoutSdkAssertions {

  private ScoutSdkAssertions() {
  }

  /**
   * Asserts that the source output of the given {@link ISourceGenerator} equals the content of the versioned given
   * file. Searches for the latest version of the file inside the given path which has a lower or equal version than the
   * api of the given {@link IJavaEnvironment}.
   *
   * @param env
   *          The {@link IJavaEnvironment} in which the specified {@link ISourceGenerator} should be executed and the
   *          api version is detected. Must not be {@code null}.
   * @param versionedPath
   *          The absolute path on where the versioned resource folders can be found.
   * @param fileName
   *          The name of the file inside the versioned resource folder.
   * @param generator
   *          The {@link ISourceGenerator} to use. Must not be {@code null}.
   */
  public static void assertEqualsVersionedRefFile(IJavaEnvironment env, String versionedPath, String fileName, ISourceGenerator<ISourceBuilder<?>> generator) {
    SdkJavaAssertions.assertEqualsRefFile(env, versionedResourcesFolders(versionedPath, env).findFirst().orElseThrow() + fileName, generator);
  }

  /**
   * Asserts that the given actual content equals the content of the given versioned file. Searches for the latest
   * version of the file inside the given path which has a lower or equal version than the api of the given
   * {@link IJavaEnvironment}.
   *
   * @param env
   *          The {@link IJavaEnvironment} in which the api version is detected. Must not be {@code null}.
   * @param versionedPath
   *          The absolute path on where the versioned resource folders can be found.
   * @param fileName
   *          The name of the file inside the versioned resource folder.
   * @param actualContent
   *          The actual content to compare against.
   */
  public static void assertEqualsVersionedRefFile(IJavaEnvironment env, String versionedPath, String fileName, CharSequence actualContent) {
    SdkAssertions.assertEqualsRefFile(versionedResourcesFolders(versionedPath, env).findFirst().orElseThrow() + fileName, actualContent);
  }
}
