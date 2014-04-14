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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
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
    setupWorkspace("operation/annotations", "test.client", "test.shared");
  }

  @Test
  public void testCreateAnnotation() throws Exception {
    IType testForm = TypeUtility.getType("test.client.ui.forms.Test1Form");
    Assert.assertTrue(TypeUtility.exists(testForm));
    IMethod method = TypeUtility.getMethod(testForm, "getStringMember");
    Assert.assertTrue(TypeUtility.exists(method));

    AnnotationCreateOperation op = new AnnotationCreateOperation(method, Signature.createTypeSignature(FormData.class.getName(), true));
    OperationJob job = new OperationJob(op);
    job.schedule();
    job.join();

    Assert.assertTrue(method.getAnnotations().length == 1);
    Assert.assertTrue(TypeUtility.exists(JdtUtility.getAnnotation(method, RuntimeClasses.FormData)));
  }

  @Test
  public void testCreateAnnotationWithComment() throws Exception {
    IType testForm = TypeUtility.getType("test.client.ui.forms.Test1Form");
    Assert.assertTrue(TypeUtility.exists(testForm));
    IMethod method = TypeUtility.getMethod(testForm, "setStringMember");
    Assert.assertTrue(TypeUtility.exists(method));

    AnnotationCreateOperation op = new AnnotationCreateOperation(method, Signature.createTypeSignature(FormData.class.getName(), true));
    OperationJob job = new OperationJob(op);
    job.schedule();
    job.join();

    Assert.assertTrue(method.getAnnotations().length == 1);
    Assert.assertTrue(TypeUtility.exists(JdtUtility.getAnnotation(method, RuntimeClasses.FormData)));
  }

}