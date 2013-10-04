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
package org.eclipse.scout.sdk.internal.test.operation;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.page.PageNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.junit.Test;

/**
 * <h3>{@link PageNewOperationTest}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class PageNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewPageWithTable() throws Exception {
    PageNewOperation newOp = new PageNewOperation("Test01Page", getClientJavaProject().getElementName() + ".page.output", getClientJavaProject());
    newOp.setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.AbstractPageWithTable));
    newOp.setFormatSource(true);

    executeBuildAssertNoCompileErrors(newOp);

    IType page = newOp.getCreatedPage();
    SdkAssert.assertExist(page);
    SdkAssert.assertPublic(page).assertNoMoreFlags();
    SdkAssert.assertTypeExists(page, SdkProperties.TYPE_NAME_OUTLINE_WITH_TABLE_TABLE);
    SdkAssert.assertHasSuperType(page, RuntimeClasses.IPageWithTable);
  }

  @Test
  public void testNewPageWithTableExtendsible() throws Exception {
    PageNewOperation newOp = new PageNewOperation("Test01Page", getClientJavaProject().getElementName() + ".page.output", getClientJavaProject());
    newOp.setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.AbstractExtensiblePageWithTable));
    newOp.setFormatSource(true);

    executeBuildAssertNoCompileErrors(newOp);

    IType page = newOp.getCreatedPage();
    SdkAssert.assertExist(page);
    SdkAssert.assertPublic(page).assertNoMoreFlags();
    SdkAssert.assertTypeExists(page, SdkProperties.TYPE_NAME_OUTLINE_WITH_TABLE_TABLE);
    SdkAssert.assertHasSuperType(page, RuntimeClasses.IPageWithTable);
  }

  @Test
  public void testNewPageWithNodes() throws Exception {
    PageNewOperation newOp = new PageNewOperation("Test01Page", getClientJavaProject().getElementName() + ".page.output", getClientJavaProject());
    newOp.setSuperTypeSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes"));
    newOp.setFormatSource(true);

    executeBuildAssertNoCompileErrors(newOp);

    IType page = newOp.getCreatedPage();
    SdkAssert.assertExist(page);
    SdkAssert.assertPublic(page).assertNoMoreFlags();
    SdkAssert.assertEquals(0, page.getTypes().length);
    SdkAssert.assertHasSuperType(page, RuntimeClasses.IPageWithNodes);
  }
}
