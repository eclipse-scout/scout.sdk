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
package org.eclipse.scout.sdk.core.signature;

import java.util.Set;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.importvalidator.ImportValidator;
import org.eclipse.scout.sdk.core.model.IMethod;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.testing.TestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class SignatureUtilsTest {

  @Test
  public void testGetResolvedSignature() throws Exception {
    IType type = TestingUtils.getType(ChildClass.class.getName());
    Assert.assertNotNull(type);

    String expected = Signature.createTypeSignature("java.util.Set<java.util.HashMap<org.eclipse.scout.sdk.core.fixture.Long, java.util.List<java.lang.Object>[]>>[][][]");
    Assert.assertNotNull(expected);

    IMethod firstCase = CoreUtils.getMethod(type, "firstCase");
    Assert.assertNotNull(type);
    Assert.assertEquals(expected, SignatureUtils.getResolvedSignature(firstCase.getReturnType()));
  }

  @Test
  public void testSimpleSignature() throws Exception {
    String signature = "Ljava.lang.String;";
    IImportValidator validator = new ImportValidator();
    Assert.assertEquals(SignatureUtils.getTypeReference(signature, validator), "String");
  }

  @Test
  public void testGetResolvedSignature_Long() throws Exception {
    ImportValidator importValidator = new ImportValidator();
    Assert.assertEquals("Long", SignatureUtils.getTypeReference(Signature.createTypeSignature(Long.class.getName()), importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_LongArray() throws Exception {
    ImportValidator importValidator = new ImportValidator();
    String signature = Signature.createArraySignature(Signature.createTypeSignature(Long.class.getName()), 1);
    Assert.assertEquals("Long[]", SignatureUtils.getTypeReference(signature, importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_ArrayOfStringSets() throws Exception {
    ImportValidator importValidator = new ImportValidator();
    String signature = Signature.createTypeSignature("java.util.Set<java.lang.String>[]");
    Assert.assertEquals("Set<String>[]", SignatureUtils.getTypeReference(signature, importValidator));
    Set<String> imports = importValidator.getImportsToCreate();
    Assert.assertTrue(imports.contains("java.util.Set"));
  }

  @Test
  public void testObjectSignatureSlashBased() throws Exception {
    ImportValidator importValidator = new ImportValidator();
    String signature = "Ljava/lang/Object;";
    Assert.assertEquals("java/lang/Object", SignatureUtils.getTypeReference(signature, importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_3dimArrayOfLongArrayOfObjectListsArrayMapSets() throws Exception {
    ImportValidator importValidator = new ImportValidator();
    String signature = Signature.createTypeSignature("java.util.Set<java.util.HashMap<java.lang.Long, java.util.List<java.lang.Object>[]>>[][][]");
    Assert.assertEquals("Set<HashMap<Long, List<Object>[]>>[][][]", SignatureUtils.getTypeReference(signature, importValidator));
    Set<String> imports = importValidator.getImportsToCreate();
    Assert.assertTrue(imports.contains("java.util.Set"));
    Assert.assertTrue(imports.contains("java.util.HashMap"));
    Assert.assertTrue(imports.contains("java.util.List"));
  }

  @Test
  public void testClassParameterized() throws Exception {
    String signature = "[Ljava.lang.Class<+[Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;";
    ImportValidator validator = new ImportValidator();

    String result = SignatureUtils.getTypeReference(signature, validator);
    Assert.assertEquals("Class<? extends IOutline[]>[]", result);
  }

  @Test
  public void testComplexNestedArrayListHashMapArray() throws Exception {
    String signature = "[Ljava.util.HashMap<Ljava.util.ArrayList<[[Ljava.lang.String;>;Lorg.eclipse.scout.sdk.workspace.member.IScoutType;>;";
    ImportValidator validator = new ImportValidator();
    String result = SignatureUtils.getTypeReference(signature, validator);
    Assert.assertEquals("HashMap<ArrayList<String[][]>, IScoutType>[]", result);
  }

  @Test
  public void testGenericExtendsWithArray() throws Exception {
    String signature = "[Ljava.lang.Class<+[Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;";
    IImportValidator validator = new ImportValidator();
    Assert.assertEquals(SignatureUtils.getTypeReference(signature, validator), "Class<? extends IOutline[]>[]");
    Set<String> imports = validator.getImportsToCreate();
    Assert.assertTrue(imports.remove("com.bsiag.scout.client.ui.desktop.outline.IOutline"));
    Assert.assertTrue(imports.isEmpty());
  }
}
