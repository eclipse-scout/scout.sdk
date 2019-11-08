/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.imports;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.JavaSourceBuilder;
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.ImportTestClass;
import org.eclipse.scout.sdk.core.fixture.sub.ImportTestClass2;
import org.eclipse.scout.sdk.core.generator.compilationunit.CompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
public class ImportValidatorTest {

  @Test
  public void testImportGrouping() {
    IImportCollector iv = createCompilationUnitImportCollector("com.orig.pck");
    iv.addImport("org.test.bla.Clazz1");
    iv.addImport("org.test.bla.Clazz2");
    iv.addImport("net.application.whatever.Clazz4");
    iv.addImport("net.application.whatever.Clazz5");
    iv.addImport("net.application.whatever.Clazz6");
    iv.addImport("javax.test.Clazz7");
    iv.addStaticImport("org.test.bla.Clazz1.myMethod");

    List<String> importsToCreate = toStringList(iv.createImportDeclarations());

    assertEquals(Arrays.asList("import static org.test.bla.Clazz1.myMethod;", "",
        "import javax.test.Clazz7;", "",
        "import org.test.bla.Clazz1;", "import org.test.bla.Clazz2;", "",
        "import net.application.whatever.Clazz4;", "import net.application.whatever.Clazz5;", "import net.application.whatever.Clazz6;"), importsToCreate);
  }

  @Test
  public void testComplexWithDollar() {
    ImportCollector collector = new ImportCollector();
    String result =
        new ImportValidator(collector).useReference("d.e.f.TopLevelAnother$MyClassThree< java.lang.Long >$InnerClass<org.test.Boolean>$SecondInner<java.util.Map<java.lang.Long[][][], java.test.Whatever$Other[]>>$ThirdInner");
    assertEquals("MyClassThree<Long>$InnerClass<Boolean>$SecondInner<Map<Long[][][],Other[]>>$ThirdInner", result);

    List<String> collectedImports = collector.getImports()
        .map(StringBuilder::toString)
        .collect(toList());

    assertEquals(5, collectedImports.size());
    assertEquals("java.lang.Long", collectedImports.get(0));
    assertEquals("d.e.f.TopLevelAnother.MyClassThree", collectedImports.get(1));
    assertEquals("org.test.Boolean", collectedImports.get(2));
    assertEquals("java.util.Map", collectedImports.get(3));
    assertEquals("java.test.Whatever.Other", collectedImports.get(4));
  }

  @Test
  public void testJavaLangPackage() {
    IImportCollector iv = createCompilationUnitImportCollector("test");
    IImportValidator validator = new ImportValidator(iv);

    String longName = validator.useReference(JavaTypes.Long);
    assertEquals(Long.class.getSimpleName(), longName);

    String longName2 = validator.useReference(JavaTypes.Long);
    assertEquals(Long.class.getSimpleName(), longName2);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = validator.useReference(ownClassFqn);
    assertEquals("MyClass", ownName);

    Collection<String> importsToCreate = toStringList(iv.createImportDeclarations());
    assertEquals(singletonList("import " + ownClassFqn + ';'), importsToCreate);
  }

  @Test
  public void testTypeArgToTypeInSamePackage() {
    ICompilationUnitGenerator<?> cu = CompilationUnitGenerator.create()
        .withElementName("MyClass.java")
        .withPackageName("test")
        .withType(TypeGenerator.create()
            .withElementName("MyClass")
            .withSuperClass("a.b.SuperClass<test.External>"));

    IJavaSourceBuilder<?> jsb = JavaSourceBuilder.create(new MemorySourceBuilder());
    cu.generate(jsb);

    Collection<String> imports = toStringList(jsb.context().validator().importCollector().getImports());
    assertEquals(2, imports.size());
    assertTrue(imports.contains("test.MyClass"));
    assertTrue(imports.contains("a.b.SuperClass"));
  }

