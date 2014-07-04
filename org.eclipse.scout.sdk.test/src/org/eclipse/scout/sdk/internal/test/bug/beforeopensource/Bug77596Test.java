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
package org.eclipse.scout.sdk.internal.test.bug.beforeopensource;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h1>Bug 77'596</h1>
 */
public class Bug77596Test extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/bugsBeforeOpensource/77596", "com.bsiag.miniapp.client", "com.bsiag.miniapp.shared");
  }

  private IStructuredType getCompanyFormHelper() {
    IType form = SdkAssert.assertTypeExists("com.bsiag.miniapp.client.ui.forms.CompanyForm");
    return ScoutTypeUtility.createStructuredForm(form);
  }

  @Test
  public void testStructure() throws Exception {
    IStructuredType helper = getCompanyFormHelper();
    Assert.assertTrue(helper.getElements(CATEGORIES.FIELD_LOGGER).size() == 1);
    Assert.assertTrue(helper.getElements(CATEGORIES.FIELD_STATIC).size() == 1);
    Assert.assertTrue(helper.getElements(CATEGORIES.FIELD_MEMBER).size() == 3);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_CONSTRUCTOR).size() == 1);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_CONFIG_EXEC).size() == 0);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_CONFIG_PROPERTY).size() == 1);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_FORM_DATA_BEAN).size() == 4);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_OVERRIDDEN).size() == 0);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_START_HANDLER).size() == 2);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_INNER_TYPE_GETTER).size() == 32);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_LOCAL_BEAN).size() == 2);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_UNCATEGORIZED).size() == 1);
  }

  @Test
  public void testSiblingConfigExec() throws Exception {
    IStructuredType helper = getCompanyFormHelper();
    // fist
    IJavaElement sibling = helper.getSiblingMethodConfigExec("execInitForm");
    Assert.assertTrue(sibling.getElementName().equals("getCompanyNr"));
  }

  @Test
  public void testSiblingFieldGetter() throws Exception {
    IStructuredType helper = getCompanyFormHelper();
    // a getField
    IJavaElement sibling = helper.getSiblingMethodFieldGetter("getOpenBankAccount");
    Assert.assertTrue(sibling.getElementName().equals("getOpenBillsField"));
    // last getField
    sibling = helper.getSiblingMethodFieldGetter("getZoraField");
    Assert.assertTrue(sibling.getElementName().equals("isDataChangedTriggerDisabled"));
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    clearWorkspace();
  }
}
