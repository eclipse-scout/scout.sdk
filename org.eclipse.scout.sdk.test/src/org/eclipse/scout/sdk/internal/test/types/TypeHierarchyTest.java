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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.holders.IntegerHolder;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.FormNewOperation;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h1>TypeHierarchyTest</h1>
 * <p>
 */
public class TypeHierarchyTest extends AbstractScoutSdkTest {

  private static String BUNDLE_NAME_CLIENT = "test.client";
  private static String BUNDLE_NAME_SHARED = "test.shared";

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("util/typeCache", BUNDLE_NAME_CLIENT, BUNDLE_NAME_SHARED);
  }

  @AfterClass
  public static void cleanUpWorkspace() throws Exception {
    deleteProjects(BUNDLE_NAME_CLIENT, BUNDLE_NAME_SHARED);
  }

  @Test
  public void testPrimaryTypeHierarchy() {
    IType companyForm = TypeUtility.getType("test.client.ui.forms.CompanyForm");
    Assert.assertTrue(TypeUtility.exists(companyForm));
    IType iformField = TypeUtility.getType(RuntimeClasses.IFormField);
    IPrimaryTypeTypeHierarchy primaryFormFieldHierarchy = TypeUtility.getPrimaryTypeHierarchy(iformField);
    ITypeHierarchy companyFormHierarchy = primaryFormFieldHierarchy.combinedTypeHierarchy(companyForm);
    Assert.assertTrue(primaryFormFieldHierarchy.isCreated());
    IType mainBox = companyForm.getType("MainBox");
    Assert.assertTrue(TypeUtility.exists(mainBox));
    IType[] formFields = TypeUtility.getInnerTypes(mainBox, TypeFilters.getSubtypeFilter(iformField, companyFormHierarchy), ScoutTypeComparators.getOrderAnnotationComparator());
    Assert.assertTrue(formFields.length == 3);
    Assert.assertEquals(formFields[0].getElementName(), "NameField");
    Assert.assertEquals(formFields[1].getElementName(), "SinceField");
    Assert.assertEquals(formFields[2].getElementName(), "DetailsGroup");
  }

  @Test
  public void testCreateNewPrimaryType() throws Exception {
    final IJavaProject project = JavaCore.create(getProject(BUNDLE_NAME_CLIENT));
    final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);
    final IPrimaryTypeTypeHierarchy primaryFormFieldHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
    IType[] subtypes = primaryFormFieldHierarchy.getAllSubtypes(iForm, TypeFilters.getClassesInProject(project));
    Assert.assertEquals(1, subtypes.length);
    final IntegerHolder formCountHolder = new IntegerHolder(-1);
    primaryFormFieldHierarchy.addHierarchyListener(new ITypeHierarchyChangedListener() {
      @Override
      public void handleEvent(int eventType, IType type) {
        switch (eventType) {
          case POST_TYPE_REMOVING:
          case POST_TYPE_ADDING:
          case POST_TYPE_CHANGED:
            formCountHolder.setValue(primaryFormFieldHierarchy.getAllSubtypes(iForm, TypeFilters.getClassesInProject(project)).length);
            synchronized (formCountHolder) {
              formCountHolder.notifyAll();
            }
            break;
        }
      }
    });
    FormNewOperation formOp = new FormNewOperation();
    formOp.setTypeName("ANewForm");
    formOp.setClientBundle(ScoutSdkCore.getScoutWorkspace().getScoutBundle(project.getProject()));
    formOp.setSuperType(Signature.createTypeSignature(RuntimeClasses.AbstractForm, true));
    OperationJob job = new OperationJob(formOp);
    job.schedule();
    job.join();
    synchronized (formCountHolder) {
      if (formCountHolder.getValue() == -1) {
        formCountHolder.wait();
      }
    }
    // expect created form
    Assert.assertEquals(2, formCountHolder.getValue().intValue());
  }
}