  @Test
  public void testTypeArgToInnerType() {
    ICompilationUnitGenerator<?> cu = CompilationUnitGenerator.create()
        .withElementName("MyClass.java")
        .withPackageName("test")
        .withType(TypeGenerator.create()
            .withElementName("MyClass")
            .withSuperClass("a.b.SuperClass<test.MyClass.Inner>"));

    IJavaSourceBuilder<?> jsb = JavaSourceBuilder.create(new MemorySourceBuilder());
    cu.generate(jsb);

    Collection<String> imports = toStringList(jsb.context().validator().importCollector().getImports());
    assertEquals(3, imports.size());
    assertTrue(imports.contains("test.MyClass.Inner"));
    assertTrue(imports.contains("test.MyClass"));
    assertTrue(imports.contains("a.b.SuperClass"));
  }

  @Test
  public void testOwnPackage() {
    IImportCollector iv = createCompilationUnitImportCollector("test.own.pck");
    IImportValidator validator = new ImportValidator(iv);

    String longName = validator.useReference(JavaTypes.Long);
    assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = validator.useReference(ownClassFqn);
    assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass2";
    String ownName2 = validator.useReference(ownClassFqn2);
    assertEquals("MyClass2", ownName2);

    Collection<String> importsToCreate = toStringList(iv.createImportDeclarations());
    assertEquals(singletonList("import " + ownClassFqn + ';'), importsToCreate);
  }

  @Test
  public void testPrimitives() {
    IImportCollector iv = createCompilationUnitImportCollector("test");
    IImportValidator validator = new ImportValidator(iv);

    String intName = validator.useReference(JavaTypes._int);
    assertEquals(JavaTypes._int, intName);

    Collection<String> importsToCreate = toStringList(iv.createImportDeclarations());
    assertEquals(new ArrayList<String>(), importsToCreate);
  }

  @Test
  public void testNullPackage() {
    IImportCollector iv = createCompilationUnitImportCollector((String) null);
    IImportValidator validator = new ImportValidator(iv);

    String longName = validator.useReference(JavaTypes.Long);
    assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = validator.useReference(ownClassFqn);
    assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass2";
    String ownName2 = validator.useReference(ownClassFqn2);
    assertEquals("MyClass2", ownName2);

    Collection<String> importsToCreate = toStringList(iv.createImportDeclarations());
    assertEquals(Arrays.asList("import " + ownClassFqn + ';', "import " + ownClassFqn2 + ';'), importsToCreate);
  }

  @Test
  public void testQualifiedPackage() {
    IImportCollector iv = createCompilationUnitImportCollector("test");
    IImportValidator validator = new ImportValidator(iv);

    String longName = validator.useReference(JavaTypes.Long);
    assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = validator.useReference(ownClassFqn);
    assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass";
    String ownName2 = validator.useReference(ownClassFqn2);
    assertEquals(ownClassFqn2, ownName2);

    Collection<String> importsToCreate = toStringList(iv.createImportDeclarations());
    assertEquals(singletonList("import " + ownClassFqn + ';'), importsToCreate);
  }

  @Test
  public void testWithInnerClasses() {
    String name = "a.b.c.MyClass$InnerClass$SecondInner";
    IImportCollector iv = createCompilationUnitImportCollector("a.b.c");
    IImportValidator validator = new ImportValidator(iv);
    assertEquals("SecondInner", validator.useReference(name));
    assertEquals("a.b.c.SecondInner", validator.useReference("a.b.c.SecondInner"));
  }

