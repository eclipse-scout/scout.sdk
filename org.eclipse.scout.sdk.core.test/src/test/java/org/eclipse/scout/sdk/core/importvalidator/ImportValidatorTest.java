/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.importvalidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.scout.sdk.core.TypeNames;
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.ImportTestClass;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitScopedImportValidator;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.EnclosingTypeScopedImportValidator;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ImportValidatorTest {

  @Test
  public void testJavaLangPackage() {
    IImportValidator iv = createImportValidator("test");
    String longName = SignatureUtils.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG, iv);
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String longName2 = SignatureUtils.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG, iv);
    Assert.assertEquals(Long.class.getSimpleName(), longName2);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = SignatureUtils.useName(ownClassFqn, iv);
    Assert.assertEquals("MyClass", ownName);

    Collection<String> importsToCreate = iv.createImportDeclarations();
    Assert.assertEquals(Arrays.asList("import " + ownClassFqn + ";"), importsToCreate);
  }

  @Test
  public void testOwnPackage() {
    IImportValidator iv = createImportValidator("test.own.pck");

    String longName = SignatureUtils.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG, iv);
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = SignatureUtils.useName(ownClassFqn, iv);
    Assert.assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass2";
    String ownName2 = SignatureUtils.useName(ownClassFqn2, iv);
    Assert.assertEquals("MyClass2", ownName2);

    Collection<String> importsToCreate = iv.createImportDeclarations();
    Assert.assertEquals(Arrays.asList("import " + ownClassFqn + ";"), importsToCreate);
  }

  @Test
  public void testPrimitives() {
    IImportValidator iv = createImportValidator("test");

    String intName = SignatureUtils.useSignature(ISignatureConstants.SIG_INT, iv);
    Assert.assertEquals(Signature.getSignatureSimpleName(ISignatureConstants.SIG_INT), intName);

    Collection<String> importsToCreate = iv.createImportDeclarations();
    Assert.assertEquals(new ArrayList<String>(), importsToCreate);
  }

  @Test
  public void testNullPackage() {
    IImportValidator iv = createImportValidator((String) null);

    String longName = SignatureUtils.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG, iv);
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = SignatureUtils.useName(ownClassFqn, iv);
    Assert.assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass2";
    String ownName2 = SignatureUtils.useName(ownClassFqn2, iv);
    Assert.assertEquals("MyClass2", ownName2);

    Collection<String> importsToCreate = iv.createImportDeclarations();
    Assert.assertEquals(Arrays.asList("import " + ownClassFqn + ";", "import " + ownClassFqn2 + ";"), importsToCreate);
  }

  @Test
  public void testQualifiedPackage() {
    IImportValidator iv = createImportValidator("test");

    String longName = SignatureUtils.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG, iv);
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = SignatureUtils.useName(ownClassFqn, iv);
    Assert.assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass";
    String ownName2 = SignatureUtils.useName(ownClassFqn2, iv);
    Assert.assertEquals(ownClassFqn2, ownName2);

    Collection<String> importsToCreate = iv.createImportDeclarations();
    Assert.assertEquals(Arrays.asList("import " + ownClassFqn + ";"), importsToCreate);
  }

  @Test
  public void testWithInnerClasses() {
    String sig = "La.b.c.MyClass$InnerClass$SecondInner;";
    IImportValidator iv = createImportValidator("a.b.c");
    Assert.assertEquals("SecondInner", SignatureUtils.useSignature(sig, iv));
    Assert.assertEquals("a.b.c.SecondInner", SignatureUtils.useName("a.b.c.SecondInner", iv));
  }

  @Test
  public void testIcuWithInnerClassesThatAlsoExistInOwnPackage() {
    IType importTest = CoreTestingUtils.createJavaEnvironment().findType(ImportTestClass.class.getName());
    IImportValidator iv = createImportValidator(importTest);

    Assert.assertEquals(ImportTestClass.Long.class.getSimpleName(), SignatureUtils.useName(importTest.getTypes().get(0).getName(), iv));
    Assert.assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), SignatureUtils.useName(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), iv));
  }

  @Test
  public void testWithDuplicateInOwnPackage() {
    IImportValidator iv = createImportValidator(CoreTestingUtils.getBaseClassIcu());

    // long on foreign package
    String longName = SignatureUtils.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG, iv);
    Assert.assertEquals(TypeNames.java_lang_Long, longName);

    longName = SignatureUtils.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG, iv);
    Assert.assertEquals(TypeNames.java_lang_Long, longName);

    // long in own package
    longName = SignatureUtils.useName(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), iv);
    Assert.assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getSimpleName(), longName);

    // own class in own package
    String baseClassName = SignatureUtils.useName(BaseClass.class.getName(), iv);
    Assert.assertEquals(BaseClass.class.getSimpleName(), baseClassName);

    // other class in own package
    String childClassName = SignatureUtils.useName(ChildClass.class.getName(), iv);
    Assert.assertEquals(ChildClass.class.getSimpleName(), childClassName);
  }

  static IImportValidator createImportValidator(String packageName) {
    return new CompilationUnitScopedImportValidator(new ImportValidator(), packageName);
  }

  static IImportValidator createImportValidator(ICompilationUnit cu) {
    CompilationUnitSourceBuilder cuSrc = new CompilationUnitSourceBuilder(cu);
    IImportValidator validator0 = new ImportValidator(cu.getJavaEnvironment());
    for (String s : cuSrc.getDeclaredImports()) {
      validator0.addImport(s);
    }
    IImportValidator validator1 = new CompilationUnitScopedImportValidator(validator0, cuSrc.getPackageName());
    return validator1;
  }

  static IImportValidator createImportValidator(IType t) {
    CompilationUnitSourceBuilder cuSrc = new CompilationUnitSourceBuilder(t.getCompilationUnit());
    IImportValidator validator0 = new ImportValidator(t.getJavaEnvironment());
    for (String s : cuSrc.getDeclaredImports()) {
      validator0.addImport(s);
    }
    IImportValidator validator1 = new CompilationUnitScopedImportValidator(validator0, cuSrc.getPackageName());
    IImportValidator validator2 = new EnclosingTypeScopedImportValidator(validator1, cuSrc.getMainType());
    return validator2;
  }

}
