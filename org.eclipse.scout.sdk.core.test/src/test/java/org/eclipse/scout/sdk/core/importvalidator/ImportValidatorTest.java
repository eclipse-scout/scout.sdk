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

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.scout.sdk.core.CoreTestingUtils;
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.ImportTestClass;
import org.eclipse.scout.sdk.core.model.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.testing.TestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ImportValidatorTest {

  @Test
  public void testJavaLangPackage() {
    ImportValidator iv = new ImportValidator("test");
    String longName = iv.getTypeName(Signature.createTypeSignature(Long.class.getName()));
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String longName2 = iv.getTypeName(Signature.createTypeSignature(Long.class.getName()));
    Assert.assertEquals(Long.class.getSimpleName(), longName2);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = iv.getTypeName(Signature.createTypeSignature(ownClassFqn));
    Assert.assertEquals("MyClass", ownName);

    Set<String> importsToCreate = iv.getImportsToCreate();
    Assert.assertEquals(new TreeSet<>(Arrays.asList(ownClassFqn)), importsToCreate);
  }

  @Test
  public void testOwnPackage() {
    ImportValidator iv = new ImportValidator("test.own.pck");

    String longName = iv.getTypeName(Signature.createTypeSignature(Long.class.getName()));
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = iv.getTypeName(Signature.createTypeSignature(ownClassFqn));
    Assert.assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass2";
    String ownName2 = iv.getTypeName(Signature.createTypeSignature(ownClassFqn2));
    Assert.assertEquals("MyClass2", ownName2);

    Set<String> importsToCreate = iv.getImportsToCreate();
    Assert.assertEquals(new TreeSet<>(Arrays.asList(ownClassFqn)), importsToCreate);
  }

  @Test
  public void testPrimitives() {
    ImportValidator iv = new ImportValidator("test");

    String intName = iv.getTypeName(ISignatureConstants.SIG_INT);
    Assert.assertEquals(Signature.getSignatureSimpleName(ISignatureConstants.SIG_INT), intName);

    Set<String> importsToCreate = iv.getImportsToCreate();
    Assert.assertEquals(new TreeSet<>(), importsToCreate);
  }

  @Test
  public void testNullPackage() {
    ImportValidator iv = new ImportValidator();

    String longName = iv.getTypeName(Signature.createTypeSignature(Long.class.getName()));
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = iv.getTypeName(Signature.createTypeSignature(ownClassFqn));
    Assert.assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass2";
    String ownName2 = iv.getTypeName(Signature.createTypeSignature(ownClassFqn2));
    Assert.assertEquals("MyClass2", ownName2);

    Set<String> importsToCreate = iv.getImportsToCreate();
    Assert.assertEquals(new TreeSet<>(Arrays.asList(ownClassFqn, ownClassFqn2)), importsToCreate);
  }

  @Test
  public void testQualifiedPackage() {
    ImportValidator iv = new ImportValidator("test");

    String longName = iv.getTypeName(Signature.createTypeSignature(Long.class.getName()));
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = iv.getTypeName(Signature.createTypeSignature(ownClassFqn));
    Assert.assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass";
    String ownName2 = iv.getTypeName(Signature.createTypeSignature(ownClassFqn2));
    Assert.assertEquals(ownClassFqn2, ownName2);

    Set<String> importsToCreate = iv.getImportsToCreate();
    Assert.assertEquals(new TreeSet<>(Arrays.asList(ownClassFqn)), importsToCreate);
  }

  @Test
  public void testWithInnerClasses() {
    String sig = "La.b.c.MyClass$InnerClass$SecondInner;";
    ImportValidator iv = new ImportValidator("a.b.c");
    Assert.assertEquals("SecondInner", iv.getTypeName(sig));
    Assert.assertEquals("a.b.c.SecondInner", iv.getTypeName("La.b.c.SecondInner;"));
  }

  @Test
  public void testIcuWithInnerClassesThatAlsoExistInOwnPackage() {
    ICompilationUnit importTest = TestingUtils.getType(ImportTestClass.class.getName(), CoreTestingUtils.SOURCE_FOLDER).getCompilationUnit();
    ImportValidator iv = new ImportValidator(importTest);

    Assert.assertEquals(ImportTestClass.Long.class.getSimpleName(), iv.getTypeName(Signature.createTypeSignature(((IType) ((IType) importTest.getTypes().get(0)).getTypes().get(0)).getName())));
    Assert.assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), iv.getTypeName(Signature.createTypeSignature(org.eclipse.scout.sdk.core.fixture.Long.class.getName())));
  }

  @Test
  public void testWithDuplicateInOwnPackage() {
    ImportValidator iv = new ImportValidator(CoreTestingUtils.getBaseClassIcu());

    // long on foreign package
    String longName = iv.getTypeName(Signature.createTypeSignature(Long.class.getName()));
    Assert.assertEquals(Long.class.getName(), longName);

    longName = iv.getTypeName(Signature.createTypeSignature(Long.class.getName()));
    Assert.assertEquals(Long.class.getName(), longName);

    // long in own package
    longName = iv.getTypeName(Signature.createTypeSignature(org.eclipse.scout.sdk.core.fixture.Long.class.getName()));
    Assert.assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getSimpleName(), longName);

    // own class in own package
    String baseClassName = iv.getTypeName(Signature.createTypeSignature(BaseClass.class.getName()));
    Assert.assertEquals(BaseClass.class.getSimpleName(), baseClassName);

    // other class in own package
    String childClassName = iv.getTypeName(Signature.createTypeSignature(ChildClass.class.getName()));
    Assert.assertEquals(ChildClass.class.getSimpleName(), childClassName);
  }
}
