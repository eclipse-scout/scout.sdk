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

import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithJdtTestProject;
import org.eclipse.scout.sdk.operation.form.field.FormFieldDeleteOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.operation.jdt.type.InnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.jdt.type.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.testing.TestWorkspaceUtility;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link TypeNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 08.02.2013
 */
public class TypeNewOperationTest extends AbstractSdkTestWithJdtTestProject {

  @Test
  public void createSimpleTypeTest() throws Exception {
    IJavaProject clientProject = getClientJavaProject();
    Assert.assertTrue(TypeUtility.exists(clientProject));
    String typeName = "SimpleType";
    String packageName = "jdt.test.client.type.output";
    PrimaryTypeNewOperation typeOp = new PrimaryTypeNewOperation(typeName, packageName, clientProject);
    typeOp.setFlags(Flags.AccPublic);
    typeOp.setIcuCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
    typeOp.setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, typeOp);

    ICompilationUnit createdCompilationUnit = typeOp.getCreatedCompilationUnit();
    Assert.assertTrue(TypeUtility.exists(createdCompilationUnit));
    Assert.assertEquals(1, createdCompilationUnit.getTypes().length);
    IType createdType = typeOp.getCreatedType();
    Assert.assertTrue(TypeUtility.exists(createdType));
    Assert.assertTrue(Flags.isPublic(createdType.getFlags()));
    Assert.assertFalse(Flags.isFinal(createdType.getFlags()));
    Assert.assertTrue(TypeUtility.exists(TypeUtility.getType(packageName + "." + typeName)));

