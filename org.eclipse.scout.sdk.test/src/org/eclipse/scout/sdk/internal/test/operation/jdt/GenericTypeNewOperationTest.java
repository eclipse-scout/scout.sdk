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
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithJdtTestProject;
import org.eclipse.scout.sdk.operation.jdt.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link GenericTypeNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 08.02.2013
 */
public class GenericTypeNewOperationTest extends AbstractSdkTestWithJdtTestProject {

  /**
   * Step1: Create a primary type with a generic super type.
   * Step2: Override a method from generic super type having the generic parameter as a return value.
   * 
   * @throws Exception
   */
  @Test
  public void testGenericSuperClass() throws Exception {
    PrimaryTypeNewOperation typeop = new PrimaryTypeNewOperation("Type01", "jdt.test.client.type.output", getClientJavaProject());
    typeop.setFlags(Flags.AccPublic);
    StringBuilder fqSuperTypeBuilder = new StringBuilder("jdt.test.client.type.Hierarchy01<");
    fqSuperTypeBuilder.append(List.class.getName()).append("<").append(File.class.getName()).append(">>");
    typeop.setSuperTypeSignature(Signature.createTypeSignature(fqSuperTypeBuilder.toString(), true));
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, typeop);
    Assert.assertTrue(TypeUtility.exists(typeop.getCreatedType()));

    // create method
    MethodOverrideOperation overrideOp = new MethodOverrideOperation("getValue", typeop.getCreatedType());
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, overrideOp);
    Assert.assertTrue(TypeUtility.exists(overrideOp.getCreatedMethod()));
    Assert.assertEquals("QList<QFile;>;", overrideOp.getCreatedMethod().getReturnType());
  }

  /**
   * Step1: Create a primary type with a generic super type. The generic super type has itself a generic super type with
   * an other generic parameter type.
   * Step2: Override a method from generic super type having the generic parameter as a return value.
   * 
   * @throws Exception
   */
  @Test
  public void testGenericSuperClass2() throws Exception {
    PrimaryTypeNewOperation typeop = new PrimaryTypeNewOperation("Type02", "jdt.test.client.type.output", getClientJavaProject());
    typeop.setFlags(Flags.AccPublic);
    StringBuilder fqSuperTypeBuilder = new StringBuilder("jdt.test.client.type.Hierarchy02<");
    fqSuperTypeBuilder.append(List.class.getName()).append("<").append(File.class.getName()).append(">>");
    typeop.setSuperTypeSignature(Signature.createTypeSignature(fqSuperTypeBuilder.toString(), true));
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, typeop);
    Assert.assertTrue(TypeUtility.exists(typeop.getCreatedType()));

    // create method
    MethodOverrideOperation overrideOp = new MethodOverrideOperation("getValue", typeop.getCreatedType());
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, overrideOp);
    Assert.assertTrue(TypeUtility.exists(overrideOp.getCreatedMethod()));
    Assert.assertEquals("QList<QFile;>;", overrideOp.getCreatedMethod().getReturnType());
  }

  /**
   * <pre>
   *  Hierarchy01<T>
   *   Hierarchy02<F> extends Hierarchy01<F>
   *    Hierarchy03 extends Hierarchy02<String>
   * </pre>
   * 
   * Step1: Create a primary type with Hierarchy03 as super type.
   * Step2: Override a method from generic super type having the generic parameter as a return value.
   * 
   * @throws Exception
   */
  @Test
  public void testGenericSuperClass3() throws Exception {
    PrimaryTypeNewOperation typeop = new PrimaryTypeNewOperation("Type03", "jdt.test.client.type.output", getClientJavaProject());
    typeop.setFlags(Flags.AccPublic);
    StringBuilder fqSuperTypeBuilder = new StringBuilder("jdt.test.client.type.Hierarchy03");
    typeop.setSuperTypeSignature(Signature.createTypeSignature(fqSuperTypeBuilder.toString(), true));
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, typeop);
    Assert.assertTrue(TypeUtility.exists(typeop.getCreatedType()));

    // create method
    MethodOverrideOperation overrideOp = new MethodOverrideOperation("getValue", typeop.getCreatedType());

    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, overrideOp);
    Assert.assertTrue(TypeUtility.exists(overrideOp.getCreatedMethod()));
    Assert.assertEquals("QString;", overrideOp.getCreatedMethod().getReturnType());
  }

  /**
   * Step1: Create a primary type with a generic super type.
   * Step2: Override a method from generic super type having the generic parameter as a return value with a source
   * builder.
   * 
   * @throws Exception
   */
  @Test
  public void testGenericSuperClass04() throws Exception {
    PrimaryTypeNewOperation typeop = new PrimaryTypeNewOperation("Type04", "jdt.test.client.type.output", getClientJavaProject());
    typeop.setFlags(Flags.AccPublic);
    String superTypeFqn = "jdt.test.client.type.Hierarchy01";
    String genericTypeFqn = List.class.getName() + "<" + File.class.getName() + ">";
    StringBuilder fqSuperTypeBuilder = new StringBuilder(superTypeFqn).append("<").append(genericTypeFqn).append(">");
    typeop.setSuperTypeSignature(Signature.createTypeSignature(fqSuperTypeBuilder.toString(), true));

    // method source builder
    String methodName = "getValue";
    IMethodSourceBuilder methodSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(methodName, TypeUtility.getType(superTypeFqn));
    methodSourceBuilder.setReturnTypeSignature(Signature.createTypeSignature(genericTypeFqn, true));
    typeop.addMethodSourceBuilder(methodSourceBuilder);

    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, typeop);
    IType type04 = typeop.getCreatedType();
    Assert.assertTrue(TypeUtility.exists(type04));

    IMethod method = TypeUtility.getMethod(type04, methodName);
    Assert.assertTrue(TypeUtility.exists(method));
    Assert.assertEquals("QList<QFile;>;", method.getReturnType());
  }
}
