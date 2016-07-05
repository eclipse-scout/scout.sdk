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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.ImportTestClass;
import org.eclipse.scout.sdk.core.importcollector.IImportCollector;
import org.eclipse.scout.sdk.core.importcollector.ImportCollector;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitScopedImportCollector;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.EnclosingTypeScopedImportCollector;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ImportValidatorTest {

  @Test
  public void testJavaLangPackage() {
    IImportCollector iv = createImportCollector("test");
    IImportValidator validator = new ImportValidator(iv);

    String longName = validator.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG);
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String longName2 = validator.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG);
    Assert.assertEquals(Long.class.getSimpleName(), longName2);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = validator.useName(ownClassFqn);
    Assert.assertEquals("MyClass", ownName);

    Collection<String> importsToCreate = iv.createImportDeclarations();
    Assert.assertEquals(Arrays.asList("import " + ownClassFqn + ";"), importsToCreate);
  }

  @Test
  public void testTypeArgToTypeInSamePackage() {
    CompilationUnitSourceBuilder cu = new CompilationUnitSourceBuilder("MyClass.java", "test");
    ITypeSourceBuilder t = new TypeSourceBuilder("MyClass");
    t.setSuperTypeSignature(Signature.createTypeSignature("a.b.SuperClass<test.External>"));
    cu.addType(t);

    IImportValidator validator = new ImportValidator(new ImportCollector((IJavaEnvironment) null));
    StringBuilder sourceBuilder = new StringBuilder();
    cu.createSource(sourceBuilder, "\n", null, validator);

    Collection<String> imports = validator.getImportCollector().getImports();
    Assert.assertEquals(2, imports.size());
    Assert.assertTrue(imports.contains("test.MyClass"));
    Assert.assertTrue(imports.contains("a.b.SuperClass"));
  }

  @Test
  public void testTypeArgToInnerType() {
    CompilationUnitSourceBuilder cu = new CompilationUnitSourceBuilder("MyClass.java", "test");
    ITypeSourceBuilder t = new TypeSourceBuilder("MyClass");
    t.setSuperTypeSignature(Signature.createTypeSignature("a.b.SuperClass<test.MyClass.Inner>"));
    cu.addType(t);

    IImportValidator validator = new ImportValidator(new ImportCollector((IJavaEnvironment) null));
    StringBuilder sourceBuilder = new StringBuilder();
    cu.createSource(sourceBuilder, "\n", null, validator);

    Collection<String> imports = validator.getImportCollector().getImports();
    Assert.assertEquals(3, imports.size());
    Assert.assertTrue(imports.contains("test.MyClass.Inner"));
    Assert.assertTrue(imports.contains("test.MyClass"));
    Assert.assertTrue(imports.contains("a.b.SuperClass"));
  }

  @Test
  public void testOwnPackage() {
    IImportCollector iv = createImportCollector("test.own.pck");
    IImportValidator validator = new ImportValidator(iv);

    String longName = validator.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG);
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = validator.useName(ownClassFqn);
    Assert.assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass2";
    String ownName2 = validator.useName(ownClassFqn2);
    Assert.assertEquals("MyClass2", ownName2);

    Collection<String> importsToCreate = iv.createImportDeclarations();
    Assert.assertEquals(Arrays.asList("import " + ownClassFqn + ";"), importsToCreate);
  }

  @Test
  public void testPrimitives() {
    IImportCollector iv = createImportCollector("test");
    IImportValidator validator = new ImportValidator(iv);

    String intName = validator.useSignature(ISignatureConstants.SIG_INT);
    Assert.assertEquals(Signature.getSignatureSimpleName(ISignatureConstants.SIG_INT), intName);

    Collection<String> importsToCreate = iv.createImportDeclarations();
    Assert.assertEquals(new ArrayList<String>(), importsToCreate);
  }

  @Test
  public void testNullPackage() {
    IImportCollector iv = createImportCollector((String) null);
    IImportValidator validator = new ImportValidator(iv);

    String longName = validator.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG);
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = validator.useName(ownClassFqn);
    Assert.assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass2";
    String ownName2 = validator.useName(ownClassFqn2);
    Assert.assertEquals("MyClass2", ownName2);

    Collection<String> importsToCreate = iv.createImportDeclarations();
    Assert.assertEquals(Arrays.asList("import " + ownClassFqn + ";", "import " + ownClassFqn2 + ";"), importsToCreate);
  }

  @Test
  public void testQualifiedPackage() {
    IImportCollector iv = createImportCollector("test");
    IImportValidator validator = new ImportValidator(iv);

    String longName = validator.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG);
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = validator.useName(ownClassFqn);
    Assert.assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass";
    String ownName2 = validator.useName(ownClassFqn2);
    Assert.assertEquals(ownClassFqn2, ownName2);

    Collection<String> importsToCreate = iv.createImportDeclarations();
    Assert.assertEquals(Arrays.asList("import " + ownClassFqn + ";"), importsToCreate);
  }

  @Test
  public void testWithInnerClasses() {
    String sig = "La.b.c.MyClass$InnerClass$SecondInner;";
    IImportCollector iv = createImportCollector("a.b.c");
    IImportValidator validator = new ImportValidator(iv);

    Assert.assertEquals("SecondInner", validator.useSignature(sig));
    Assert.assertEquals("a.b.c.SecondInner", validator.useName("a.b.c.SecondInner"));
  }

  @Test
  public void testIcuWithInnerClassesThatAlsoExistInOwnPackage() {
    IType importTest = CoreTestingUtils.createJavaEnvironment().findType(ImportTestClass.class.getName());
    IImportCollector iv = createImportValidator(importTest);
    IImportValidator validator = new ImportValidator(iv);

    Assert.assertEquals(ImportTestClass.Long.class.getSimpleName(), validator.useName(importTest.innerTypes().first().name()));
    Assert.assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), validator.useName(org.eclipse.scout.sdk.core.fixture.Long.class.getName()));
  }

  @Test
  public void testWildcardSignature() {
    IImportCollector iv = createImportCollector("a.b.c");
    IImportValidator validator = new ImportValidator(iv);
    String ref = validator.useName("? extends java.lang.Long");
    String ref2 = validator.useName("? extends java.test.Long");
    String ref3 = validator.useName("?");
    Assert.assertEquals("? extends Long", ref);
    Assert.assertEquals("? extends java.test.Long", ref2);
    Assert.assertEquals("?", ref3);
  }

  @Test
  public void testTypeVariableSignature() {
    IImportCollector iv = createImportCollector("a.b.c");
    IImportValidator validator = new ImportValidator(iv);
    String typeParameterSignature1 = Signature.createTypeParameterSignature("TYPE_PARAM_NAME", new String[]{
        Signature.createTypeSignature(AbstractList.class.getName()),
        Signature.createTypeSignature(RandomAccess.class.getName()),
        Signature.createTypeSignature(Serializable.class.getName())});
    String typeParameterSignature2 = Signature.createTypeParameterSignature("TYPE_PARAM_NAME", new String[]{
        Signature.createTypeSignature("java.test.RandomAccess")});
    String typeParameterSignature3 = Signature.createTypeParameterSignature("TYPE_PARAM_NAME", new String[]{});
    String typeParameterSignature4 = Signature.createTypeParameterSignature("TYPE_PARAM_NAME", new String[]{
        Signature.createTypeSignature(AbstractList.class.getName()),
        Signature.createTypeSignature(RandomAccess.class.getName())});

    String ref1 = validator.useSignature(typeParameterSignature1);
    Assert.assertEquals("TYPE_PARAM_NAME extends AbstractList & RandomAccess & Serializable", ref1);
    String ref2 = validator.useSignature(typeParameterSignature2);
    Assert.assertEquals("TYPE_PARAM_NAME extends java.test.RandomAccess", ref2);
    String ref3 = validator.useSignature(typeParameterSignature3);
    Assert.assertEquals("TYPE_PARAM_NAME", ref3);
    String ref4 = validator.useSignature(typeParameterSignature4);
    Assert.assertEquals("TYPE_PARAM_NAME extends AbstractList & RandomAccess", ref4);
  }

  @Test
  public void testWithInnerClassOfPrimaryTypeHavingTypeArgs() {
    IImportCollector iv = createImportCollector("a.b.c");
    IImportValidator validator = new ImportValidator(iv);
    String ref = validator.useName("d.e.f.MyClassOne<java.lang.Long>.InnerClass.SecondInner");
    Assert.assertEquals("MyClassOne<Long>.InnerClass.SecondInner", ref);

    ref = validator.useName("d.e.f.MyClassTwo<java.lang.Long>.InnerClass<java.lang.Boolean>.SecondInner<java.util.Map<java.lang.Long, java.lang.String>>.ThirdInner");
    Assert.assertEquals("MyClassTwo<Long>.InnerClass<Boolean>.SecondInner<Map<Long, String>>.ThirdInner", ref);

    ref = validator.useName("d.e.f.TopLevel.MyClassThree<java.lang.Long>.InnerClass<java.lang.Boolean>.SecondInner<java.util.Map<java.lang.Long, java.lang.String>>.ThirdInner");
    Assert.assertEquals("MyClassThree<Long>.InnerClass<Boolean>.SecondInner<Map<Long, String>>.ThirdInner", ref);

    ref = validator.useName("d.e.f.TopLevelAnother.MyClassThree<java.lang.Long>.InnerClass<org.test.Boolean>.SecondInner<java.util.Map<java.lang.Long[][][], java.lang.String[]>>.ThirdInner");
    Assert.assertEquals("d.e.f.TopLevelAnother.MyClassThree<Long>.InnerClass<org.test.Boolean>.SecondInner<Map<Long[][][], String[]>>.ThirdInner", ref);

    Collection<String> importsToCreate = iv.getImports();
    Assert.assertEquals(7, importsToCreate.size());
    Assert.assertTrue(importsToCreate.contains("d.e.f.MyClassOne"));
    Assert.assertTrue(importsToCreate.contains("java.lang.Long"));
    Assert.assertTrue(importsToCreate.contains("d.e.f.MyClassTwo"));
    Assert.assertTrue(importsToCreate.contains("java.lang.Boolean"));
    Assert.assertTrue(importsToCreate.contains("java.util.Map"));
    Assert.assertTrue(importsToCreate.contains("java.lang.String"));
    Assert.assertTrue(importsToCreate.contains("d.e.f.TopLevel.MyClassThree"));
    Assert.assertFalse(importsToCreate.contains("d.e.f.TopLevelAnother.MyClassThree")); // d.e.f.TopLevelAnother.MyClassThree is not part of the imports becuase it would be fully qualified!
    Assert.assertFalse(importsToCreate.contains("org.test.Boolean")); // org.test.Boolean is not part of the list because it would be fully qualified!
  }

  @Test
  public void testGetSegments() {
    String[] origSegments = new String[]{"d", "e", "f", "MyClass<java.lang.Long[]>", "InnerClass<java.lang.Boolean>", "SecondInner<java.util.Map<java.lang.Long, java.lang.String[][][]>[]>", "ThirdInner"};
    StringBuilder sigBuilder = new StringBuilder();
    for (String s : origSegments) {
      sigBuilder.append(s).append('.');
    }
    sigBuilder.delete(sigBuilder.length() - 1, sigBuilder.length());
    List<String> segments = ImportValidator.getSegments(sigBuilder.toString());
    Assert.assertArrayEquals(origSegments, segments.toArray());
  }

  @Test
  public void testWithDuplicateInOwnPackage() {
    IImportCollector iv = createImportValidator(CoreTestingUtils.getBaseClassIcu());
    IImportValidator validator = new ImportValidator(iv);

    // long on foreign package
    String longName = validator.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG);
    Assert.assertEquals(IJavaRuntimeTypes.Long, longName);

    longName = validator.useSignature(ISignatureConstants.SIG_JAVA_LANG_LONG);
    Assert.assertEquals(IJavaRuntimeTypes.Long, longName);

    // long in own package
    longName = validator.useName(org.eclipse.scout.sdk.core.fixture.Long.class.getName());
    Assert.assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getSimpleName(), longName);

    // own class in own package
    String baseClassName = validator.useName(BaseClass.class.getName());
    Assert.assertEquals(BaseClass.class.getSimpleName(), baseClassName);

    // other class in own package
    String childClassName = validator.useName(ChildClass.class.getName());
    Assert.assertEquals(ChildClass.class.getSimpleName(), childClassName);
  }

  static IImportCollector createImportCollector(String packageName) {
    return new CompilationUnitScopedImportCollector(new ImportCollector(), packageName);
  }

  static IImportCollector createImportValidator(ICompilationUnit cu) {
    CompilationUnitSourceBuilder cuSrc = new CompilationUnitSourceBuilder(cu);
    IImportCollector validator0 = new ImportCollector(cu.javaEnvironment());
    for (String s : cuSrc.getDeclaredImports()) {
      validator0.addImport(s);
    }
    IImportCollector validator1 = new CompilationUnitScopedImportCollector(validator0, cuSrc.getPackageName());
    return validator1;
  }

  static IImportCollector createImportValidator(IType t) {
    CompilationUnitSourceBuilder cuSrc = new CompilationUnitSourceBuilder(t.compilationUnit());
    IImportCollector validator0 = new ImportCollector(t.javaEnvironment());
    for (String s : cuSrc.getDeclaredImports()) {
      validator0.addImport(s);
    }
    IImportCollector validator1 = new CompilationUnitScopedImportCollector(validator0, cuSrc.getPackageName());
    IImportCollector validator2 = new EnclosingTypeScopedImportCollector(validator1, cuSrc.getMainType());
    return validator2;
  }

}
