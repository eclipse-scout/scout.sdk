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
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <h1>Bug 87'486</h1>
 * <p>
 * <b>Symptom:</b> NPEs are thrown when expanding the forms node of a Scout client project.
 * <p>
 * <b>Reason:</b> The way icons are listed in the AbstractIcons.java (Scout framework) and Icons.java (project) has
 * changed. The fields are no more final, but only static.
 */
@Ignore
public class Bug87486Test extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("bugsBeforeOpensource/87486", "a.shared", "a.client");
  }

  @AfterClass
  public static void cleanUpWorkspace() throws Exception {
    deleteProjects("a.client", "a.shared");
  }

  @Test
  public void testGetIcon_AbstractIcons_Folder() throws Exception {
    checkResolveName("tree_node_closed", true);
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
    ScoutSdkCore.getScoutWorkspace().getRootProjects();

    IType icons = TypeUtility.getType("a.shared.Icons");
    Assert.assertTrue(TypeUtility.exists(icons));
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("a.shared");
    IScoutProject scoutProject = ScoutSdkCore.getScoutWorkspace().getScoutBundle(project).getScoutProject();
    IIconProvider projectIcons = scoutProject.getIconProvider();
    ScoutIconDesc iconDesc = projectIcons.getIcon("\"" + iconName + "\"");

    Assert.assertNotNull(iconDesc);
    Assert.assertEquals(iconName, iconDesc.getIconName());
    if (assertIconExists) {
      Assert.assertNotNull(iconDesc.getImageDescriptor());
    }
    else {
      Assert.assertNull(iconDesc.getImageDescriptor());
    }
  }
}
