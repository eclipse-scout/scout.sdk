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

import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.ToolbuttonNewOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Test;

/**
 * <h3>{@link ToolbuttonNewOperationTest}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class ToolbuttonNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewToolbutton() throws Exception {
    SdkAssert.assertExist(getSharedJavaProject());
    IType desktop = TypeUtility.getType("sample.client.ui.desktop.Desktop");
    ToolbuttonNewOperation toolbuttonOp = new ToolbuttonNewOperation("NewToolButton01", desktop);
    toolbuttonOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.client.ui.action.tool.AbstractToolButton", true));
    executeBuildAssertNoCompileErrors(toolbuttonOp);
    IType createdToolbutton = toolbuttonOp.getCreatedType();
    SdkAssert.assertExist(createdToolbutton);
    SdkAssert.assertPublic(createdToolbutton).assertNoMoreFlags();
    Set<IType> tb = TypeUtility.getInnerTypesOrdered(desktop, TypeUtility.getType(RuntimeClasses.IToolButton), ScoutTypeComparators.getOrderAnnotationComparator());
    IType[] toolButtons = tb.toArray(new IType[tb.size()]);
    SdkAssert.assertEquals(4, toolButtons.length);
    SdkAssert.assertEquals(toolbuttonOp.getElementName(), toolButtons[3].getElementName());
    // clean up
    JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
    delOp.addMember(createdToolbutton);
    executeBuildAssertNoCompileErrors(delOp);
    tb = TypeUtility.getInnerTypesOrdered(desktop, TypeUtility.getType(RuntimeClasses.IToolButton), ScoutTypeComparators.getOrderAnnotationComparator());
    SdkAssert.assertEquals(3, tb.size());
  }

  @Test
  public void testNewToolbuttonWithTextAndSibling() throws Exception {
    SdkAssert.assertExist(getSharedJavaProject());
    IType desktop = TypeUtility.getType("sample.client.ui.desktop.Desktop");
    ToolbuttonNewOperation toolbuttonOp = new ToolbuttonNewOperation("NewToolButton02", desktop);
    toolbuttonOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.client.ui.action.tool.AbstractToolButton", true));
    toolbuttonOp.setSibling(TypeUtility.getType("sample.client.ui.desktop.Desktop.SecondTool"));
    INlsProject nlsProject = ScoutTypeUtility.findNlsProject(getSharedJavaProject());
    INlsEntry entry = nlsProject.getEntry("Text02");
    toolbuttonOp.setNlsEntry(entry);

    executeBuildAssertNoCompileErrors(toolbuttonOp);
    IType createdToolbutton = toolbuttonOp.getCreatedType();
    SdkAssert.assertExist(createdToolbutton);
    SdkAssert.assertPublic(createdToolbutton).assertNoMoreFlags();
    IMethod getTextMethod = SdkAssert.assertMethodExist(createdToolbutton, SdkProperties.METHOD_NAME_GET_CONFIGURED_TEXT);
    SdkAssert.assertPublic(getTextMethod).assertNoMoreFlags();
    Set<IType> tb = TypeUtility.getInnerTypesOrdered(desktop, TypeUtility.getType(RuntimeClasses.IToolButton), ScoutTypeComparators.getOrderAnnotationComparator());
    IType[] toolButtons = tb.toArray(new IType[tb.size()]);
    SdkAssert.assertEquals(4, toolButtons.length);
    SdkAssert.assertEquals(toolbuttonOp.getElementName(), toolButtons[1].getElementName());
    // clean up
    JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
    delOp.addMember(createdToolbutton);
    executeBuildAssertNoCompileErrors(delOp);
    tb = TypeUtility.getInnerTypesOrdered(desktop, TypeUtility.getType(RuntimeClasses.IToolButton), ScoutTypeComparators.getOrderAnnotationComparator());
    SdkAssert.assertEquals(3, tb.size());
  }
}
