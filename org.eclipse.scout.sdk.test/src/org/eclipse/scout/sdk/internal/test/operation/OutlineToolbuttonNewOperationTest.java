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
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.OutlineToolbuttonNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Test;

/**
 * <h3>{@link OutlineToolbuttonNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2013
 *        TODO {@link OutlineToolbuttonNewOperation} is not usable anymore or wasn't ever usable. The toolbutton
 *        interface does not satisfy the viewbuttons
 */
public class OutlineToolbuttonNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testCreateToolbutton() throws Exception {
    SdkAssert.assertExist(getSharedJavaProject());
    IType desktop = TypeUtility.getType("sample.client.ui.desktop.Desktop");

    OutlineToolbuttonNewOperation newOp = new OutlineToolbuttonNewOperation("OutlineToolButton01", desktop, true);
    newOp.setOutlineType(TypeUtility.getType("sample.client.ui.desktop.outlines.FirstOutline"));

    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, newOp);
    IType outlineButton = newOp.getCreatedType();
    SdkAssert.assertExist(outlineButton);
  }
}
