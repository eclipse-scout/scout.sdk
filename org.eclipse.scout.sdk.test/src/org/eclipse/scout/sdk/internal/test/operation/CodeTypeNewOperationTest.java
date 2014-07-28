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
package org.eclipse.scout.sdk.internal.test.operation;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.CodeTypeNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link CodeTypeNewOperationTest}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class CodeTypeNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewCodeType() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    IScoutBundle sharedBundle = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(getSharedJavaProject());
    CodeTypeNewOperation codeTypeOp = new CodeTypeNewOperation("CodeType01", DefaultTargetPackage.get(sharedBundle, IDefaultTargetPackage.SHARED_SERVICES_CODE), sharedBundle.getJavaProject());
    codeTypeOp.setSuperTypeSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType<" + Long.class.getName() + ", " + Long.class.getName() + ">"));
    // nls
    INlsProject nlsProject = ScoutTypeUtility.findNlsProject(getSharedJavaProject());
    INlsEntry entry = nlsProject.getEntry("Text02");
    codeTypeOp.setNlsEntry(entry);
    codeTypeOp.setFormatSource(true);
    executeBuildAssertNoCompileErrors(codeTypeOp);

    SdkAssert.assertExist(codeTypeOp.getCreatedType());
    testApiOfCodeType01();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfCodeType01() throws Exception {
    // type CodeType01
    IType codeType01 = SdkAssert.assertTypeExists("services.code.CodeType01");
    SdkAssert.assertHasFlags(codeType01, 1);
    SdkAssert.assertHasSuperTypeSignature(codeType01, "QAbstractCodeType<QLong;QLong;>;");

    // fields of CodeType01
    SdkAssert.assertEquals("field count of 'CodeType01'", 2, codeType01.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(codeType01, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    IField iD = SdkAssert.assertFieldExist(codeType01, "ID");
    SdkAssert.assertHasFlags(iD, 25);
    SdkAssert.assertFieldSignature(iD, "QLong;");

    SdkAssert.assertEquals("method count of 'CodeType01'", 3, codeType01.getMethods().length);
    IMethod codeType011 = SdkAssert.assertMethodExist(codeType01, "CodeType01", new String[]{});
    SdkAssert.assertTrue(codeType011.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(codeType011, "V");
    IMethod getConfiguredText = SdkAssert.assertMethodExist(codeType01, "getConfiguredText", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getConfiguredText, "QString;");
    SdkAssert.assertAnnotation(getConfiguredText, "java.lang.Override");
    IMethod getId = SdkAssert.assertMethodExist(codeType01, "getId", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getId, "QLong;");
    SdkAssert.assertAnnotation(getId, "java.lang.Override");

    SdkAssert.assertEquals("inner types count of 'CodeType01'", 0, codeType01.getTypes().length);
  }
}