    // delete again
    JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
    delOp.addMember(createdType);
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, delOp);
    SdkAssert.assertNotExist(createdCompilationUnit);

  }

  @Test
  public void testInterface() throws Exception {
    IJavaProject clientProject = getClientJavaProject();
    Assert.assertTrue(TypeUtility.exists(clientProject));
    PrimaryTypeNewOperation typeOp = new PrimaryTypeNewOperation("IInterface", "jdt.test.client.type.output", clientProject);
    typeOp.setFlags(Flags.AccInterface | Flags.AccPublic);
    TestWorkspaceUtility.executeAndBuildWorkspace(typeOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertTrue(TypeUtility.exists(typeOp.getCreatedType()));
    Assert.assertTrue(typeOp.getCreatedType().isInterface());
    Assert.assertTrue(Flags.isPublic(typeOp.getCreatedType().getFlags()));
  }

  @Test
  public void testAbstract() throws Exception {
    IJavaProject clientProject = getClientJavaProject();
    Assert.assertTrue(TypeUtility.exists(clientProject));
    PrimaryTypeNewOperation typeOp = new PrimaryTypeNewOperation("AbstractType", TypeUtility.getPackage(clientProject, "jdt.test.client.type.output"));
    typeOp.setFlags(Flags.AccAbstract | Flags.AccPublic);
    TestWorkspaceUtility.executeAndBuildWorkspace(typeOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertTrue(TypeUtility.exists(typeOp.getCreatedType()));
    Assert.assertFalse(typeOp.getCreatedType().isInterface());
    Assert.assertTrue(Flags.isAbstract(typeOp.getCreatedType().getFlags()));
    Assert.assertTrue(Flags.isPublic(typeOp.getCreatedType().getFlags()));
  }

  @Test
  public void testTypeWithAnnotation() throws Exception {
    IJavaProject clientProject = getClientJavaProject();
    Assert.assertTrue(TypeUtility.exists(clientProject));
    PrimaryTypeNewOperation typeOp = new PrimaryTypeNewOperation("TestWithAnnotation", TypeUtility.getPackage(clientProject, "jdt.test.client.type.output"));
    typeOp.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createFormDataAnnotation());
    TestWorkspaceUtility.executeAndBuildWorkspace(typeOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertEquals(1, typeOp.getCreatedType().getAnnotations().length);
  }

  @Test
  public void testTypeWithField() throws Exception {
    IJavaProject clientProject = getClientJavaProject();
    Assert.assertTrue(TypeUtility.exists(clientProject));
    PrimaryTypeNewOperation typeOp = new PrimaryTypeNewOperation("TestWithField", TypeUtility.getPackage(clientProject, "jdt.test.client.type.output"));
    // field 01
    FieldSourceBuilder fieldSourceBuilder = new FieldSourceBuilder("field01");
    fieldSourceBuilder.setSignature(Signature.createTypeSignature(List.class.getName() + "<" + String.class.getName() + ">", true));
    typeOp.addFieldSourceBuilder(fieldSourceBuilder);
    // field02
    FieldSourceBuilder fieldSb02 = new FieldSourceBuilder("field02");
    fieldSb02.setSignature(Signature.createTypeSignature(String.class.getName(), true));
    fieldSb02.setFlags(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    fieldSb02.setValue("\"aText\"");
    typeOp.addFieldSourceBuilder(fieldSb02);
    TestWorkspaceUtility.executeAndBuildWorkspace(typeOp);
    TestWorkspaceUtility.assertNoCompileErrors();

    Assert.assertEquals(2, typeOp.getCreatedType().getFields().length);
    // check field 01
    IField field01 = typeOp.getCreatedType().getField("field01");
    Assert.assertTrue(TypeUtility.exists(field01));
    Assert.assertEquals("field01", field01.getElementName());
    Assert.assertFalse(Flags.isPublic(field01.getFlags()));
    Assert.assertFalse(Flags.isPrivate(field01.getFlags()));
    Assert.assertFalse(Flags.isProtected(field01.getFlags()));
    Assert.assertFalse(Flags.isStatic(field01.getFlags()));
    Assert.assertTrue(Flags.isPackageDefault(field01.getFlags()));
    Assert.assertFalse(Flags.isFinal(field01.getFlags()));
    Assert.assertNull(field01.getConstant());
    // check field 02
    IField field02 = typeOp.getCreatedType().getField("field02");
    Assert.assertTrue(TypeUtility.exists(field02));
    Assert.assertEquals("field02", field02.getElementName());
    Assert.assertTrue(Flags.isPublic(field02.getFlags()));
    Assert.assertFalse(Flags.isPrivate(field02.getFlags()));
    Assert.assertFalse(Flags.isProtected(field02.getFlags()));
    Assert.assertTrue(Flags.isStatic(field02.getFlags()));
    Assert.assertTrue(Flags.isFinal(field02.getFlags()));
    Assert.assertFalse(Flags.isPackageDefault(field02.getFlags()));
    Assert.assertEquals("\"aText\"", field02.getConstant());
  }

  @Test
  public void testTypeWithInterface() throws Exception {
    IJavaProject clientProject = getClientJavaProject();
    Assert.assertTrue(TypeUtility.exists(clientProject));
    PrimaryTypeNewOperation typeOp = new PrimaryTypeNewOperation("TypeWithInterface", TypeUtility.getPackage(clientProject, "jdt.test.client.type.output"));
    IType markerInterface = SdkAssert.assertTypeExists("jdt.test.client.type.IMarkerInterface");
    typeOp.addInterfaceSignature(Signature.createTypeSignature(markerInterface.getFullyQualifiedName(), true));
    TestWorkspaceUtility.executeAndBuildWorkspace(typeOp);
    TestWorkspaceUtility.assertNoCompileErrors();

    Assert.assertTrue(TypeUtility.exists(typeOp.getCreatedType()));
    String[] interfaceSignatures = typeOp.getCreatedType().getSuperInterfaceTypeSignatures();
    Assert.assertEquals(1, interfaceSignatures.length);
    Assert.assertEquals(Signature.createTypeSignature(markerInterface.getFullyQualifiedName(), true), SignatureUtility.getResolvedSignature(interfaceSignatures[0], typeOp.getCreatedType()));
  }

  @Test
  public void testTypeWithSuperClass() throws Exception {
    IJavaProject clientProject = getClientJavaProject();
    Assert.assertTrue(TypeUtility.exists(clientProject));
    PrimaryTypeNewOperation typeOp = new PrimaryTypeNewOperation("TypeWithSuperClass", TypeUtility.getPackage(clientProject, "jdt.test.client.type.output"));
    IType superClass = SdkAssert.assertTypeExists("jdt.test.client.type.AbstractType");
    typeOp.setSuperTypeSignature(Signature.createTypeSignature(superClass.getFullyQualifiedName(), true));
    TestWorkspaceUtility.executeAndBuildWorkspace(typeOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertTrue(TypeUtility.exists(typeOp.getCreatedType()));
    Assert.assertEquals(Signature.createTypeSignature(superClass.getFullyQualifiedName(), true), SignatureUtility.getResolvedSignature(typeOp.getCreatedType().getSuperclassTypeSignature(), typeOp.getCreatedType()));
  }

  @Test
  public void createInnerTypeTest() throws Exception {
    IType testType = createTestType("InnerTypeTest");
    InnerTypeNewOperation typeOp = new InnerTypeNewOperation("InnerType", testType);
    typeOp.setFlags(Flags.AccPublic);
    typeOp.setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
    TestWorkspaceUtility.executeAndBuildWorkspace(typeOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    IType createdType = typeOp.getCreatedType();
    Assert.assertTrue(TypeUtility.exists(createdType));
    Assert.assertEquals(createdType.getDeclaringType(), testType);
  }

  @Test
  public void createInnerTypeWithMethodTest() throws Exception {
    IType testType = createTestType("InnerTypeWithMethodTest");

    InnerTypeNewOperation typeOp = new InnerTypeNewOperation("InnerType", testType);
    typeOp.setFlags(Flags.AccPublic);
    typeOp.setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
    // Method
    MethodSourceBuilder methodOp = new MethodSourceBuilder("aMethod");
    methodOp.setFlags(Flags.AccPublic);
    methodOp.setReturnTypeSignature(Signature.SIG_VOID);
    methodOp.addParameter(new MethodParameter("aList", Signature.createTypeSignature(List.class.getName() + "<" + String.class.getName() + ">", true)));
    typeOp.addMethodSourceBuilder(methodOp);
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, typeOp);
    SdkAssert.assertExist(typeOp.getCreatedType());

    IType createdType = typeOp.getCreatedType();
    Assert.assertTrue(TypeUtility.exists(createdType));
    SdkAssert.assertMethodExist(createdType, "aMethod");

    SdkAssert.assertExist(TypeUtility.getMethod(createdType, "aMethod", new String[]{Signature.createTypeSignature(List.class.getName() + "<" + String.class.getName() + ">", true)}));
    SdkAssert.assertExist(createdType.getDeclaringType());
  }

  @Test
  public void testOrderedInnerTypeFirst() throws Exception {
    IType mainBox = SdkAssert.assertTypeExists("jdt.test.client.TestForm.MainBox");
    OrderedInnerTypeNewOperation innerTypeOp = new OrderedInnerTypeNewOperation("FirstField", mainBox);
    innerTypeOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.client.ui.form.fields.placeholder.AbstractPlaceholderField", true));
    innerTypeOp.setOrderDefinitionType(TypeUtility.getType(RuntimeClasses.IFormField));
    innerTypeOp.setSibling(TypeUtility.getType("jdt.test.client.TestForm.MainBox.StringField"));
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, innerTypeOp);
    IAnnotation annotation = innerTypeOp.getCreatedType().getAnnotation("Order");
    Assert.assertTrue(TypeUtility.exists(annotation));
    IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
    Assert.assertEquals(1, memberValuePairs.length);
    Assert.assertEquals(Double.valueOf(10), memberValuePairs[0].getValue());
    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(innerTypeOp.getCreatedType(), false);
    TestWorkspaceUtility.executeAndBuildWorkspace(delOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertFalse(TypeUtility.exists(innerTypeOp.getCreatedType()));
  }

  @Test
  public void testOrderedInnerTypeLast() throws Exception {
    IType mainBox = SdkAssert.assertTypeExists("jdt.test.client.TestForm.MainBox");
    OrderedInnerTypeNewOperation innerTypeOp = new OrderedInnerTypeNewOperation("LastField", mainBox);
    innerTypeOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.client.ui.form.fields.placeholder.AbstractPlaceholderField", true));
    innerTypeOp.setOrderDefinitionType(TypeUtility.getType(RuntimeClasses.IFormField));
    TestWorkspaceUtility.executeAndBuildWorkspace(innerTypeOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    IAnnotation annotation = innerTypeOp.getCreatedType().getAnnotation("Order");
    Assert.assertTrue(TypeUtility.exists(annotation));
    IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
    Assert.assertEquals(1, memberValuePairs.length);
    Assert.assertEquals(Double.valueOf(120), memberValuePairs[0].getValue());
    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(innerTypeOp.getCreatedType(), false);
    TestWorkspaceUtility.executeAndBuildWorkspace(delOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertFalse(TypeUtility.exists(innerTypeOp.getCreatedType()));
  }

  @Test
  public void testOrderedInnerTypeMiddle() throws Exception {
    IType mainBox = SdkAssert.assertTypeExists("jdt.test.client.TestForm.MainBox");
    OrderedInnerTypeNewOperation innerTypeOp = new OrderedInnerTypeNewOperation("MiddleField", mainBox);
    innerTypeOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.client.ui.form.fields.placeholder.AbstractPlaceholderField", true));
    innerTypeOp.setOrderDefinitionType(TypeUtility.getType(RuntimeClasses.IFormField));
    innerTypeOp.setSibling(TypeUtility.getType("jdt.test.client.TestForm.MainBox.GroupBox"));
    TestWorkspaceUtility.executeAndBuildWorkspace(innerTypeOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    IAnnotation annotation = innerTypeOp.getCreatedType().getAnnotation("Order");
    Assert.assertTrue(TypeUtility.exists(annotation));
    IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
    Assert.assertEquals(1, memberValuePairs.length);
    Assert.assertEquals(Double.valueOf(40), memberValuePairs[0].getValue());
    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(innerTypeOp.getCreatedType(), false);
    TestWorkspaceUtility.executeAndBuildWorkspace(delOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertFalse(TypeUtility.exists(innerTypeOp.getCreatedType()));
  }

  @Test
  public void testOrderedInnerTypeWithGeneric() throws Exception {
    IType mainBox = SdkAssert.assertTypeExists("jdt.test.client.TestForm.MainBox");
    OrderedInnerTypeNewOperation innerTypeOp = new OrderedInnerTypeNewOperation("ASmartField", mainBox);
    innerTypeOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.client.ui.form.fields.placeholder.AbstractPlaceholderField", true));
    innerTypeOp.setOrderDefinitionType(TypeUtility.getType(RuntimeClasses.IFormField));
    innerTypeOp.setSibling(TypeUtility.getType("jdt.test.client.TestForm.MainBox.GroupBox"));
    TestWorkspaceUtility.executeAndBuildWorkspace(innerTypeOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    IAnnotation annotation = innerTypeOp.getCreatedType().getAnnotation("Order");
    Assert.assertTrue(TypeUtility.exists(annotation));
    IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
    Assert.assertEquals(1, memberValuePairs.length);
    Assert.assertEquals(Double.valueOf(40), memberValuePairs[0].getValue());
    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(innerTypeOp.getCreatedType(), false);
    TestWorkspaceUtility.executeAndBuildWorkspace(delOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    Assert.assertFalse(TypeUtility.exists(innerTypeOp.getCreatedType()));
  }

//
  private IType createTestType(String typeName) throws Exception {
    IJavaProject clientProject = getClientJavaProject();
    Assert.assertTrue(TypeUtility.exists(clientProject));
    PrimaryTypeNewOperation typeOp = new PrimaryTypeNewOperation(typeName, TypeUtility.getPackage(clientProject, "jdt.test.client.type.output"));
    typeOp.setFlags(Flags.AccPublic);
    typeOp.setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
    TestWorkspaceUtility.executeAndBuildWorkspace(typeOp);
    TestWorkspaceUtility.assertNoCompileErrors();
    return typeOp.getCreatedType();
  }

}
