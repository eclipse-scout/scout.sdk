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

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.util.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h1>AnnotationTest</h1>
 * <p>
 */
public class HierarchyTest extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("util/typeCache", "test.client", "test.shared");
  }

  @AfterClass
  public static void cleanUpWorkspace() throws Exception {
    deleteProjects("test.client", "test.shared");
  }

  @Test
  public void testPrimaryTypeHierarchy() {

    IType companyForm = ScoutSdk.getType("test.client.ui.forms.CompanyForm");
    Assert.assertTrue(TypeUtility.exists(companyForm));
    IType iformField = ScoutSdk.getType(RuntimeClasses.IFormField);
    IPrimaryTypeTypeHierarchy primaryFormFieldHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iformField);
    Assert.assertFalse(primaryFormFieldHierarchy.isCreated());
    ITypeHierarchy companyFormHierarchy = primaryFormFieldHierarchy.combinedTypeHierarchy(companyForm);
    Assert.assertTrue(primaryFormFieldHierarchy.isCreated());
    IType mainBox = companyForm.getType("MainBox");
    Assert.assertTrue(TypeUtility.exists(mainBox));
    IType[] formFields = TypeUtility.getInnerTypes(mainBox, TypeFilters.getSubtypeFilter(iformField, companyFormHierarchy), TypeComparators.getOrderAnnotationComparator());
    Assert.assertTrue(formFields.length == 3);
    Assert.assertEquals(formFields[0].getElementName(), "NameField");
    Assert.assertEquals(formFields[1].getElementName(), "SinceField");
    Assert.assertEquals(formFields[2].getElementName(), "DetailsGroup");
  }

  @Test
  public void testModifyPrimaryTypeHierarchy() throws Exception {
    IType companyForm = ScoutSdk.getType("test.client.ui.forms.CompanyForm");
    Assert.assertTrue(TypeUtility.exists(companyForm));
    IType iformField = ScoutSdk.getType(RuntimeClasses.IFormField);
    IPrimaryTypeTypeHierarchy primaryFormFieldHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iformField);

    // create new myAbstractFormField
    ScoutTypeNewOperation op = new ScoutTypeNewOperation("AbstractMyStringField", "test.client.ui.custom.field", SdkTypeUtility.getScoutBundle(companyForm));
    op.setTypeModifiers(Flags.AccAbstract | Flags.AccPublic);
    op.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractStringField, true));

    OperationJob job = new OperationJob(op);
    job.schedule();
    job.join();

    IType t = op.getCreatedType();
    Assert.assertFalse(primaryFormFieldHierarchy.isCreated());
    Assert.assertTrue(primaryFormFieldHierarchy.contains(op.getCreatedType()));
    Assert.assertTrue(primaryFormFieldHierarchy.isCreated());
    JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
    delOp.addMember(t);
    job = new OperationJob(op);
    job.schedule();
    job.join();
    Assert.assertFalse(primaryFormFieldHierarchy.isCreated());
  }
}
