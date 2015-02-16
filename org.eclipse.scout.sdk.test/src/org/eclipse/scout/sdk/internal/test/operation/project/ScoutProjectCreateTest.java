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

import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.helper.ScoutProjectHelper;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.operation.project.template.OutlineTemplateOperation;
import org.eclipse.scout.sdk.operation.project.template.SingleFormTemplateOperation;
import org.eclipse.scout.sdk.testing.TestWorkspaceUtility;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.scout.sdk.util.internal.typecache.JavaResourceChangedEmitter;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TODO work in progress
 */
public class ScoutProjectCreateTest extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setup() {
    ScoutSdkCore.getScoutWorkspace();
  }

  @Test
  public void testCreateBundles() throws Exception {
    try {
      ScoutSdkCore.getScoutWorkspace();
      IScoutBundle project = ScoutProjectHelper.setupNewProject("org.eclipse.testapp", true, true, true, true, false);
      TestWorkspaceUtility.assertNoCompileErrors();
      Assert.assertNotNull(project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false));
      Assert.assertNotNull(project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), true));
      Assert.assertNotNull(project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER), false));
      Assert.assertNotNull(project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_UI_SWT), false));
      Assert.assertNull(project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_UI_SWING), false));
    }
    finally {
      clearWorkspace();
      IScoutBundleGraph bundleGraph = ScoutSdkCore.getScoutWorkspace().getBundleGraph();
      bundleGraph.waitFor();
      Assert.assertEquals(0, bundleGraph.getBundles(ScoutBundleFilters.getRootBundlesFilter()).size());
    }
  }

  @Test
  public void testLoop() throws Exception {
    // create 5 times a project with the same name and remove it again
    for (int i = 0; i < 5; i++) {
      System.out.println("-------------- start " + i + "--------------");
      testTemplateDesktopForm("org.eclipse.testapp");
      for (ICompilationUnit icu : JavaResourceChangedEmitter.getPendingWorkingCopies()) {
        System.out.println("still available resource: " + icu.getElementName());
      }
      System.out.println("-------------- end " + i + "--------------");
    }
  }

  private void testTemplateDesktopForm(String projectName) throws Exception {
    try {
      PropertyMap properties = new PropertyMap();
      ScoutProjectHelper.setupNewProject(projectName, true, true, true, properties);

      final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);
      ITypeHierarchy hierarchy = TypeUtility.getTypeHierarchy(iForm);
      for (IType tx : hierarchy.getAllSubtypes(iForm)) {
        if (tx.getElementName().equals("DesktopForm")) {
          System.out.println("EEKEKEKKEKEKEKKEKEKKEKEKEKKEKEKKEKE ");
        }
      }
      final ICachedTypeHierarchy formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
      Set<IType> subtypes = formHierarchy.getAllSubtypes(iForm, TypeFilters.getInWorkspaceFilter());
      if (subtypes.size() > 0) {
        System.out.println("Should not come up:");
        for (IType t : subtypes) {
          System.out.println(" - " + t.getFullyQualifiedName() + " exists " + TypeUtility.exists(t) + "  " + t.getResource().exists());
        }
      }
      Assert.assertEquals(0, subtypes.size());
      SingleFormTemplateOperation op = new SingleFormTemplateOperation();
      op.setProperties(properties);
      op.init();
      executeBuildAssertNoCompileErrors(op);

      JdtUtility.waitForIndexesReady();
      System.out.println("iForm exists " + iForm.exists() + "  " + iForm.getJavaProject().exists());
      subtypes = formHierarchy.getAllSubtypes(iForm, TypeFilters.getInWorkspaceFilter());
      if (subtypes.size() != 1) {
        System.out.println("NOT FIRED RESOURCES -------");
        for (ICompilationUnit icu : JavaResourceChangedEmitter.getPendingWorkingCopies()) {
          System.out.println(" - '" + icu.getElementName() + "'");
        }
        System.out.println("EEEEEEEEEEEEEEEEEEEEEERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
        Thread.sleep(10000);
      }
      Assert.assertEquals(1, subtypes.size());
    }
    finally {
      clearWorkspace();
      Assert.assertEquals(0, ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getRootBundlesFilter()).size());
    }

  }

  @Test
  public void testTemplateOutlineTreeTable() throws Exception {
    try {
      PropertyMap properties = new PropertyMap();
      ScoutProjectHelper.setupNewProject("org.eclipse.testapp1", true, true, true, properties);

      final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);
      final ICachedTypeHierarchy formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
      Set<IType> subtypes = formHierarchy.getAllSubtypes(iForm, TypeFilters.getInWorkspaceFilter());
      Assert.assertEquals(0, subtypes.size());

      OutlineTemplateOperation op = new OutlineTemplateOperation();
      op.setProperties(properties);
      op.init();
      executeBuildAssertNoCompileErrors(op);
    }
    finally {
      clearWorkspace();
      Assert.assertEquals(0, ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getRootBundlesFilter()).size());
    }
  }
}
