/**
 *
 */
package org.eclipse.scout.sdk.internal.test.util.signature;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.FullyQuallifiedValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.signature.SimpleImportValidator;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link SignatureUtilityTest}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 23.02.2011
 */
public class SignatureUtilityTest {

  private static final String LONG_SIGNATURE = SignatureCache.createTypeSignature("java.lang.Long");

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

  @Test(timeout = 200L)
  public void testClassParameterized() throws Exception {
    String signature = "[Ljava.lang.Class<+[Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;";
    FullyQuallifiedValidator validator = new FullyQuallifiedValidator();

    String result = SignatureUtility.getTypeReference(signature, validator);
    Assert.assertEquals("java.lang.Class<? extends com.bsiag.scout.client.ui.desktop.outline.IOutline[]>[]", result);
  }

  @Test(timeout = 4L)
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

}
