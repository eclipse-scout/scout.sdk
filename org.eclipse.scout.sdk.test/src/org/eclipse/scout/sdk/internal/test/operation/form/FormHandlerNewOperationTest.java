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
package org.eclipse.scout.sdk.internal.test.operation.form;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.form.FormHandlerDeleteOperation;
import org.eclipse.scout.sdk.operation.form.FormHandlerNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link FormHandlerNewOperationTest}</h3>
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class FormHandlerNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewFormHandler() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    IType form = TypeUtility.getType("sample.client.empty.EmptyForm");
    FormHandlerNewOperation newHandlerOp = new FormHandlerNewOperation("NewHandler", form);
    executeBuildAssertNoCompileErrors(newHandlerOp);
    IType formHandler = newHandlerOp.getCreatedType();
    SdkAssert.assertExist(formHandler);
    IMethod startMethod = newHandlerOp.getCreatedStartMethod();
    SdkAssert.assertExist(startMethod);
    SdkAssert.assertHasSuperType(formHandler, RuntimeClasses.IFormHandler);

    // clean up
    FormHandlerDeleteOperation delOp = new FormHandlerDeleteOperation(formHandler, true);
    executeBuildAssertNoCompileErrors(delOp);
  }
}
