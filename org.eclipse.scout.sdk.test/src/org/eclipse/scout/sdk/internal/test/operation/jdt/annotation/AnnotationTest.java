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
package org.eclipse.scout.sdk.internal.test.operation.jdt.annotation;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.operation.jdt.annotation.AnnotationNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h1>AnnotationTest</h1>
 * <p>
 */
public class AnnotationTest extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/operation/annotations", "test.client", "test.shared");
  }

  @Test
  public void testCreateAnnotation() throws Exception {
    IType testForm = SdkAssert.assertTypeExists("test.client.ui.forms.Test1Form");
    IMethod method = SdkAssert.assertMethodExist(testForm, "getStringMember");

    AnnotationNewOperation op = new AnnotationNewOperation(SignatureCache.createTypeSignature(FormData.class.getName()), method);
    executeBuildAssertNoCompileErrors(op);

    Assert.assertTrue(method.getAnnotations().length == 1);
    Assert.assertTrue(TypeUtility.exists(JdtUtility.getAnnotation(method, RuntimeClasses.FormData)));
  }

  @Test
  public void testCreateAnnotationWithComment() throws Exception {
    IType testForm = SdkAssert.assertTypeExists("test.client.ui.forms.Test1Form");
    IMethod method = SdkAssert.assertMethodExist(testForm, "setStringMember");

    AnnotationNewOperation op = new AnnotationNewOperation(SignatureCache.createTypeSignature(FormData.class.getName()), method);
    executeBuildAssertNoCompileErrors(op);

    Assert.assertTrue(method.getAnnotations().length == 1);
    Assert.assertTrue(TypeUtility.exists(JdtUtility.getAnnotation(method, RuntimeClasses.FormData)));
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    clearWorkspace();
  }
}
