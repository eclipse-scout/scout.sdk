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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.helper.ScoutProjectHelper;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.operation.outline.OutlineNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.testing.TestWorkspaceUtility;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.junit.Test;

/**
 *
 */
public class TypeHierarchyTest2 extends AbstractScoutSdkTest {

  @Test
  public void testRemoveType() throws Exception {
    try {
      IScoutBundle project = ScoutProjectHelper.setupNewProject("abc.testapp", true, true, true);
      final IType iOutline = TypeUtility.getType(RuntimeClasses.IOutline);
      final IPrimaryTypeTypeHierarchy outlineHierarchy = TypeUtility.getPrimaryTypeHierarchy(iOutline);
      IType[] subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(0, subtypes.length);
      IScoutBundle bundle = project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false);
      SdkAssert.assertNotNull(bundle);
      OutlineNewOperation createOutlineOp = new OutlineNewOperation("Test1Outline", DefaultTargetPackage.get(bundle, IDefaultTargetPackage.CLIENT_OUTLINES), bundle.getJavaProject());
      executeBuildAssertNoCompileErrors(createOutlineOp);
      IType outline = createOutlineOp.getCreatedType();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(1, subtypes.length);
      outline.delete(true, new NullProgressMonitor());
      TestWorkspaceUtility.buildWorkspace();
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
      IScoutBundle clientBundle = project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false);
      SdkAssert.assertNotNull(clientBundle);
      OutlineNewOperation createOutlineOp = new OutlineNewOperation("Test1Outline", DefaultTargetPackage.get(clientBundle, IDefaultTargetPackage.CLIENT_OUTLINES), clientBundle.getJavaProject());
      executeBuildAssertNoCompileErrors(createOutlineOp);
      IType outline = createOutlineOp.getCreatedType();
      TestWorkspaceUtility.buildWorkspace();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(1, subtypes.length);
      outline.getCompilationUnit().getResource().delete(true, new NullProgressMonitor());
      TestWorkspaceUtility.buildWorkspace();
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
      IScoutBundle clientBundle = project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false);
      SdkAssert.assertNotNull(clientBundle);
      OutlineNewOperation createOutlineOp = new OutlineNewOperation("Test1Outline", DefaultTargetPackage.get(clientBundle, IDefaultTargetPackage.CLIENT_OUTLINES), clientBundle.getJavaProject());
      executeBuildAssertNoCompileErrors(createOutlineOp);

      IType outline = createOutlineOp.getCreatedType();
      TestWorkspaceUtility.buildWorkspace();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(1, subtypes.length);
      outline.getPackageFragment().delete(true, new NullProgressMonitor());
      TestWorkspaceUtility.buildWorkspace();
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
      IScoutBundle clientBundle = project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false);
      SdkAssert.assertNotNull(clientBundle);
      OutlineNewOperation createOutlineOp = new OutlineNewOperation("Test1Outline", DefaultTargetPackage.get(clientBundle, IDefaultTargetPackage.CLIENT_OUTLINES), clientBundle.getJavaProject());
      executeBuildAssertNoCompileErrors(createOutlineOp);

      IType outline = createOutlineOp.getCreatedType();
      TestWorkspaceUtility.buildWorkspace();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(1, subtypes.length);
      outline.getPackageFragment().getResource().delete(true, new NullProgressMonitor());
      TestWorkspaceUtility.buildWorkspace();
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
      IScoutBundle clientBundle = project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false);
      SdkAssert.assertNotNull(clientBundle);
      OutlineNewOperation createOutlineOp = new OutlineNewOperation("Test1Outline", DefaultTargetPackage.get(clientBundle, IDefaultTargetPackage.CLIENT_OUTLINES), clientBundle.getJavaProject());
      executeBuildAssertNoCompileErrors(createOutlineOp);

      IType outline = createOutlineOp.getCreatedType();
      TestWorkspaceUtility.buildWorkspace();
      subtypes = outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter());
      assertEquals(1, subtypes.length);
      outline.getJavaProject().getProject().delete(true, new NullProgressMonitor());
      TestWorkspaceUtility.buildWorkspace();
      iOutline = TypeUtility.getType(RuntimeClasses.IOutline);
      assertFalse(TypeUtility.exists(outline));
      assertFalse(TypeUtility.exists(iOutline));
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
      IScoutBundle clientBundle = project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false);
      SdkAssert.assertNotNull(clientBundle);
      OutlineNewOperation createOutlineOp = new OutlineNewOperation("Test1Outline", DefaultTargetPackage.get(clientBundle, IDefaultTargetPackage.CLIENT_OUTLINES), clientBundle.getJavaProject());
      executeBuildAssertNoCompileErrors(createOutlineOp);
      createOutlineOp.getCreatedType();
      assertEquals(1, outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter()).length);
      clearWorkspace();
      ResourcesPlugin.getWorkspace().checkpoint(false);
      JdtUtility.waitForIndexesReady();
      ScoutSdkCore.getScoutWorkspace().getBundleGraph().waitFor();
      assertFalse(TypeUtility.exists(iOutline));

      // second project
      IScoutBundle project2 = ScoutProjectHelper.setupNewProject("zyx.test", true, true, true);
      iOutline = SdkAssert.assertTypeExists(RuntimeClasses.IOutline);
      outlineHierarchy = TypeUtility.getPrimaryTypeHierarchy(iOutline);
      assertEquals(0, outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter()).length);
      // create outline
      IScoutBundle clientBundle2 = project2.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false);
      SdkAssert.assertNotNull(clientBundle2);
      OutlineNewOperation createOutlineOp2 = new OutlineNewOperation("Test2Outline", DefaultTargetPackage.get(clientBundle2, IDefaultTargetPackage.CLIENT_OUTLINES), clientBundle2.getJavaProject());
      executeBuildAssertNoCompileErrors(createOutlineOp2);
      TestWorkspaceUtility.buildWorkspace();
      assertEquals(1, outlineHierarchy.getAllSubtypes(iOutline, TypeFilters.getInWorkspaceFilter()).length);
    }
    finally {
      clearWorkspace();
    }
  }
}
