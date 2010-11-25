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
package org.eclipse.scout.sdk.test;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.SimpleImportValidator;
import org.junit.Assert;
import org.junit.Test;

public class ScoutSdkUtilityTest {

  private static final String LONG_SIGNATURE = Signature.createTypeSignature("java.lang.Long", true);

  @Test
  public void testGetResolvedSignature_Long() throws Exception {
    SimpleImportValidator importValidator = new SimpleImportValidator();
    Assert.assertEquals("Long", ScoutSdkUtility.getSimpleTypeRefName(LONG_SIGNATURE, importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_LongArray() throws Exception {
    SimpleImportValidator importValidator = new SimpleImportValidator();
    String signature = Signature.createArraySignature(LONG_SIGNATURE, 1);
    Assert.assertEquals("Long[]", ScoutSdkUtility.getSimpleTypeRefName(signature, importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_ArrayOfStringSets() throws Exception {
    SimpleImportValidator importValidator = new SimpleImportValidator();
    String signature = Signature.createTypeSignature("java.util.Set<java.lang.String>[]", true);
    Assert.assertEquals("Set<String>[]", ScoutSdkUtility.getSimpleTypeRefName(signature, importValidator));
    List<String> imports = Arrays.asList(importValidator.getImportsToCreate());
    Assert.assertTrue(imports.contains("java.util.Set"));
  }

  @Test
  public void testObjectSignatureSlashBased() throws Exception {
    SimpleImportValidator importValidator = new SimpleImportValidator();
    String signature = "Ljava/lang/Object;";
    Assert.assertEquals("Object", ScoutSdkUtility.getSimpleTypeRefName(signature, importValidator));
  }

  @Test
  public void testObjectArray6() throws Exception {
    SimpleImportValidator importValidator = new SimpleImportValidator();
    String signature = "|Ljava/lang/Object;";
    Assert.assertEquals("Object...", ScoutSdkUtility.getSimpleTypeRefName(signature, importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_3dimArrayOfLongArrayOfObjectListsArrayMapSets() throws Exception {
    SimpleImportValidator importValidator = new SimpleImportValidator();
    String signature = Signature.createTypeSignature("java.util.Set<java.util.HashMap<java.lang.Long, java.util.List<java.lang.Object>[]>>[][][]", true);
    Assert.assertEquals("Set<HashMap<Long,List<Object>[]>>[][][]", ScoutSdkUtility.getSimpleTypeRefName(signature, importValidator));
    List<String> imports = Arrays.asList(importValidator.getImportsToCreate());
    Assert.assertTrue(imports.contains("java.util.Set"));
    Assert.assertTrue(imports.contains("java.util.HashMap"));
    Assert.assertTrue(imports.contains("java.util.List"));
  }
}
