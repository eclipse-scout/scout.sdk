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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.outline.OutlineNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Test;

/**
 * <h3>{@link OutlineNewOperationTest}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 15.04.2013
 */
public class OutlineNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testApi() throws Exception {
    IType dektopExtension = TypeUtility.getType(IRuntimeClasses.IDesktopExtension);
    SdkAssert.assertMethodExist(dektopExtension, "getCoreDesktop");
    IType abstractDesktopExtension = TypeUtility.getType("org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension");
    SdkAssert.assertExist(abstractDesktopExtension);
    SdkAssert.assertMethodExist(abstractDesktopExtension, "getConfiguredOutlines");
    IType abstractDesktop = TypeUtility.getType("org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop");
    SdkAssert.assertMethodExist(abstractDesktop, "getConfiguredOutlines");
  }

  @Test
  public void testNewOutline() throws Exception {
    OutlineNewOperation outlineOp = new OutlineNewOperation("Test01Outline", getClientJavaProject().getElementName() + ".outline.output", getClientJavaProject());
    outlineOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IOutline, getClientJavaProject()));

    executeBuildAssertNoCompileErrors(outlineOp);

    IType outline = outlineOp.getCreatedType();
    SdkAssert.assertExist(outline);
    testApiOfTest01Outline();

  }

  @Test
  public void testNewOutlineOnDesktop() throws Exception {
    OutlineNewOperation outlineOp = new OutlineNewOperation("Test02Outline", getClientJavaProject().getElementName() + ".outline.output", getClientJavaProject());
    outlineOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IOutline, getClientJavaProject()));
    IType desktop = TypeUtility.getType("sample.client.ui.desktop.Desktop");
    outlineOp.setDesktopType(desktop);
    // nls
    INlsProject nlsProject = ScoutTypeUtility.findNlsProject(getSharedJavaProject());
    INlsEntry entry = nlsProject.getEntry("Text02");
    outlineOp.setNlsEntry(entry);

    executeBuildAssertNoCompileErrors(outlineOp);
    IType outline = outlineOp.getCreatedType();
    SdkAssert.assertExist(outline);

    testApiOfTest02Outline();

    SdkAssert.assertMethodExist(desktop, "getConfiguredOutlines");
    IType viewButton = SdkAssert.assertTypeExists(desktop, "Test02Outline" + SdkProperties.SUFFIX_VIEW_BUTTON);
    SdkAssert.assertMethodExist(viewButton, "getConfiguredText");
    SdkAssert.assertHasSuperType(viewButton, RuntimeClasses.IViewButton);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfTest01Outline() throws Exception {
    // type Test01Outline
    IType test01Outline = SdkAssert.assertTypeExists("sample.client.outline.output.Test01Outline");
    SdkAssert.assertHasFlags(test01Outline, 1);
    SdkAssert.assertHasSuperTypeSignature(test01Outline, "QAbstractExtensibleOutline;");

    // fields of Test01Outline
    SdkAssert.assertEquals("field count of 'Test01Outline'", 0, test01Outline.getFields().length);

    SdkAssert.assertEquals("method count of 'Test01Outline'", 0, test01Outline.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'Test01Outline'", 0, test01Outline.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfTest02Outline() throws Exception {
    // type Test02Outline
    IType test02Outline = SdkAssert.assertTypeExists("sample.client.outline.output.Test02Outline");
    SdkAssert.assertHasFlags(test02Outline, 1);
    SdkAssert.assertHasSuperTypeSignature(test02Outline, "QAbstractExtensibleOutline;");

    // fields of Test02Outline
    SdkAssert.assertEquals("field count of 'Test02Outline'", 0, test02Outline.getFields().length);

    SdkAssert.assertEquals("method count of 'Test02Outline'", 1, test02Outline.getMethods().length);
    IMethod getConfiguredTitle = SdkAssert.assertMethodExist(test02Outline, "getConfiguredTitle", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getConfiguredTitle, "QString;");
    SdkAssert.assertAnnotation(getConfiguredTitle, "java.lang.Override");

    SdkAssert.assertEquals("inner types count of 'Test02Outline'", 0, test02Outline.getTypes().length);
  }

}
