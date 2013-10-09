/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.testing.TestUtility;
import org.eclipse.scout.sdk.testing.TestWorkspaceUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractScoutSdkTest {

  @BeforeClass
  public static void setup() throws CoreException {
    TestUtility.setAutoUpdateDto(false);
    TestUtility.showEgitMessageBoxes(false);
    TestUtility.setAutoBuildWorkspace(false);
  }

  /**
   * deletes all workspace projects and waits for a silent workspace
   * 
   * @throws Exception
   */
  @AfterClass
  public static void clearWorkspace() throws Exception {
    TestWorkspaceUtility.clearWorkspace();
  }

  protected static void setupWorkspace(String baseFolder, String... projects) throws Exception {
    TestWorkspaceUtility.setupWorkspace(Platform.getBundle(Activator.PLUGIN_ID), baseFolder, projects);
    TestWorkspaceUtility.buildWorkspaceAndAssertNoCompileErrors();
  }

  /**
   * @return returns the project with the given name.
   */
  protected static IProject getProject(String projectName) {
    return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
  }

  protected static void executeBuildAssertNoCompileErrors(IOperation... ops) throws Exception {
    TestWorkspaceUtility.executeAndBuildWorkspace(ops);
    TestWorkspaceUtility.assertNoCompileErrors();
  }
}
