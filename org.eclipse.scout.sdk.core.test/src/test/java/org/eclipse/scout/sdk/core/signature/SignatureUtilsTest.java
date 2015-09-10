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

import java.util.Collection;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.Long;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.importvalidator.ImportValidator;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class SignatureUtilsTest {

  @Test
  public void testGetResolvedSignature() {
    IType type = CoreTestingUtils.createJavaEnvironment().findType(ChildClass.class.getName());
    Assert.assertNotNull(type);

    String expected = Signature.createTypeSignature("java.util.Set<java.util.HashMap<org.eclipse.scout.sdk.core.fixture.Long, java.util.List<java.lang.Object>[]>>[][][]");
    Assert.assertNotNull(expected);

    IMethod firstCase = CoreUtils.getMethod(type, "firstCase");
    Assert.assertNotNull(type);
    Assert.assertEquals(expected, SignatureUtils.getTypeSignature(firstCase.getReturnType()));
  }

  @Test
  public void testSimpleSignature() {
    String signature = "Ljava.lang.String;";
    IImportValidator validator = new ImportValidator();
    Assert.assertEquals(SignatureUtils.useSignature(signature, validator), "String");
  }

  @Test
  public void testGetResolvedSignature_Long() {
    ImportValidator importValidator = new ImportValidator();
    Assert.assertEquals("Long", SignatureUtils.useName(Long.class.getName(), importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_LongArray() {
    ImportValidator importValidator = new ImportValidator();
    String signature = Signature.createArraySignature(Signature.createTypeSignature(Long.class.getName()), 1);
    Assert.assertEquals("Long[]", SignatureUtils.useSignature(signature, importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_ArrayOfStringSets() {
    ImportValidator importValidator = new ImportValidator();
    String signature = Signature.createTypeSignature("java.util.Set<java.lang.String>[]");
    Assert.assertEquals("Set<String>[]", SignatureUtils.useSignature(signature, importValidator));
    Collection<String> imports = importValidator.createImportDeclarations();
    Assert.assertTrue(imports.contains("import java.util.Set;"));
  }

  @Test
  public void testObjectSignatureSlashBased() {
    ImportValidator importValidator = new ImportValidator();
    String signature = "Ljava/lang/Object;";
    Assert.assertEquals("java/lang/Object", SignatureUtils.useSignature(signature, importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_3dimArrayOfLongArrayOfObjectListsArrayMapSets() {
    ImportValidator importValidator = new ImportValidator();
    String signature = Signature.createTypeSignature("java.util.Set<java.util.HashMap<java.lang.Long, java.util.List<java.lang.Object>[]>>[][][]");
    Assert.assertEquals("Set<HashMap<Long, List<Object>[]>>[][][]", SignatureUtils.useSignature(signature, importValidator));
    Collection<String> imports = importValidator.createImportDeclarations();
    Assert.assertTrue(imports.contains("import java.util.Set;"));
    Assert.assertTrue(imports.contains("import java.util.HashMap;"));
    Assert.assertTrue(imports.contains("import java.util.List;"));
  }

  @Test
  public void testClassParameterized() {
    String signature = "[Ljava.lang.Class<+[Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;";
    ImportValidator validator = new ImportValidator();

    String result = SignatureUtils.useSignature(signature, validator);
    Assert.assertEquals("Class<? extends IOutline[]>[]", result);
  }

  @Test
  public void testComplexNestedArrayListHashMapArray() {
    String signature = "[Ljava.util.HashMap<Ljava.util.ArrayList<[[Ljava.lang.String;>;Lorg.eclipse.scout.sdk.workspace.member.IScoutType;>;";
    ImportValidator validator = new ImportValidator();
    String result = SignatureUtils.useSignature(signature, validator);
    Assert.assertEquals("HashMap<ArrayList<String[][]>, IScoutType>[]", result);
  }

  @Test
  public void testGenericExtendsWithArray() {
    String signature = "[Ljava.lang.Class<+[Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;";
    IImportValidator validator = new ImportValidator();
    Assert.assertEquals(SignatureUtils.useSignature(signature, validator), "Class<? extends IOutline[]>[]");
    Collection<String> imports = validator.createImportDeclarations();
    Assert.assertTrue(imports.remove("import com.bsiag.scout.client.ui.desktop.outline.IOutline;"));
    Assert.assertTrue(imports.isEmpty());
  }
}
