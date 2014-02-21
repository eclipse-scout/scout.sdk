/**
 *
 */
package org.eclipse.scout.sdk.internal.test.util.signature;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.FullyQuallifiedValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.ITypeGenericMapping;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.signature.SimpleImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h3>{@link SignatureUtilityTest}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 23.02.2011
 */
public class SignatureUtilityTest extends AbstractScoutSdkTest {

  private static final String LONG_SIGNATURE = SignatureCache.createTypeSignature("java.lang.Long");

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/util/signature", "signature.tests");
  }

  @Test
  public void testSimpleSignature() throws Exception {
    String signature = "Ljava.lang.String;";
    IImportValidator validator = new SimpleImportValidator();
    Assert.assertEquals(SignatureUtility.getTypeReference(signature, validator), "String");
  }

  @Test
  public void testGetResolvedSignature_Long() throws Exception {
    SimpleImportValidator importValidator = new SimpleImportValidator();
    Assert.assertEquals("Long", SignatureUtility.getTypeReference(LONG_SIGNATURE, importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_LongArray() throws Exception {
    SimpleImportValidator importValidator = new SimpleImportValidator();
    String signature = Signature.createArraySignature(LONG_SIGNATURE, 1);
    Assert.assertEquals("Long[]", SignatureUtility.getTypeReference(signature, importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_ArrayOfStringSets() throws Exception {
    SimpleImportValidator importValidator = new SimpleImportValidator();
    String signature = SignatureCache.createTypeSignature("java.util.Set<java.lang.String>[]");
    Assert.assertEquals("Set<String>[]", SignatureUtility.getTypeReference(signature, importValidator));
    List<String> imports = Arrays.asList(importValidator.getImportsToCreate());
    Assert.assertTrue(imports.contains("java.util.Set"));
  }

  @Test
  public void testObjectSignatureSlashBased() throws Exception {
    SimpleImportValidator importValidator = new SimpleImportValidator();
    String signature = "Ljava/lang/Object;";
    Assert.assertEquals("java/lang/Object", SignatureUtility.getTypeReference(signature, importValidator));
  }

  @Test
  public void testObjectArray6() throws Exception {
    SimpleImportValidator importValidator = new SimpleImportValidator();
    String signature = "|Ljava/lang/Object;";
    Assert.assertEquals("java/lang/Object...", SignatureUtility.getTypeReference(signature, importValidator));
  }

  @Test
  public void testGetSimpleTypeSignature_3dimArrayOfLongArrayOfObjectListsArrayMapSets() throws Exception {
    SimpleImportValidator importValidator = new SimpleImportValidator();
    String signature = SignatureCache.createTypeSignature("java.util.Set<java.util.HashMap<java.lang.Long, java.util.List<java.lang.Object>[]>>[][][]");
    Assert.assertEquals("Set<HashMap<Long, List<Object>[]>>[][][]", SignatureUtility.getTypeReference(signature, importValidator));
    List<String> imports = Arrays.asList(importValidator.getImportsToCreate());
    Assert.assertTrue(imports.contains("java.util.Set"));
    Assert.assertTrue(imports.contains("java.util.HashMap"));
    Assert.assertTrue(imports.contains("java.util.List"));
  }

  @Test(timeout = 250L)
  public void testClassParameterized() throws Exception {
    String signature = "[Ljava.lang.Class<+[Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;";
    FullyQuallifiedValidator validator = new FullyQuallifiedValidator();

    String result = SignatureUtility.getTypeReference(signature, validator);
    Assert.assertEquals("java.lang.Class<? extends com.bsiag.scout.client.ui.desktop.outline.IOutline[]>[]", result);
  }

  @Test(timeout = 8L)
  public void testComplexNestedArrayListHashMapArray() throws Exception {
    String signature = "[Ljava.util.HashMap<Ljava.util.ArrayList<[[Ljava.lang.String;>;Lorg.eclipse.scout.sdk.workspace.member.IScoutType;>;";
    FullyQuallifiedValidator validator = new FullyQuallifiedValidator();
    String result = SignatureUtility.getTypeReference(signature, validator);
    Assert.assertEquals("java.util.HashMap<java.util.ArrayList<java.lang.String[][]>, org.eclipse.scout.sdk.workspace.member.IScoutType>[]", result);
  }

  @Test
  public void testGenericByteArray() throws Exception {
    String signature = "L" + RuntimeClasses.AbstractPropertyData + "<[" + Signature.SIG_BYTE + ">;";
    IImportValidator validator = new SimpleImportValidator();
    Assert.assertEquals(SignatureUtility.getTypeReference(signature, validator), "AbstractPropertyData<byte[]>");
  }

  @Test
  public void testGenericOfAny() throws Exception {
    String signature = "L" + RuntimeClasses.AbstractPropertyData + "<*>;";
    IImportValidator validator = new SimpleImportValidator();
    Assert.assertEquals(SignatureUtility.getTypeReference(signature, validator), "AbstractPropertyData<?>");
    HashSet<String> imports = new HashSet<String>(Arrays.asList(validator.getImportsToCreate()));
    Assert.assertTrue(imports.remove(RuntimeClasses.AbstractPropertyData));
    Assert.assertTrue(imports.isEmpty());
  }

  @Test
  public void testGenericExtendsWithArray() throws Exception {
    String signature = "[Ljava.lang.Class<+[Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;";
    IImportValidator validator = new SimpleImportValidator();
    Assert.assertEquals(SignatureUtility.getTypeReference(signature, validator), "Class<? extends IOutline[]>[]");
    HashSet<String> imports = new HashSet<String>(Arrays.asList(validator.getImportsToCreate()));
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
    Map<String, ITypeGenericMapping> collector = new HashMap<String, ITypeGenericMapping>();
    String superTypeSignature = Signature.createTypeSignature("signature.tests.generic.AbstractClass01<" + String.class.getName() + "," + Integer.class.getName() + ">", true);
    SignatureUtility.resolveGenericParametersInSuperHierarchy(Signature.createTypeSignature("signature.tests.generic.output.TestType", true), null, superTypeSignature, null, collector);
    Assert.assertEquals(Signature.createTypeSignature(String.class.getName(), true), collector.get("signature.tests.generic.IInterface01").getParameterSignature("GENERIC"));
    Assert.assertEquals(Signature.createTypeSignature(Integer.class.getName(), true), collector.get("signature.tests.generic.IInterface02").getParameterSignature("A"));
  }

  @Test
  public void testGenericResolver02() throws Exception {
    Map<String, ITypeGenericMapping> collector = new HashMap<String, ITypeGenericMapping>();
    String superTypeSignature = Signature.createTypeSignature("signature.tests.generic.AbstractClass02<" + Integer.class.getName() + ">", true);
    SignatureUtility.resolveGenericParametersInSuperHierarchy(Signature.createTypeSignature("signature.tests.generic.output.TestType", true), null, superTypeSignature, null, collector);
    Assert.assertEquals(Signature.createTypeSignature(String.class.getName(), true), collector.get("signature.tests.generic.IInterface01").getParameterSignature("GENERIC"));
    Assert.assertEquals(Signature.createTypeSignature(List.class.getName() + "<" + Integer.class.getName() + ">", true), collector.get("signature.tests.generic.IInterface02").getParameterSignature("A"));
  }

  @Test
  public void testGenericResolver03() throws Exception {
    Map<String, ITypeGenericMapping> collector = new HashMap<String, ITypeGenericMapping>();
    String superTypeSignature = Signature.createTypeSignature("org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField<" + Integer.class.getName() + ">", true);
    SignatureUtility.resolveGenericParametersInSuperHierarchy(Signature.createTypeSignature("signature.tests.generic.output.TestType", true), null, superTypeSignature, null, collector);
    Assert.assertEquals(Signature.createTypeSignature(Integer.class.getName(), true), collector.get("org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField").getParameterSignature("T"));
  }

}
