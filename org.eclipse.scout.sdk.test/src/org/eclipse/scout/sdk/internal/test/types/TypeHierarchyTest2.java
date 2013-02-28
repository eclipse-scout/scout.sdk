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
package org.eclipse.scout.sdk.internal.test.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.helper.ScoutProjectHelper;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.outline.OutlineNewOperation;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class TypeHierarchyTest2 extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setup() {
    setAutoUpdateFormData(false);
    ScoutSdkCore.getScoutWorkspace();
  }

  @Test
  public void testRemoveType() throws Exception {
    try {
      IScoutBundle project = ScoutProjectHelper.setupNewProject("abc.testapp", true, true, true);
      final IType iOutline = TypeUtility.getType(RuntimeClasses.IOutline);
      final IPrimaryTypeTypeHierarchy outlineHierarchy = TypeUtility.getPrimaryTypeHierarchy(iOutline);
      IType[] subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(0, subtypes.length);
      OutlineNewOperation createOutlineOp = new OutlineNewOperation();
      createOutlineOp.setClientBundle(project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false));
      createOutlineOp.setTypeName("Test1Outline");
      OperationJob job = new OperationJob(createOutlineOp);
      job.schedule();
      job.join();
      IType outline = createOutlineOp.getCreatedOutline();
      buildWorkspace();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(1, subtypes.length);
      outline.delete(true, new NullProgressMonitor());
      buildWorkspace();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(0, subtypes.length);
    }
    finally {
      clearWorkspace();
    }
    assertEquals(0, ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getRootBundlesFilter()).length);
  }

  @Test
  public void testRemoveFile() throws Exception {
    try {
      IScoutBundle project = ScoutProjectHelper.setupNewProject("abc.testapp", true, true, true);
      final IType iOutline = TypeUtility.getType(RuntimeClasses.IOutline);
      final IPrimaryTypeTypeHierarchy outlineHierarchy = TypeUtility.getPrimaryTypeHierarchy(iOutline);
      IType[] subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(0, subtypes.length);
      OutlineNewOperation createOutlineOp = new OutlineNewOperation();
      createOutlineOp.setClientBundle(project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false));
      createOutlineOp.setTypeName("Test1Outline");
      OperationJob job = new OperationJob(createOutlineOp);
      job.schedule();
      job.join();
      IType outline = createOutlineOp.getCreatedOutline();
      buildWorkspace();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(1, subtypes.length);
      outline.getCompilationUnit().getResource().delete(true, new NullProgressMonitor());
      buildWorkspace();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(0, subtypes.length);
    }
    finally {
      clearWorkspace();
    }
    assertEquals(0, ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getRootBundlesFilter()).length);
  }

  @Test
  public void testRemovePackage() throws Exception {
    try {
      IScoutBundle project = ScoutProjectHelper.setupNewProject("abc.testapp", true, true, true);
      final IType iOutline = TypeUtility.getType(RuntimeClasses.IOutline);
      final IPrimaryTypeTypeHierarchy outlineHierarchy = TypeUtility.getPrimaryTypeHierarchy(iOutline);
      IType[] subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(0, subtypes.length);
      OutlineNewOperation createOutlineOp = new OutlineNewOperation();
      createOutlineOp.setClientBundle(project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false));
      createOutlineOp.setTypeName("Test1Outline");
      OperationJob job = new OperationJob(createOutlineOp);
      job.schedule();
      job.join();
      IType outline = createOutlineOp.getCreatedOutline();
      buildWorkspace();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(1, subtypes.length);
      outline.getPackageFragment().delete(true, new NullProgressMonitor());
      buildWorkspace();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(0, subtypes.length);
    }
    finally {
      clearWorkspace();
    }
    assertEquals(0, ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getRootBundlesFilter()).length);
  }

  @Test
  public void testRemovePackageFolder() throws Exception {
    try {
      IScoutBundle project = ScoutProjectHelper.setupNewProject("abc.testapp", true, true, true);
      final IType iOutline = TypeUtility.getType(RuntimeClasses.IOutline);
      final IPrimaryTypeTypeHierarchy outlineHierarchy = TypeUtility.getPrimaryTypeHierarchy(iOutline);
      IType[] subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(0, subtypes.length);
      OutlineNewOperation createOutlineOp = new OutlineNewOperation();
      createOutlineOp.setClientBundle(project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false));
      createOutlineOp.setTypeName("Test1Outline");
      OperationJob job = new OperationJob(createOutlineOp);
      job.schedule();
      job.join();
      IType outline = createOutlineOp.getCreatedOutline();
      buildWorkspace();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(1, subtypes.length);
      outline.getPackageFragment().getResource().delete(true, new NullProgressMonitor());
      buildWorkspace();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(0, subtypes.length);
    }
    finally {
      clearWorkspace();
    }
    assertEquals(0, ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getRootBundlesFilter()).length);
  }

  @Test
  public void testRemoveProject() throws Exception {
    try {
      IScoutBundle project = ScoutProjectHelper.setupNewProject("abc.testapp", true, true, true);
      IType iOutline = TypeUtility.getType(RuntimeClasses.IOutline);
      IPrimaryTypeTypeHierarchy outlineHierarchy = TypeUtility.getPrimaryTypeHierarchy(iOutline);
      IType[] subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(0, subtypes.length);
      OutlineNewOperation createOutlineOp = new OutlineNewOperation();
      createOutlineOp.setClientBundle(project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false));
      createOutlineOp.setTypeName("Test1Outline");
      OperationJob job = new OperationJob(createOutlineOp);
      job.schedule();
      job.join();
      IType outline = createOutlineOp.getCreatedOutline();
      buildWorkspace();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(1, subtypes.length);
      outline.getJavaProject().getProject().delete(true, new NullProgressMonitor());
      buildWorkspace();
      iOutline = TypeUtility.getType(RuntimeClasses.IOutline);
      assertFalse(TypeUtility.exists(outline));
      assertFalse(TypeUtility.exists(iOutline));
      assertNull(project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false));
    }
    finally {
      clearWorkspace();
    }
    assertEquals(0, ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getRootBundlesFilter()).length);
  }

  @Test
  public void testHierarchyExistenceOverTwoProjects() throws Exception {
    try {
      IScoutBundle project = ScoutProjectHelper.setupNewProject("abc.test", true, true, true);
      IType iOutline = TypeUtility.getType(RuntimeClasses.IOutline);
      IPrimaryTypeTypeHierarchy outlineHierarchy = TypeUtility.getPrimaryTypeHierarchy(iOutline);
      assertEquals(0, outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter()).length);
      // create outline
      OutlineNewOperation createOutlineOp = new OutlineNewOperation();
      createOutlineOp.setClientBundle(project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false));
      createOutlineOp.setTypeName("Test1Outline");
      OperationJob job = new OperationJob(createOutlineOp);
      job.schedule();
      job.join();
      createOutlineOp.getCreatedOutline();
      buildWorkspace();
      assertEquals(1, outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter()).length);
      clearWorkspace();
      assertFalse(TypeUtility.exists(iOutline));
      // secound project
      IScoutBundle project2 = ScoutProjectHelper.setupNewProject("zyx.test", true, true, true);
      assertFalse(TypeUtility.exists(iOutline));
      iOutline = TypeUtility.getType(RuntimeClasses.IOutline);
      assertTrue(TypeUtility.exists(iOutline));
      outlineHierarchy = TypeUtility.getPrimaryTypeHierarchy(iOutline);
      assertEquals(0, outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter()).length);
      // create outline
      OutlineNewOperation createOutlineOp2 = new OutlineNewOperation();
      createOutlineOp2.setClientBundle(project2.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false));
      createOutlineOp2.setTypeName("Test2Outline");
      OperationJob createOutlineJob2 = new OperationJob(createOutlineOp2);
      createOutlineJob2.schedule();
      createOutlineJob2.join();
      createOutlineOp2.getCreatedOutline();
      buildWorkspace();
      assertEquals(1, outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter()).length);
    }
    finally {
      clearWorkspace();
    }
  }

}
