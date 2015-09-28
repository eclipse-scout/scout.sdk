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
import org.eclipse.scout.sdk.core.importcollector.ImportCollector;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.importvalidator.ImportValidator;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
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

    IMethod firstCase = type.methods().withName("firstCase").first();
    Assert.assertNotNull(type);
    Assert.assertEquals(expected, SignatureUtils.getTypeSignature(firstCase.returnType()));
  }

  @Test
  public void testSimpleSignature() {
    String signature = "Ljava.lang.String;";
    IImportValidator validator = new ImportValidator(new ImportCollector());
    Assert.assertEquals(validator.useSignature(signature), "String");
  }

  @Test
  public void testGetResolvedSignature_Long() {
    IImportValidator validator = new ImportValidator(new ImportCollector());
    Assert.assertEquals("Long", validator.useName(Long.class.getName()));
  }

  @Test
  public void testGetSimpleTypeSignature_LongArray() {
    IImportValidator validator = new ImportValidator(new ImportCollector());
    String signature = Signature.createArraySignature(Signature.createTypeSignature(Long.class.getName()), 1);
    Assert.assertEquals("Long[]", validator.useSignature(signature));
  }

  @Test
  public void testGetSimpleTypeSignature_ArrayOfStringSets() {
    ImportCollector collector = new ImportCollector();
    IImportValidator validator = new ImportValidator(collector);
    String signature = Signature.createTypeSignature("java.util.Set<java.lang.String>[]");
    Assert.assertEquals("Set<String>[]", validator.useSignature(signature));
    Collection<String> imports = collector.createImportDeclarations();
    Assert.assertTrue(imports.contains("import java.util.Set;"));
  }

  @Test
  public void testObjectSignatureSlashBased() {
    IImportValidator validator = new ImportValidator(new ImportCollector());
    String signature = "Ljava/lang/Object;";
    Assert.assertEquals("java/lang/Object", validator.useSignature(signature));
  }

  @Test
  public void testGetSimpleTypeSignature_3dimArrayOfLongArrayOfObjectListsArrayMapSets() {
    ImportCollector collector = new ImportCollector();
    IImportValidator validator = new ImportValidator(collector);
    String signature = Signature.createTypeSignature("java.util.Set<java.util.HashMap<java.lang.Long, java.util.List<java.lang.Object>[]>>[][][]");
    Assert.assertEquals("Set<HashMap<Long, List<Object>[]>>[][][]", validator.useSignature(signature));
    Collection<String> imports = collector.createImportDeclarations();
    Assert.assertTrue(imports.contains("import java.util.Set;"));
    Assert.assertTrue(imports.contains("import java.util.HashMap;"));
    Assert.assertTrue(imports.contains("import java.util.List;"));
  }

  @Test
  public void testClassParameterized() {
    String signature = "[Ljava.lang.Class<+[Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;";
    IImportValidator validator = new ImportValidator(new ImportCollector());

    String result = validator.useSignature(signature);
    Assert.assertEquals("Class<? extends IOutline[]>[]", result);
  }

  @Test
  public void testComplexNestedArrayListHashMapArray() {
    String signature = "[Ljava.util.HashMap<Ljava.util.ArrayList<[[Ljava.lang.String;>;Lorg.eclipse.scout.sdk.workspace.member.IScoutType;>;";
    IImportValidator validator = new ImportValidator(new ImportCollector());
    String result = validator.useSignature(signature);
    Assert.assertEquals("HashMap<ArrayList<String[][]>, IScoutType>[]", result);
  }

  @Test
  public void testGenericExtendsWithArray() {
    String signature = "[Ljava.lang.Class<+[Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;";
    ImportCollector collector = new ImportCollector();
    IImportValidator validator = new ImportValidator(collector);
    Assert.assertEquals(validator.useSignature(signature), "Class<? extends IOutline[]>[]");
    Collection<String> imports = collector.createImportDeclarations();
    Assert.assertTrue(imports.remove("import com.bsiag.scout.client.ui.desktop.outline.IOutline;"));
    Assert.assertTrue(imports.isEmpty());
  }
}
