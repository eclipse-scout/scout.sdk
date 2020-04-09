/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.project;

import java.io.File;
import java.io.IOException;

import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.s.testing.IntegrationTest;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * <h3>{@link ScoutProjectNewTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
@Category(IntegrationTest.class)
public class ScoutProjectNewTest {

  @Test
  public void testProjectCreation() throws IOException {
    File targetDirectory = null;
    try {
      targetDirectory = CoreScoutTestingUtils.createTestProject();
      CoreScoutTestingUtils.runMavenCleanTest(new File(targetDirectory, CoreScoutTestingUtils.PROJECT_ARTIFACT_ID));
    }
    finally {
      if (targetDirectory != null) {
        CoreUtils.deleteDirectory(targetDirectory);
      }
    }
  }
}
