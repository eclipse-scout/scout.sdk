/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.project;

import static org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils.runMavenCleanTest;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link ScoutProjectNewTest}</h3>
 *
 * @since 5.2.0
 */
public class ScoutProjectNewTest {

  @Test
  public void testHelloWorldArchetype() throws IOException {
    Path targetDirectory = null;
    try {
      targetDirectory = CoreScoutTestingUtils.createClassicTestProject();
      runMavenCleanTest(targetDirectory.resolve(CoreScoutTestingUtils.PROJECT_ARTIFACT_ID));
    }
    finally {
      deleteDir(targetDirectory);
    }
  }

  @Test
  public void testHelloJsArchetype() throws IOException {
    Path targetDirectory = null;
    try {
      targetDirectory = CoreScoutTestingUtils.createJsTestProject();
      runMavenCleanTest(targetDirectory.resolve(CoreScoutTestingUtils.PROJECT_ARTIFACT_ID));
    }
    finally {
      deleteDir(targetDirectory);
    }
  }

  private static void deleteDir(Path p) throws IOException {
    if (p == null) {
      return;
    }

    CoreUtils.deleteDirectory(p);
  }
}
