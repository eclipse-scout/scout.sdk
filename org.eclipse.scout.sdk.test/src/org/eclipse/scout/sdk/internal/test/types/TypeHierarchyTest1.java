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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.commons.holders.IntegerHolder;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.operation.form.FormNewOperation;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h1>TypeHierarchyTest</h1>
 * <p>
 */
public class TypeHierarchyTest1 extends AbstractScoutSdkTest {

  private static String BUNDLE_NAME_CLIENT = "test.client";
  private static String BUNDLE_NAME_SHARED = "test.shared";
  private static String BUNDLE_NAME_SERVER = "test.server";

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/util/typeCache", BUNDLE_NAME_CLIENT, BUNDLE_NAME_SHARED, BUNDLE_NAME_SERVER);
  }

  @Test
  public void testPrimaryTypeHierarchy() {
    IType companyForm = SdkAssert.assertTypeExists("test.client.ui.forms.CompanyForm");
    IType iformField = TypeUtility.getType(RuntimeClasses.IFormField);
    IPrimaryTypeTypeHierarchy primaryFormFieldHierarchy = TypeUtility.getPrimaryTypeHierarchy(iformField);
    ITypeHierarchy companyFormHierarchy = primaryFormFieldHierarchy.combinedTypeHierarchy(companyForm);
    Assert.assertTrue(primaryFormFieldHierarchy.isCreated());

    IType mainBox = SdkAssert.assertTypeExists(companyForm, "MainBox");
    IType[] formFields = TypeUtility.getInnerTypes(mainBox, TypeFilters.getSubtypeFilter(iformField, companyFormHierarchy), ScoutTypeComparators.getOrderAnnotationComparator());
    Assert.assertTrue(formFields.length == 3);
    Assert.assertEquals(formFields[0].getElementName(), "NameField");
    Assert.assertEquals(formFields[1].getElementName(), "SinceField");
    Assert.assertEquals(formFields[2].getElementName(), "DetailsGroup");
  }

  @Test
  public void testFormHierarchy() throws Exception {
    final IJavaProject project = JavaCore.create(getProject(BUNDLE_NAME_CLIENT));
    final IScoutBundle sb = ScoutTypeUtility.getScoutBundle(project);
    final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);
    final IPrimaryTypeTypeHierarchy formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
    IType[] subtypes = formHierarchy.getAllSubtypes(iForm, ScoutTypeFilters.getTypesInScoutBundles(sb));
    Assert.assertEquals(1, subtypes.length);
    final IntegerHolder formCountHolder = new IntegerHolder(-1);
    formHierarchy.addHierarchyListener(new ITypeHierarchyChangedListener() {
      @Override
      public void hierarchyInvalidated() {
        formCountHolder.setValue(formHierarchy.getAllSubtypes(iForm, ScoutTypeFilters.getTypesInScoutBundles(sb)).length);
        synchronized (formCountHolder) {
          formCountHolder.notifyAll();
        }
      }
    });
    IScoutBundle client = ScoutTypeUtility.getScoutBundle(project.getProject());
    SdkAssert.assertNotNull(client);
    FormNewOperation formOp = new FormNewOperation("ANewForm", client.getPackageName(".ui.forms"), client.getJavaProject());
    formOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IForm, project));
    executeBuildAssertNoCompileErrors(formOp);
    synchronized (formCountHolder) {
      while (formCountHolder.getValue() == -1) {
        formCountHolder.wait();
      }
    }
    // expect created form
    Assert.assertEquals(2, formCountHolder.getValue().intValue());
  }

  @Test
  public void testCreateNewService() throws Exception {
    final IType iService = TypeUtility.getType(RuntimeClasses.IService);
    final IPrimaryTypeTypeHierarchy serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iService);
    IType[] subtypes = serviceHierarchy.getAllSubtypes(iService, TypeFilters.getInWorkspaceFilter());
    Assert.assertEquals(2, subtypes.length);
    final IntegerHolder serviceCountHolder = new IntegerHolder(-1);
    serviceHierarchy.addHierarchyListener(new ITypeHierarchyChangedListener() {
      @Override
      public void hierarchyInvalidated() {
        serviceCountHolder.setValue(serviceHierarchy.getAllSubtypes(iService, TypeFilters.getInWorkspaceFilter()).length);
        synchronized (serviceCountHolder) {
          serviceCountHolder.notifyAll();
        }
      }
    });
    IScoutBundle clientBundle = ScoutTypeUtility.getScoutBundle(getProject(BUNDLE_NAME_CLIENT));
    SdkAssert.assertNotNull(clientBundle);
    IScoutBundle sharedBundle = ScoutTypeUtility.getScoutBundle(getProject(BUNDLE_NAME_SHARED));
    SdkAssert.assertNotNull(sharedBundle);
    IScoutBundle serverBundle = ScoutTypeUtility.getScoutBundle(getProject(BUNDLE_NAME_SERVER));
    SdkAssert.assertNotNull(serverBundle);
    ServiceNewOperation serviceOp = new ServiceNewOperation("ITestService", "TestService");
    serviceOp.addProxyRegistrationProject(clientBundle.getJavaProject());
    serviceOp.addServiceRegistrationProject(serverBundle.getJavaProject());
    serviceOp.setImplementationProject(serverBundle.getJavaProject());
    serviceOp.setInterfaceProject(sharedBundle.getJavaProject());
    serviceOp.addInterfaceInterfaceSignature(SignatureCache.createTypeSignature(RuntimeClasses.IService2));
    serviceOp.setInterfacePackageName(sharedBundle.getDefaultPackage(IDefaultTargetPackage.SHARED_SERVICES) + ".notexisting");
    serviceOp.setImplementationPackageName(serverBundle.getDefaultPackage(IDefaultTargetPackage.SERVER_SERVICES) + ".notexisting");
    serviceOp.setImplementationSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IService, serverBundle.getJavaProject()));
    executeBuildAssertNoCompileErrors(serviceOp);
    synchronized (serviceCountHolder) {
      while (serviceCountHolder.getValue() == -1) {
        serviceCountHolder.wait();
      }
    }
    // expect created form
    Assert.assertEquals(4, serviceCountHolder.getValue().intValue());
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    clearWorkspace();
  }
}
