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

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.typecache.PrimaryTypeHierarchy;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h3>{@link RecreateTypeTest}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 15.07.2011
 */
public class RecreateTypeTest extends AbstractScoutSdkTest {

  private static final String CLIENT_BUNDLE_NAME = "test.client";
  private static final String SHARED_BUNDLE_NAME = "test.shared";

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/util/typeCache", CLIENT_BUNDLE_NAME, SHARED_BUNDLE_NAME);
    SdkAssert.assertExist(JavaCore.create(getProject(CLIENT_BUNDLE_NAME)));
    SdkAssert.assertExist(JavaCore.create(getProject(SHARED_BUNDLE_NAME)));
  }

  @Test
  public void testRecreateType() throws Exception {
    IType iformField = TypeUtility.getType(RuntimeClasses.IFormField);
    PrimaryTypeHierarchy primaryFormFieldHierarchy = (PrimaryTypeHierarchy) TypeUtility.getPrimaryTypeHierarchy(iformField);
    Assert.assertFalse(primaryFormFieldHierarchy.isCreated());
    IProject clientProject = getProject("test.client");
    IScoutBundle clientBundle = ScoutTypeUtility.getScoutBundle(clientProject);
    // ensure created
    // create new MyAbstractFormField
    IType abstractMyStringField = createType(clientBundle, "AbstractMyStringField", "test.client.ui.custom.field");
    Assert.assertTrue(primaryFormFieldHierarchy.contains(abstractMyStringField));

    // delete MyAbstractFormField
    deleteType(abstractMyStringField);
    Assert.assertFalse(primaryFormFieldHierarchy.contains(abstractMyStringField));

    // recreate
    abstractMyStringField = createType(clientBundle, "AbstractMyStringField", "test.client.ui.custom.field");
    Assert.assertTrue(primaryFormFieldHierarchy.contains(abstractMyStringField));

    // delete MyAbstractFormField
    deleteType(abstractMyStringField);
    Assert.assertFalse(primaryFormFieldHierarchy.contains(abstractMyStringField));

  }

  private IType createType(IScoutBundle bundle, String typeName, String packageName) throws Exception {
    // create new MyAbstractFormField
    PrimaryTypeNewOperation op = new PrimaryTypeNewOperation(typeName, packageName, ScoutUtility.getJavaProject(bundle));
    op.setFlags(Flags.AccAbstract | Flags.AccPublic);
    op.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IStringField, ScoutUtility.getJavaProject(bundle)));
    executeBuildAssertNoCompileErrors(op);
    return op.getCreatedType();
  }

  private void deleteType(IType abstractMyStringField) throws Exception {
    JavaElementDeleteOperation deleteTypeOp = new JavaElementDeleteOperation();
    deleteTypeOp.addMember(abstractMyStringField);
    executeBuildAssertNoCompileErrors(deleteTypeOp);
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    clearWorkspace();
  }
}
