/**
 *
 */
package org.eclipse.scout.sdk.test.util;

import java.util.Arrays;
import java.util.HashSet;

import junit.framework.Assert;

import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.jdt.signature.SimpleImportValidator;
import org.junit.Test;

/**
 * <h3>{@link ScoutSignatureTest}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 23.02.2011
 */
public class ScoutSignatureTest {

  @Test
  public void testSimpleSignature() throws Exception {
    String signature = "Ljava.lang.String;";
    IImportValidator validator = new SimpleImportValidator();
    Assert.assertEquals(ScoutSdkUtility.getSimpleTypeRefName(signature, validator), "String");
  }

  @Test
  public void testGenericByteArray() throws Exception {
    String signature = "L" + RuntimeClasses.AbstractPropertyData + "<[" + Signature.SIG_BYTE + ">;";
    IImportValidator validator = new SimpleImportValidator();
    Assert.assertEquals(ScoutSdkUtility.getSimpleTypeRefName(signature, validator), "AbstractPropertyData<byte[]>");
  }

  @Test
  public void testGenericOfAny() throws Exception {
    String signature = "L" + RuntimeClasses.AbstractPropertyData + "<*>;";
    IImportValidator validator = new SimpleImportValidator();
    Assert.assertEquals(ScoutSdkUtility.getSimpleTypeRefName(signature, validator), "AbstractPropertyData<?>");
    HashSet<String> imports = new HashSet<String>(Arrays.asList(validator.getImportsToCreate()));
    Assert.assertTrue(imports.remove(RuntimeClasses.AbstractPropertyData));
    Assert.assertTrue(imports.isEmpty());
  }

  @Test
  public void testGenericExtendsWithArray() throws Exception {
    String signature = "[Ljava.lang.Class<+[Lcom.bsiag.scout.client.ui.desktop.outline.IOutline;>;";
    IImportValidator validator = new SimpleImportValidator();
    Assert.assertEquals(ScoutSdkUtility.getSimpleTypeRefName(signature, validator), "Class<? extends IOutline[]>[]");
    HashSet<String> imports = new HashSet<String>(Arrays.asList(validator.getImportsToCreate()));
    Assert.assertTrue(imports.remove("com.bsiag.scout.client.ui.desktop.outline.IOutline"));
    Assert.assertTrue(imports.isEmpty());
  }

}
