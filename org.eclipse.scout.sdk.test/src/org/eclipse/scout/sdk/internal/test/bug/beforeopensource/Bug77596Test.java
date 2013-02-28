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
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h1>Bug 77'596</h1>
 * <p>
 */
public class Bug77596Test extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("bugsBeforeOpensource/77596", "com.bsiag.miniapp.client", "com.bsiag.miniapp.shared");
  }

  private IStructuredType getCompanyFormHelper() {
    IType form = TypeUtility.getType("com.bsiag.miniapp.client.ui.forms.CompanyForm");
    Assert.assertTrue(TypeUtility.exists(form));
    IStructuredType helper = null;
    /*try {
      TuningUtility.startTimer();*/
    helper = ScoutTypeUtility.createStructuredForm(form);
    /*}
    finally {
      TuningUtility.stopTimer("time to build structure helper.");
    }*/
    return helper;
  }

  @Test
  public void testStructure() throws Exception {
    IStructuredType helper = getCompanyFormHelper();
    Assert.assertTrue(helper.getElements(CATEGORIES.FIELD_LOGGER).length == 1);
    Assert.assertTrue(helper.getElements(CATEGORIES.FIELD_STATIC).length == 1);
    Assert.assertTrue(helper.getElements(CATEGORIES.FIELD_MEMBER).length == 4);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_CONSTRUCTOR).length == 1);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_CONFIG_EXEC).length == 0);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_CONFIG_PROPERTY).length == 2);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_FORM_DATA_BEAN).length == 4);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_OVERRIDDEN).length == 0);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_START_HANDLER).length == 2);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_INNER_TYPE_GETTER).length == 32);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_LOCAL_BEAN).length == 2);
    Assert.assertTrue(helper.getElements(CATEGORIES.METHOD_UNCATEGORIZED).length == 1);
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