  @Test
  @ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
  public void testIcuWithInnerClassesThatAlsoExistInOwnPackage(IJavaEnvironment env) {
    IType importTest = env.requireType(ImportTestClass.class.getName());
    IImportCollector iv = createEnclosingTypeImportCollector(importTest);
    IImportValidator validator = new ImportValidator(iv);

    assertEquals(ImportTestClass.Long.class.getSimpleName(), validator.useReference(importTest.innerTypes().first().get().name()));
    assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), validator.useReference(org.eclipse.scout.sdk.core.fixture.Long.class.getName()));
  }

  @Test
  @ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
  public void testResolveSuperClassInnerNameWhileExistingInOwnClassToo(IJavaEnvironment env) {
    IType importTest = env.requireType(ImportTestClass2.class.getName());
    IImportCollector iv = createEnclosingTypeImportCollector(importTest);
    IImportValidator validator = new ImportValidator(iv);

    // must be qualified because the same name also exists in the sub class itself (and the parent class).
    assertEquals(ImportTestClass.Long.class.getName().replace('$', '.'), validator.useReference(ImportTestClass.Long.class.getName()));
  }

  @Test
  public void testWildcardComplex() {
    IImportCollector iv = createCompilationUnitImportCollector("a.b.c");
    IImportValidator validator = new ImportValidator(iv);
    String ref = validator.useReference("java.util.Map$Entry<? extends java.io.Serializable, java.util.Set<? extends org.eclipse.scout.Test$Inner>>");
    assertEquals("Entry<? extends Serializable,Set<? extends Inner>>", ref);

    List<String> collectedImports = iv.getImports()
        .map(StringBuilder::toString)
        .collect(toList());

    assertEquals(4, collectedImports.size());
    assertTrue(collectedImports.contains("java.util.Map.Entry"));
    assertTrue(collectedImports.contains("java.io.Serializable"));
    assertTrue(collectedImports.contains("java.util.Set"));
    assertTrue(collectedImports.contains("org.eclipse.scout.Test.Inner"));
  }

  @Test
  public void testWildcard() {
    IImportCollector iv = createCompilationUnitImportCollector("a.b.c");
    IImportValidator validator = new ImportValidator(iv);
    String ref = validator.useReference("? extends java.lang.Long");
    String ref2 = validator.useReference("? extends java.test.Long");
    String ref3 = validator.useReference("?");
    String ref4 = validator.useReference("java.util.Set<? extends java.util.List<java.lang.CharSequence[]>[]>[]");
    String ref5 = validator.useReference("java.util.List<?extends java.lang.CharSequence>");
    String ref6 = validator.useReference("java.util.List<?>");
    String ref7 = validator.useReference("java.util.Set<? super java.util.List<java.lang.CharSequence[]>[]>[]");
    String ref8 = validator.useReference("java.util.List<?super java.lang.CharSequence>");

    assertEquals("? extends Long", ref);
    assertEquals("? extends java.test.Long", ref2);
    assertEquals("?", ref3);
    assertEquals("Set<? extends List<CharSequence[]>[]>[]", ref4);
    assertEquals("List<? extends CharSequence>", ref5);
    assertEquals("List<?>", ref6);
    assertEquals("Set<? super List<CharSequence[]>[]>[]", ref7);
    assertEquals("List<? super CharSequence>", ref8);
  }

  @Test
  public void testWithInnerClassOfPrimaryTypeHavingTypeArgs() {
    IImportCollector iv = createCompilationUnitImportCollector("a.b.c");
    IImportValidator validator = new ImportValidator(iv);
    String ref = validator.useReference("d.e.f.MyClassOne<java.lang.Long>.InnerClass.SecondInner");
    assertEquals("MyClassOne<Long>.InnerClass.SecondInner", ref);

    ref = validator.useReference("d.e.f.MyClassTwo<java.lang.Long>.InnerClass<java.lang.Boolean>.SecondInner<java.util.Map<java.lang.Long, java.lang.String>>.ThirdInner");
    assertEquals("MyClassTwo<Long>.InnerClass<Boolean>.SecondInner<Map<Long,String>>.ThirdInner", ref);

    ref = validator.useReference("d.e.f.TopLevel.MyClassThree<java.lang.Long>.InnerClass<java.lang.Boolean>.SecondInner<java.util.Map<java.lang.Long, java.lang.String>>.ThirdInner");
    assertEquals("MyClassThree<Long>.InnerClass<Boolean>.SecondInner<Map<Long,String>>.ThirdInner", ref);

    ref = validator.useReference("d.e.f.TopLevelAnother.MyClassThree<java.lang.Long>.InnerClass<org.test.Boolean>.SecondInner<java.util.Map<java.lang.Long[][][], java.lang.String[]>>.ThirdInner");
    assertEquals("d.e.f.TopLevelAnother.MyClassThree<Long>.InnerClass<org.test.Boolean>.SecondInner<Map<Long[][][],String[]>>.ThirdInner", ref);

    Collection<String> importsToCreate = toStringList(iv.getImports());
    assertEquals(7, importsToCreate.size());
    assertTrue(importsToCreate.contains("d.e.f.MyClassOne"));
    assertTrue(importsToCreate.contains("java.lang.Long"));
    assertTrue(importsToCreate.contains("d.e.f.MyClassTwo"));
    assertTrue(importsToCreate.contains("java.lang.Boolean"));
    assertTrue(importsToCreate.contains("java.util.Map"));
    assertTrue(importsToCreate.contains("java.lang.String"));
    assertTrue(importsToCreate.contains("d.e.f.TopLevel.MyClassThree"));
    assertFalse(importsToCreate.contains("d.e.f.TopLevelAnother.MyClassThree")); // d.e.f.TopLevelAnother.MyClassThree is not part of the imports becuase it would be fully qualified!
    assertFalse(importsToCreate.contains("org.test.Boolean")); // org.test.Boolean is not part of the list because it would be fully qualified!
  }

  @Test
  @ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
  public void testWithDuplicateInOwnPackage(IJavaEnvironment env) {
    IImportCollector iv = createCompilationUnitImportCollector(env.requireType(BaseClass.class.getName()).requireCompilationUnit());
    IImportValidator validator = new ImportValidator(iv);

    // long on foreign package
    String longName = validator.useReference(JavaTypes.Long);
    assertEquals(JavaTypes.Long, longName);

    longName = validator.useReference(JavaTypes.Long);
    assertEquals(JavaTypes.Long, longName);

    // long in own package
    longName = validator.useReference(org.eclipse.scout.sdk.core.fixture.Long.class.getName());
    assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getSimpleName(), longName);

    // own class in own package
    String baseClassName = validator.useReference(BaseClass.class.getName());
    assertEquals(BaseClass.class.getSimpleName(), baseClassName);

    // other class in own package
    String childClassName = validator.useReference(ChildClass.class.getName());
    assertEquals(ChildClass.class.getSimpleName(), childClassName);
  }

  protected static IImportCollector createCompilationUnitImportCollector(String packageName) {
    return new CompilationUnitScopedImportCollector(new ImportCollector(), packageName);
  }

  protected static List<String> toStringList(Stream<StringBuilder> b) {
    return b
        .map(StringBuilder::toString)
        .collect(toList());
  }

  protected static IImportCollector createCompilationUnitImportCollector(ICompilationUnit cu) {
    return createCompilationUnitImportCollector(cu.toWorkingCopy(), cu.javaEnvironment());
  }

  protected static IImportCollector createCompilationUnitImportCollector(ICompilationUnitGenerator<?> cuSrc, IJavaEnvironment env) {
    IImportCollector validator0 = new ImportCollector(env);
    cuSrc.imports().forEach(validator0::addImport);
    return new CompilationUnitScopedImportCollector(validator0, cuSrc.packageName().get());
  }

  protected static IImportCollector createEnclosingTypeImportCollector(IType t) {
    ICompilationUnitGenerator<?> cuSrc = t.requireCompilationUnit().toWorkingCopy();
    IImportCollector collector1 = createCompilationUnitImportCollector(cuSrc, t.javaEnvironment());
    return new EnclosingTypeScopedImportCollector(collector1, cuSrc.mainType().get());
  }
}
