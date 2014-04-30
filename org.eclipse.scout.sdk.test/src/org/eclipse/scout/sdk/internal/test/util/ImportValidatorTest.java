/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.test.util;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.util.signature.ImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link ImportValidatorTest}</h3>
 * 
 * @author Matthias Villiger
 * @since 4.0.0 27.03.2014
 */
public class ImportValidatorTest {

  @Test
  public void testJavaLangPackage() throws Exception {
    ImportValidator iv = new ImportValidator("test");
    String longName = iv.getTypeName(SignatureCache.createTypeSignature(Long.class.getName()));
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String longName2 = iv.getTypeName(SignatureCache.createTypeSignature(Long.class.getName()));
    Assert.assertEquals(Long.class.getSimpleName(), longName2);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = iv.getTypeName(SignatureCache.createTypeSignature(ownClassFqn));
    Assert.assertEquals("MyClass", ownName);

    Set<String> importsToCreate = iv.getImportsToCreate();
    Assert.assertEquals(new TreeSet<String>(CollectionUtility.arrayList(ownClassFqn)), importsToCreate);
  }

  @Test
  public void testOwnPackage() throws Exception {
    ImportValidator iv = new ImportValidator("test.own.pck");

    String longName = iv.getTypeName(SignatureCache.createTypeSignature(Long.class.getName()));
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = iv.getTypeName(SignatureCache.createTypeSignature(ownClassFqn));
    Assert.assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass2";
    String ownName2 = iv.getTypeName(SignatureCache.createTypeSignature(ownClassFqn2));
    Assert.assertEquals("MyClass2", ownName2);

    Set<String> importsToCreate = iv.getImportsToCreate();
    Assert.assertEquals(new TreeSet<String>(CollectionUtility.arrayList(ownClassFqn)), importsToCreate);
  }

  @Test
  public void testPrimitives() throws Exception {
    ImportValidator iv = new ImportValidator("test");

    String intName = iv.getTypeName(Signature.SIG_INT);
    Assert.assertEquals(Signature.getSignatureSimpleName(Signature.SIG_INT), intName);

    Set<String> importsToCreate = iv.getImportsToCreate();
    Assert.assertEquals(new TreeSet<String>(), importsToCreate);
  }

  @Test
  public void testNullPackage() throws Exception {
    ImportValidator iv = new ImportValidator();

    String longName = iv.getTypeName(SignatureCache.createTypeSignature(Long.class.getName()));
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = iv.getTypeName(SignatureCache.createTypeSignature(ownClassFqn));
    Assert.assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass2";
    String ownName2 = iv.getTypeName(SignatureCache.createTypeSignature(ownClassFqn2));
    Assert.assertEquals("MyClass2", ownName2);

    Set<String> importsToCreate = iv.getImportsToCreate();
    Assert.assertEquals(new TreeSet<String>(CollectionUtility.arrayList(ownClassFqn, ownClassFqn2)), importsToCreate);
  }

  @Test
  public void testQualifiedPackage() throws Exception {
    ImportValidator iv = new ImportValidator("test");

    String longName = iv.getTypeName(SignatureCache.createTypeSignature(Long.class.getName()));
    Assert.assertEquals(Long.class.getSimpleName(), longName);

    String ownClassFqn = "test.blub.MyClass";
    String ownName = iv.getTypeName(SignatureCache.createTypeSignature(ownClassFqn));
    Assert.assertEquals("MyClass", ownName);

    String ownClassFqn2 = "test.own.pck.MyClass";
    String ownName2 = iv.getTypeName(SignatureCache.createTypeSignature(ownClassFqn2));
    Assert.assertEquals(ownClassFqn2, ownName2);

    Set<String> importsToCreate = iv.getImportsToCreate();
    Assert.assertEquals(new TreeSet<String>(CollectionUtility.arrayList(ownClassFqn)), importsToCreate);
  }
}
