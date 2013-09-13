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
package org.eclipse.scout.sdk.internal.test.operation.jdt;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.PackageFragementNewOperation;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.testing.TestWorkspaceUtility;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h3>{@link PackageFragmentNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 08.02.2013
 */
public class PackageFragmentNewOperationTest extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/operation/method", "test.client", "test.shared");
  }

  @Test
  public void createPackageTest() throws Exception {
    IProject clientProject = getProject("test.client");
    Assert.assertNotNull(clientProject);
    IJavaProject javaProject = JavaCore.create(clientProject);
    String packageName = "abc.def";
    PackageFragementNewOperation packageOp = new PackageFragementNewOperation(packageName, javaProject);
    packageOp.setExportPackagePolicy(ExportPolicy.AddPackageWhenNotEmpty);

    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, packageOp);

    IPackageFragment createdPackageFragment = packageOp.getCreatedPackageFragment();
    Assert.assertTrue(TypeUtility.exists(createdPackageFragment));
    Assert.assertTrue(TypeUtility.exists(TypeUtility.getPackage(javaProject, packageName)));
    // check export package
    PluginModelHelper h = new PluginModelHelper(clientProject);
    Assert.assertFalse(h.Manifest.existsExportPackage(createdPackageFragment));
  }

  @Test
  public void tryToCreateAnExistingPackage() throws Exception {
    IProject clientProject = getProject("test.client");
    Assert.assertNotNull(clientProject);
    IJavaProject javaProject = JavaCore.create(clientProject);
    String packageName = "test.client";
    Assert.assertTrue(TypeUtility.exists(TypeUtility.getPackage(javaProject, packageName)));

    PackageFragementNewOperation packageOp = new PackageFragementNewOperation(packageName, javaProject);
    packageOp.setExportPackagePolicy(ExportPolicy.AddPackageWhenNotEmpty);
    packageOp.setNoErrorWhenPackageAlreadyExist(true);
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, packageOp);

    SdkAssert.assertExist(packageOp.getCreatedPackageFragment());
  }

  @Test
  public void createPackageWithIcu() throws Exception {
    IProject clientProject = getProject("test.client");
    Assert.assertNotNull(clientProject);
    IJavaProject javaProject = JavaCore.create(clientProject);
    String packageName = "abc.def01";
    PackageFragementNewOperation packageOp = new PackageFragementNewOperation(packageName, javaProject);
    packageOp.setExportPackagePolicy(ExportPolicy.AddPackage);

    TestWorkspaceUtility.executeAndBuildWorkspace(SYSTEM_PROPERTIES_FORM_DATA_USER, packageOp);

    IPackageFragment createdPackageFragment = packageOp.getCreatedPackageFragment();
    Assert.assertTrue(TypeUtility.exists(createdPackageFragment));
    Assert.assertTrue(TypeUtility.exists(TypeUtility.getPackage(javaProject, packageName)));

    // check export package
    PluginModelHelper h = new PluginModelHelper(clientProject);
    Assert.assertTrue(h.Manifest.existsExportPackage(createdPackageFragment));

    PrimaryTypeNewOperation typeOp = new PrimaryTypeNewOperation("AbcType", createdPackageFragment);
    typeOp.setFlags(Flags.AccPublic);

    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, typeOp);

  }

}
