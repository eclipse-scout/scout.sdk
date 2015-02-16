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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PrimaryTypeHierarchyTest extends AbstractScoutSdkTest {

  private static final String BUNDLE_NAME_CLIENT = "testTypeCache.client";
  private static final String BUNDLE_NAME_SHARED = "testTypeCache.shared";
  private static final String BUNDLE_NAME_SERVER = "testTypeCache.server";

  private static ITypeHierarchy iGroupBoxHierarchy;
  private static ITypeHierarchy iGroupBoxPrimaryHierarchy;

  private static IType abstractGroupBox;
  private static IType mainBox;
  private static IType abstractDetailsGroup;

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/util/typeCache", BUNDLE_NAME_CLIENT, BUNDLE_NAME_SHARED, BUNDLE_NAME_SERVER);

    abstractGroupBox = TypeUtility.getType(RuntimeClasses.AbstractGroupBox);
    iGroupBoxPrimaryHierarchy = TypeUtility.getPrimaryTypeHierarchy(abstractGroupBox);
    iGroupBoxHierarchy = TypeUtility.getTypeHierarchy(abstractGroupBox);

    IType companyForm = SdkAssert.assertTypeExists("test.client.ui.forms.CompanyForm");
    mainBox = SdkAssert.assertTypeExists(companyForm, "MainBox");

    abstractDetailsGroup = SdkAssert.assertTypeExists("test.client.ui.template.formfield.AbstractDetailsGroup");
  }

  @Test
  public void testContains() {
    Assert.assertTrue(iGroupBoxHierarchy.contains(mainBox));
    Assert.assertFalse(iGroupBoxPrimaryHierarchy.contains(mainBox));

    Assert.assertTrue(iGroupBoxHierarchy.contains(abstractDetailsGroup));
    Assert.assertTrue(iGroupBoxPrimaryHierarchy.contains(abstractDetailsGroup));
  }

  @Test
  public void testGetSuperclass() {
    Assert.assertEquals(abstractGroupBox, iGroupBoxHierarchy.getSuperclass(mainBox));
    Assert.assertEquals(null, iGroupBoxPrimaryHierarchy.getSuperclass(mainBox));

    Assert.assertEquals(abstractGroupBox, iGroupBoxHierarchy.getSuperclass(abstractDetailsGroup));
    Assert.assertEquals(abstractGroupBox, iGroupBoxPrimaryHierarchy.getSuperclass(abstractDetailsGroup));
  }

  @Test
  public void testIsSubtype() {
    Assert.assertTrue(iGroupBoxHierarchy.isSubtype(abstractGroupBox, mainBox));
    Assert.assertFalse(iGroupBoxPrimaryHierarchy.isSubtype(abstractGroupBox, mainBox));

    Assert.assertTrue(iGroupBoxHierarchy.isSubtype(abstractGroupBox, abstractDetailsGroup));
    Assert.assertTrue(iGroupBoxPrimaryHierarchy.isSubtype(abstractGroupBox, abstractDetailsGroup));
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    clearWorkspace();
  }
}
