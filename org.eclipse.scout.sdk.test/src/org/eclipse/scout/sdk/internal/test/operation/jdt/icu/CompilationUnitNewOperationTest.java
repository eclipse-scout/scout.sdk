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
package org.eclipse.scout.sdk.internal.test.operation.jdt.icu;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitNewOperation;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.testing.TestWorkspaceUtility;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h3>{@link CompilationUnitNewOperationTest}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 08.02.2013
 */
public class CompilationUnitNewOperationTest extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/operation/jdt", "jdt.test.client", "jdt.test.shared", "jdt.test.server");
  }

  @Test
  public void testCreateIcuInExistingPackage() throws Exception {
    IJavaProject clientProject = JavaCore.create(getProject("jdt.test.client"));
    Assert.assertTrue(TypeUtility.exists(clientProject));

    CompilationUnitNewOperation icuNewOp = new CompilationUnitNewOperation("SimpleIcu.java", "jdt.test.client.icu.output", clientProject);
    executeBuildAssertNoCompileErrors(icuNewOp);

    Assert.assertTrue(TypeUtility.exists(icuNewOp.getCreatedCompilationUnit()));
    Assert.assertEquals(0, icuNewOp.getCreatedCompilationUnit().getTypes().length);
  }

  @Test
  public void testCreateIcuInNewPackage() throws Exception {
    IJavaProject clientProject = JavaCore.create(getProject("jdt.test.client"));
    Assert.assertTrue(TypeUtility.exists(clientProject));

    CompilationUnitNewOperation icuNewOp = new CompilationUnitNewOperation("SimpleIcu.java", "jdt.test.client.icu.output01", clientProject);
    TestWorkspaceUtility.executeAndBuildWorkspace(icuNewOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertTrue(TypeUtility.exists(icuNewOp.getCreatedCompilationUnit()));
    Assert.assertEquals(0, icuNewOp.getCreatedCompilationUnit().getTypes().length);
  }

  @Test
  public void testCompilationUnitInExportedPackage() throws Exception {
    IJavaProject clientProject = JavaCore.create(getProject("jdt.test.client"));
    Assert.assertTrue(TypeUtility.exists(clientProject));

    CompilationUnitNewOperation icuNewOp = new CompilationUnitNewOperation("SimpleIcu.java", "jdt.test.client.icu.output02", clientProject);
    icuNewOp.addTypeSourceBuilder(new TypeSourceBuilder("SimpleIcu"));
    icuNewOp.setPackageExportPolicy(ExportPolicy.AddPackage);
    TestWorkspaceUtility.executeAndBuildWorkspace(icuNewOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertTrue(TypeUtility.exists(icuNewOp.getCreatedCompilationUnit()));
    Assert.assertEquals(1, icuNewOp.getCreatedCompilationUnit().getTypes().length);
    PluginModelHelper h = new PluginModelHelper(clientProject.getProject());
    Assert.assertTrue(h.Manifest.existsExportPackage("jdt.test.client.icu.output02"));
  }

  @Test
  public void createCompilationUnitWithTypeTest() throws Exception {
    IJavaProject clientProject = JavaCore.create(getProject("jdt.test.client"));
    Assert.assertTrue(TypeUtility.exists(clientProject));
    CompilationUnitNewOperation icuNewOp = new CompilationUnitNewOperation("CompilationUnitTestClass.java", TypeUtility.getPackage(clientProject, "jdt.test.client.icu.output"));
    icuNewOp.addTypeSourceBuilder(new TypeSourceBuilder("CompilationUnitTestClass"));

    TestWorkspaceUtility.executeAndBuildWorkspace(icuNewOp);
    TestWorkspaceUtility.assertNoCompileErrors();

    Assert.assertTrue(TypeUtility.exists(TypeUtility.getType("jdt.test.client.icu.output.CompilationUnitTestClass")));
    Assert.assertEquals(1, icuNewOp.getCreatedCompilationUnit().getTypes().length);
  }
}
