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
package org.eclipse.scout.sdk.internal.test.operation.project;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.project.template.OutlineTemplateOperation;
import org.eclipse.scout.sdk.operation.project.template.SingleFormTemplateOperation;
import org.eclipse.scout.sdk.util.ScoutSeverityManager;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class ScoutProjectCreateTest extends AbstractCreateNewProjectTest {

  @BeforeClass
  public static void setup() {
    setAutoUpdateFormData(false);
    ScoutSdkCore.getScoutWorkspace();
  }

  @Test
  public void testCreateBundles() throws Exception {
    ScoutSdkCore.getScoutWorkspace();
    IScoutProject project = setupNewProject("org.eclipse.testapp", "", "testapp");
    int severity = ScoutSeverityManager.getInstance().getSeverityOf(ResourcesPlugin.getWorkspace().getRoot());
    Assert.assertTrue(severity < IMarker.SEVERITY_ERROR);
    Assert.assertNotNull(project.getClientBundle());
    Assert.assertNotNull(project.getSharedBundle());
    Assert.assertNotNull(project.getServerBundle());
    Assert.assertNotNull(project.getUiSwtBundle());
    Assert.assertNull(project.getUiSwingBundle());
    clearWorkspace();
    Assert.assertEquals(0, ScoutSdkCore.getScoutWorkspace().getRootProjects().length);
  }

  @Test
  public void testLoop() throws Exception {
    for (int i = 0; i < 3; i++) {
      testTemplateDesktopForm("org.eclipse.testapp");
    }
  }

  private void testTemplateDesktopForm(String projectName) throws Exception {
    IScoutProject project = setupNewProject(projectName, "", "testapp");
    final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);
    final IPrimaryTypeTypeHierarchy formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
    IType[] subtypes = formHierarchy.getAllSubtypes(iForm, TypeFilters.getInWorkspaceFilter());
    Assert.assertEquals(0, subtypes.length);
    SingleFormTemplateOperation op = new SingleFormTemplateOperation(project);
    OperationJob job = new OperationJob(op);
    job.schedule();
    job.join();
    buildWorkspace();
    int severity = ScoutSeverityManager.getInstance().getSeverityOf(ResourcesPlugin.getWorkspace().getRoot());
    Assert.assertTrue(severity < IMarker.SEVERITY_ERROR);
    subtypes = formHierarchy.getAllSubtypes(iForm, TypeFilters.getInWorkspaceFilter());
    Assert.assertEquals(1, subtypes.length);
    clearWorkspace();
    Assert.assertEquals(0, ScoutSdkCore.getScoutWorkspace().getRootProjects().length);

  }

  @Test
  public void testTemplateOutlineTreeTable() throws Exception {
    IScoutProject project = setupNewProject("org.eclipse.testapp1", "", "testapp1");
    final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);
    final IPrimaryTypeTypeHierarchy formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
    IType[] subtypes = formHierarchy.getAllSubtypes(iForm, TypeFilters.getInWorkspaceFilter());
    Assert.assertEquals(0, subtypes.length);
    OutlineTemplateOperation op = new OutlineTemplateOperation(project);
    OperationJob job = new OperationJob(op);
    job.schedule();
    job.join();
    buildWorkspace();
    int severity = ScoutSeverityManager.getInstance().getSeverityOf(ResourcesPlugin.getWorkspace().getRoot());
    Assert.assertTrue(severity < IMarker.SEVERITY_ERROR);
    clearWorkspace();
    Assert.assertEquals(0, ScoutSdkCore.getScoutWorkspace().getRootProjects().length);
  }
}
