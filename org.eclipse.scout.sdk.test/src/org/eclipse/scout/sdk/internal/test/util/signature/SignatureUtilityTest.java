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
package org.eclipse.scout.sdk.internal.test.util.signature;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.IResolvedTypeParameter;
import org.eclipse.scout.sdk.util.signature.ITypeParameterMapping;
import org.eclipse.scout.sdk.util.signature.ImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h3>{@link SignatureUtilityTest}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 23.02.2011
 */
public class SignatureUtilityTest extends AbstractScoutSdkTest {

  private static final String LONG_SIGNATURE = SignatureCache.createTypeSignature(Long.class.getName());
  private static final String TEST_PACKAGE = "signature.tests";

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/util/signature", "signature.tests");
  }

  @Test
  public void testSimpleSignature() throws Exception {
    String signature = "Ljava.lang.String;";
    IImportValidator validator = new ImportValidator(TEST_PACKAGE);
    Assert.assertEquals(SignatureUtility.getTypeReference(signature, validator), "String");
  }

  @Test
  public void testGetResolvedSignature_Long() throws Exception {
    ImportValidator importValidator = new ImportValidator(TEST_PACKAGE);
    Assert.assertEquals("Long", SignatureUtility.getTypeReference(LONG_SIGNATURE, importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_LongArray() throws Exception {
    ImportValidator importValidator = new ImportValidator(TEST_PACKAGE);
    String signature = Signature.createArraySignature(LONG_SIGNATURE, 1);
    Assert.assertEquals("Long[]", SignatureUtility.getTypeReference(signature, importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_ArrayOfStringSets() throws Exception {
    ImportValidator importValidator = new ImportValidator(TEST_PACKAGE);
    String signature = SignatureCache.createTypeSignature("java.util.Set<java.lang.String>[]");
    Assert.assertEquals("Set<String>[]", SignatureUtility.getTypeReference(signature, importValidator));
    Set<String> imports = importValidator.getImportsToCreate();
    Assert.assertTrue(imports.contains("java.util.Set"));
  }

  @Test
  public void testObjectSignatureSlashBased() throws Exception {
    ImportValidator importValidator = new ImportValidator(TEST_PACKAGE);
    String signature = "Ljava/lang/Object;";
    Assert.assertEquals("java/lang/Object", SignatureUtility.getTypeReference(signature, importValidator));
  }

  @Test
  public void testObjectArray6() throws Exception {
    ImportValidator importValidator = new ImportValidator(TEST_PACKAGE);
    String signature = "|Ljava/lang/Object;";
    Assert.assertEquals("java/lang/Object...", SignatureUtility.getTypeReference(signature, importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_3dimArrayOfLongArrayOfObjectListsArrayMapSets() throws Exception {
    ImportValidator importValidator = new ImportValidator(TEST_PACKAGE);
    String signature = SignatureCache.createTypeSignature("java.util.Set<java.util.HashMap<java.lang.Long, java.util.List<java.lang.Object>[]>>[][][]");
    Assert.assertEquals("Set<HashMap<Long, List<Object>[]>>[][][]", SignatureUtility.getTypeReference(signature, importValidator));
    Set<String> imports = importValidator.getImportsToCreate();
    Assert.assertTrue(imports.contains("java.util.Set"));
    Assert.assertTrue(imports.contains("java.util.HashMap"));
    Assert.assertTrue(imports.contains("java.util.List"));
  }

  @Test
  //TODO: re-enable timeout as soon as a dedicated performance testing infrastructure is available (timeout = 250L)
  public void testClassParameterized() throws Exception {
    String signature = "[Ljava.lang.Class<+[Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;";
    ImportValidator validator = new ImportValidator(TEST_PACKAGE);

    String result = SignatureUtility.getTypeReference(signature, validator);
    Assert.assertEquals("Class<? extends IOutline[]>[]", result);
  }

  @Test
  //TODO: re-enable timeout as soon as a dedicated performance testing infrastructure is available (timeout = 8L)
  public void testComplexNestedArrayListHashMapArray() throws Exception {
    String signature = "[Ljava.util.HashMap<Ljava.util.ArrayList<[[Ljava.lang.String;>;Lorg.eclipse.scout.sdk.workspace.member.IScoutType;>;";
    ImportValidator validator = new ImportValidator(TEST_PACKAGE);
    String result = SignatureUtility.getTypeReference(signature, validator);
    Assert.assertEquals("HashMap<ArrayList<String[][]>, IScoutType>[]", result);
  }

  @Test
  public void testGenericByteArray() throws Exception {
    String signature = "L" + RuntimeClasses.AbstractPropertyData + "<[" + Signature.SIG_BYTE + ">;";
    IImportValidator validator = new ImportValidator(TEST_PACKAGE);
    Assert.assertEquals(SignatureUtility.getTypeReference(signature, validator), "AbstractPropertyData<byte[]>");
  }

  @Test
  public void testGenericOfAny() throws Exception {
    String signature = "L" + RuntimeClasses.AbstractPropertyData + "<*>;";
    IImportValidator validator = new ImportValidator(TEST_PACKAGE);
    Assert.assertEquals(SignatureUtility.getTypeReference(signature, validator), "AbstractPropertyData<?>");
    Set<String> imports = validator.getImportsToCreate();
    Assert.assertTrue(imports.remove(RuntimeClasses.AbstractPropertyData));
    Assert.assertTrue(imports.isEmpty());
  }

  @Test
  public void testGenericExtendsWithArray() throws Exception {
    String signature = "[Ljava.lang.Class<+[Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;";
    IImportValidator validator = new ImportValidator(TEST_PACKAGE);
    Assert.assertEquals(SignatureUtility.getTypeReference(signature, validator), "Class<? extends IOutline[]>[]");
    Set<String> imports = validator.getImportsToCreate();
    Assert.assertTrue(imports.remove("com.bsiag.scout.client.ui.desktop.outline.IOutline"));
    Assert.assertTrue(imports.isEmpty());
  }

  @Test
  public void testResolveSignature() throws Exception {
    IType type = TypeUtility.getType("signature.tests.SignatureTest");

    String resolvedSignature01 = SignatureUtility.getResolvedSignature("Qsignature.tests.SignatureRefType.InnerType01;", type);
    Assert.assertEquals("Lsignature.tests.SignatureRefType.InnerType01;", resolvedSignature01);
    Assert.assertTrue(TypeUtility.exists(TypeUtility.getTypeBySignature(resolvedSignature01)));

    String resolvedSignature02 = SignatureUtility.getResolvedSignature("QSignatureRefType.InnerType01;", type);
    Assert.assertEquals("Lsignature.tests.SignatureRefType.InnerType01;", resolvedSignature02);
    Assert.assertTrue(TypeUtility.exists(TypeUtility.getTypeBySignature(resolvedSignature02)));

    String resolvedSignature03 = SignatureUtility.getResolvedSignature("QInnerType01;", type);
    Assert.assertEquals("Lsignature.tests.SignatureRefType.InnerType01;", resolvedSignature03);
    Assert.assertTrue(TypeUtility.exists(TypeUtility.getTypeBySignature(resolvedSignature03)));

    String resolvedSig04 = SignatureUtility.getResolvedSignature("Qsignature.tests.SignatureRefType.InnerType02.InnerType03;", type);
    Assert.assertEquals("Lsignature.tests.SignatureRefType.InnerType02.InnerType03;", resolvedSig04);
    Assert.assertTrue(TypeUtility.exists(TypeUtility.getTypeBySignature(resolvedSig04)));

    String resolvedSig05 = SignatureUtility.getResolvedSignature("QSignatureRefType.InnerType02.InnerType03;", type);
    Assert.assertEquals("Lsignature.tests.SignatureRefType.InnerType02.InnerType03;", resolvedSig05);
    Assert.assertTrue(TypeUtility.exists(TypeUtility.getTypeBySignature(resolvedSig05)));

    String resolvedSig06 = SignatureUtility.getResolvedSignature("QInnerType02.InnerType03;", type);
    Assert.assertEquals("Lsignature.tests.SignatureRefType.InnerType02.InnerType03;", resolvedSig06);
    Assert.assertTrue(TypeUtility.exists(TypeUtility.getTypeBySignature(resolvedSig06)));
  }

  @Test
  public void testGenericResolver01() throws Exception {
    String superTypeSignature = Signature.createTypeSignature("signature.tests.generic.AbstractClass01<" + String.class.getName() + "," + Integer.class.getName() + ">", true);
    Map<String, ITypeParameterMapping> mappings = SignatureUtility.resolveTypeParameters(Signature.createTypeSignature("signature.tests.generic.output.TestType", true), superTypeSignature, null);
    Assert.assertEquals(CollectionUtility.hashSet(Signature.createTypeSignature(String.class.getName(), true)),
        mappings.get("signature.tests.generic.IInterface01").getTypeParameterBounds(0));
    Assert.assertEquals(CollectionUtility.hashSet(Signature.createTypeSignature(Integer.class.getName(), true)),
        mappings.get("signature.tests.generic.IInterface02").getTypeParameterBounds(0));
  }

  @Test
  public void testGenericResolver02() throws Exception {
    String superTypeSignature = Signature.createTypeSignature("signature.tests.generic.AbstractClass02<" + Integer.class.getName() + ">", true);
    Map<String, ITypeParameterMapping> mappings = SignatureUtility.resolveTypeParameters(Signature.createTypeSignature("signature.tests.generic.output.TestType", true), superTypeSignature, null);
    Assert.assertEquals(CollectionUtility.hashSet(Signature.createTypeSignature(String.class.getName(), true)),
        mappings.get("signature.tests.generic.IInterface01").getTypeParameterBounds("GENERIC"));
    Assert.assertEquals(CollectionUtility.hashSet(Signature.createTypeSignature(List.class.getName() + "<" + Integer.class.getName() + ">", true)),
        mappings.get("signature.tests.generic.IInterface02").getTypeParameterBounds("A"));
  }

  @Test
  public void testGenericResolver03() throws Exception {
    String superTypeSignature = Signature.createTypeSignature("org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField<" + Integer.class.getName() + ">", true);
    Map<String, ITypeParameterMapping> mappings = SignatureUtility.resolveTypeParameters(Signature.createTypeSignature("signature.tests.generic.output.TestType", true), superTypeSignature, null);
    Assert.assertEquals(CollectionUtility.hashSet(Signature.createTypeSignature(Integer.class.getName(), true)),
        mappings.get("org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField").getTypeParameterBounds(0));
  }

  @Test
  public void testGenericResolver04() throws Exception {
    Map<String, ITypeParameterMapping> mappings = SignatureUtility.resolveTypeParameters(TypeUtility.getType("org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField"));
    Assert.assertEquals(CollectionUtility.hashSet(Signature.createTypeSignature(Object.class.getName(), true)),
        mappings.get(IRuntimeClasses.IValueField).getTypeParameterBounds(0));
  }

  @Test
  public void testGenericResolver05() throws Exception {
    String typeName = "org.eclipse.scout.rt.shared.extension.AbstractSerializableExtension";
    Map<String, ITypeParameterMapping> mappings = SignatureUtility.resolveTypeParameters(TypeUtility.getType(typeName));
    Assert.assertEquals(CollectionUtility.hashSet(Signature.createTypeSignature("org.eclipse.scout.rt.shared.extension.IExtensibleObject", true), Signature.createTypeSignature(Serializable.class.getName(), true)),
        mappings.get(typeName).getTypeParameter(0).getBoundsSignatures());
  }

  @Test
  public void testGenericResolverHierarchy() throws Exception {
    String startClass = "signature.tests.generic.Level01Class";
    String middleClass = "signature.tests.generic.Level02Class";
    String middleIfc = "signature.tests.generic.Level02Ifc";
    String topIfc = "signature.tests.generic.Level03Ifc";
    String topIfcB = "signature.tests.generic.Level03IfcB";
    IType type = TypeUtility.getType(startClass);
    SdkAssert.assertExist(type);

    Map<String, ITypeParameterMapping> collector = SignatureUtility.resolveTypeParameters(type);

    Assert.assertEquals(8, collector.size());

    ITypeParameterMapping mappingLevel01 = collector.get(startClass);
    Assert.assertEquals(2, mappingLevel01.getParameterCount());
    Assert.assertEquals(CollectionUtility.hashSet("TA01;"), mappingLevel01.getTypeParameter(0).getBoundsSignatures());
    Assert.assertEquals(CollectionUtility.hashSet("TB01;"), mappingLevel01.getTypeParameter(1).getBoundsSignatures());

    Map<String, ITypeParameterMapping> mappingLevel02 = mappingLevel01.getSuperMappings();
    Assert.assertEquals(3, mappingLevel02.size());

    ITypeParameterMapping middleClassMapping = mappingLevel01.getSuperMapping(middleClass);
    ITypeParameterMapping middleIfcMapping = middleClassMapping.getSuperMapping(middleIfc);
    Assert.assertEquals(1, middleIfcMapping.getTypeParameter("BI02").getOrdinal());
    ITypeParameterMapping topIfcMapping = middleIfcMapping.getSuperMapping(topIfcB);
    Assert.assertEquals(CollectionUtility.hashSet("TB01;"), topIfcMapping.getTypeParameterBounds(0));

    // A01 on Level01Class
    IResolvedTypeParameter A01 = mappingLevel01.getTypeParameter(0);
    Map<String, Set<IResolvedTypeParameter>> allReferences = A01.getAllReferences();
    Assert.assertEquals(2, allReferences.size());

    // A01 on Level02Class
    Set<IResolvedTypeParameter> referencesOfA01 = A01.getReferences(middleClass);
    Assert.assertEquals(1, referencesOfA01.size());
    A01 = CollectionUtility.firstElement(referencesOfA01);

    // A01 on Level02Ifc
    referencesOfA01 = A01.getReferences(middleIfc);
    Assert.assertEquals(1, referencesOfA01.size());
    A01 = CollectionUtility.firstElement(referencesOfA01);

    // A01 on Level03Ifc
    referencesOfA01 = A01.getReferences(topIfc);
    Assert.assertEquals(1, referencesOfA01.size());
    A01 = CollectionUtility.firstElement(referencesOfA01);

    // check that no duplicates are created
    Assert.assertSame(collector.get(topIfc).getTypeParameter("AI03"), A01);
    IResolvedTypeParameter correspondingTypeParameterOnSubLevel = A01.getCorrespondingTypeParameterOnSubLevel(mappingLevel01.getType());

    // walk down again
    for (int i = 0; i < 3; i++) {
      A01 = A01.getReferencedTypeParameter();
    }

    Assert.assertSame(mappingLevel01.getTypeParameter("A01"), A01);
    Assert.assertSame(A01, correspondingTypeParameterOnSubLevel);
    Assert.assertEquals(0, A01.getOrdinal());
  }
}
