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
package org.eclipse.scout.sdk.internal.test.operation.jdt;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.operation.jdt.field.FieldNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.testing.TestWorkspaceUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h3>{@link FieldNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 08.02.2013
 */
public class FieldNewOperationTest extends AbstractScoutSdkTest {

  private Long foo;

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/operation/method", "test.client", "test.shared");
  }

  @Test
  public void createPrivateField() throws Exception {
    IType declaringType = TypeUtility.getType("test.client.MethodBodyTest");
    String fieldName = "myField";
    FieldNewOperation fieldOp = new FieldNewOperation(fieldName, declaringType);
    fieldOp.setFlags(Flags.AccPrivate);
    fieldOp.setSignature(Signature.createTypeSignature(String.class.getName(), true));
    TestWorkspaceUtility.executeAndBuildWorkspace(fieldOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertTrue(TypeUtility.exists(fieldOp.getCreatedField()));
    IField field = declaringType.getField(fieldName);
    Assert.assertTrue(TypeUtility.exists(field));
    Assert.assertTrue(Flags.isPrivate(field.getFlags()));
    Assert.assertFalse(Flags.isProtected(field.getFlags()));
    Assert.assertFalse(Flags.isPublic(field.getFlags()));
    Assert.assertFalse(Flags.isStatic(field.getFlags()));
    Assert.assertFalse(Flags.isFinal(field.getFlags()));
  }

  @Test
  public void testFieldUsingAnImport() throws Exception {
    IType declaringType = TypeUtility.getType("test.client.MethodBodyTest");
    String fieldName = "listField";
    FieldNewOperation fieldOp = new FieldNewOperation(fieldName, declaringType);
    fieldOp.setFlags(Flags.AccPrivate);
    fieldOp.setSignature(Signature.createTypeSignature(ArrayList.class.getName() + "<" + File.class.getName() + ">", true));
    TestWorkspaceUtility.executeAndBuildWorkspace(fieldOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertTrue(TypeUtility.exists(fieldOp.getCreatedField()));
    IField field = declaringType.getField(fieldName);
    Assert.assertTrue(TypeUtility.exists(field));
    Assert.assertTrue(Flags.isPrivate(field.getFlags()));
    Assert.assertEquals(field.getTypeSignature(), Signature.createTypeSignature(ArrayList.class.getSimpleName() + "<" + File.class.getSimpleName() + ">", false));
  }

  @Test
  public void createPublicStaticField() throws Exception {
    IType declaringType = TypeUtility.getType("test.client.MethodBodyTest");
    String fieldName = "PUBLIC_STATIC_FIELD";
    FieldNewOperation fieldOp = new FieldNewOperation(fieldName, declaringType);
    fieldOp.setFlags(Flags.AccPublic | Flags.AccStatic);
    fieldOp.setSignature(Signature.createTypeSignature(Integer.class.getName(), true));
    TestWorkspaceUtility.executeAndBuildWorkspace(fieldOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertTrue(TypeUtility.exists(fieldOp.getCreatedField()));
    IField field = declaringType.getField(fieldName);
    Assert.assertTrue(TypeUtility.exists(field));
    Assert.assertFalse(Flags.isPrivate(field.getFlags()));
    Assert.assertFalse(Flags.isProtected(field.getFlags()));
    Assert.assertTrue(Flags.isPublic(field.getFlags()));
    Assert.assertTrue(Flags.isStatic(field.getFlags()));
    Assert.assertFalse(Flags.isFinal(field.getFlags()));
  }

  @Test
  public void createPublicStaticFinalField() throws Exception {
    IType declaringType = TypeUtility.getType("test.client.MethodBodyTest");
    String fieldName = "PUBLIC_STATIC_FINAL_FIELD";
    FieldNewOperation fieldOp = new FieldNewOperation(fieldName, declaringType);
    fieldOp.setFlags(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    fieldOp.setSignature(Signature.SIG_INT);
    fieldOp.setValue("1");
    TestWorkspaceUtility.executeAndBuildWorkspace(fieldOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertTrue(TypeUtility.exists(fieldOp.getCreatedField()));
    IField field = declaringType.getField(fieldName);
    Assert.assertTrue(TypeUtility.exists(field));
    Assert.assertFalse(Flags.isPrivate(field.getFlags()));
    Assert.assertFalse(Flags.isProtected(field.getFlags()));
    Assert.assertTrue(Flags.isPublic(field.getFlags()));
    Assert.assertTrue(Flags.isStatic(field.getFlags()));
    Assert.assertTrue(Flags.isFinal(field.getFlags()));
    Object constant = field.getConstant();
    Assert.assertEquals(1, constant);
  }

  @Test
  public void createFieldWithAnnotation() throws Exception {
    IType declaringType = TypeUtility.getType("test.client.MethodBodyTest");
    String fieldName = "fieldWithAnnotation";
    FieldNewOperation fieldOp = new FieldNewOperation(fieldName, declaringType);
    fieldOp.setFlags(Flags.AccPrivate);
    fieldOp.setSignature(Signature.createTypeSignature(String.class.getName(), true));
    fieldOp.addAnnotationSourceBuilder(new AnnotationSourceBuilder(Signature.createTypeSignature(Deprecated.class.getName(), true)));
    TestWorkspaceUtility.executeAndBuildWorkspace(fieldOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertTrue(TypeUtility.exists(fieldOp.getCreatedField()));
    IField field = declaringType.getField(fieldName);
    Assert.assertTrue(TypeUtility.exists(field));
    Assert.assertTrue(Flags.isPrivate(field.getFlags()));
    Assert.assertFalse(Flags.isProtected(field.getFlags()));
    Assert.assertFalse(Flags.isPublic(field.getFlags()));
    Assert.assertFalse(Flags.isStatic(field.getFlags()));
    Assert.assertFalse(Flags.isFinal(field.getFlags()));
    Assert.assertTrue(TypeUtility.exists(field.getAnnotation("Deprecated")));

  }
}
