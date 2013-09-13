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
package org.eclipse.scout.sdk.internal.test.jdt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.helper.ScoutProjectHelper;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.testing.TestWorkspaceUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.junit.Test;

/**
 *
 */
public class JavaProjectTests extends AbstractScoutSdkTest {

  /**
   * Resource project instances are reused. Creating a project1 'aProject' deleting it and create a second project with
   * the same name the references to the project1+2 are the same (equals).
   */
  @Test
  public void testResourceProjectEquality() throws Exception {
    String projectName = "aProject";
    IProject project1 = TestWorkspaceUtility.createProject(projectName);
    assertTrue(project1.exists());
    clearWorkspace();
    assertFalse(project1.exists());
    IProject project2 = TestWorkspaceUtility.createProject(projectName);
    assertTrue(project2.exists());
    assertTrue(project1.exists());
    assertTrue(project1.equals(project2));
    assertSame(project1, project2);
    clearWorkspace();
  }

  /**
   * Java project instances are reused. Creating a project1 'aProject' deleting it and create a second project with
   * the same name the references to the project1+2 are the same (equals).
   */
  @Test
  public void testJavaProjectEquality() throws Exception {
    try {
      IScoutBundle module = ScoutProjectHelper.setupNewProject("a", false, true, false);
      IJavaProject shared1 = ScoutUtility.getJavaProject(module.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), true));
      assertTrue(shared1.exists());
      clearWorkspace();
      assertFalse(shared1.exists());

      IScoutBundle module2 = ScoutProjectHelper.setupNewProject("a", false, true, false);
      IJavaProject shared2 = ScoutUtility.getJavaProject(module2.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), true));
      assertTrue(shared2.exists());
      assertTrue(shared1.exists());
      assertNotSame(shared1, shared2);
      assertTrue(shared1.equals(shared2));
      clearWorkspace();
    }
    finally {
      clearWorkspace();
    }
  }

}
