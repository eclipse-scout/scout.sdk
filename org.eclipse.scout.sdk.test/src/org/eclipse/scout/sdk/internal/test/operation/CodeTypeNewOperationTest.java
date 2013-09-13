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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.CodeTypeNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link CodeTypeNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2013
 */
public class CodeTypeNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewCodeType() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    CodeTypeNewOperation codeTypeOp = new CodeTypeNewOperation();
    IScoutBundle sharedBundle = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(getSharedJavaProject());
    codeTypeOp.setSharedBundle(sharedBundle);
    codeTypeOp.setTypeName("CodeType01");
    codeTypeOp.setPackageName(DefaultTargetPackage.get(sharedBundle, IDefaultTargetPackage.SHARED_SERVICES_CODE));
    codeTypeOp.setSuperTypeSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType<" + Long.class.getName() + ">"));
    codeTypeOp.setGenericTypeSignature(Signature.createTypeSignature(Long.class.getName(), true));
    // nls
    INlsProject nlsProject = ScoutTypeUtility.findNlsProject(getSharedJavaProject());
    INlsEntry entry = nlsProject.getEntry("Text02");
    codeTypeOp.setNlsEntry(entry);
    codeTypeOp.setFormatSource(true);
    executeBuildAssertNoCompileErrors(codeTypeOp);
    IType codeType = codeTypeOp.getCreatedType();
    SdkAssert.assertExist(codeType);
    SdkAssert.assertPublic(codeType).assertNoMoreFlags();
    IMethod getIdMethod = SdkAssert.assertMethodExist(codeType, "getId");
    SdkAssert.assertPublic(getIdMethod).assertNoMoreFlags();
    IField idField = SdkAssert.assertFieldExist(codeType, "ID");
    SdkAssert.assertPublic(idField).assertFinal().assertStatic().assertNoMoreFlags();
    SdkAssert.assertSerialVersionUidExists(codeType);

  }
}
