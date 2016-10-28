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
package org.eclipse.scout.sdk.internal.test.bug.beforeopensource;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h1>Bug 87'486</h1>
 * <p>
 * <b>Symptom:</b> NPEs are thrown when expanding the forms node of a Scout client project.
 * <p>
 * <b>Reason:</b> The way icons are listed in the AbstractIcons.java (Scout framework) and Icons.java (project) has
 * changed. The fields are no more final, but only static.
 */

public class Bug87486Test extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/bugsBeforeOpensource/87486", "a.shared", "a.client");
  }

  @Test
  public void testGetIcon_WellFormedIcon() throws Exception {
    checkResolveName("wellFormedIcon", true);
  }

  @Test
  public void testGetIcon_LegacyIcon() throws Exception {
    checkResolveName("legacyIcon", true);
  }

  @Test
  public void testGetIcon_MissingIcon() throws Exception {
    checkResolveName("missingIcon", false);
  }

  @Test
  public void testGetIcon_SpacesIcon() throws Exception {
    checkResolveName("spacesIcon", true);
  }

  @Test
  public void testGetIcon_BlockCommentIcon() throws Exception {
    checkResolveName("blockCommentIcon", true);
  }

  @Test
  public void testGetIcon_LineCommentIcon() throws Exception {
    checkResolveName("lineCommentIcon", true);
  }

  private void checkResolveName(String iconName, boolean assertIconExists) {
    ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getRootBundlesFilter());

    SdkAssert.assertTypeExists("a.shared.Icons");
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("a.shared");
    IScoutBundle scoutProject = ScoutTypeUtility.getScoutBundle(project);
    IIconProvider projectIcons = scoutProject.getIconProvider();
    ScoutIconDesc iconDesc = projectIcons.getIcon(iconName);

    Assert.assertNotNull("icon '" + iconName + "' dos not exist!", iconDesc);
    Assert.assertEquals(iconName, iconDesc.getIconName());
    if (assertIconExists) {
      Assert.assertNotNull(iconDesc.getImageDescriptor());
    }
    else {
      Assert.assertNull(iconDesc.getImageDescriptor());
    }
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    clearWorkspace();
  }
}