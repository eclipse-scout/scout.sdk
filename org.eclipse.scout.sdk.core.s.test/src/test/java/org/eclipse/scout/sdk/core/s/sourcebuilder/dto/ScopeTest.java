/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.sourcebuilder.dto;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

import formdata.client.scope.extended.ExtendedScopeTestForm;
import formdata.client.scope.field.AbstractScopeTestGroupBox;
import formdata.client.scope.orig.ScopeTestForm;

/**
 * <h3>{@link ScopeTest}</h3>
 *
 * @author Matthias Villiger
 * @since 6.1.0
 */
public class ScopeTest {

  /**
   * Tests that the scope is correctly reset for inner types of super classes. See commit
   * 82988fb5af1e77cb5032b9b0b82f29f0b53401b9<br>
   * This test may fail with a compile error (not implemented methods) if the wrong super class is used!
   */
  @Test
  public void testScope() {
    IType dtoField = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(AbstractScopeTestGroupBox.class.getName());
    testApiOfAbstractScopeTestGroupBoxData(dtoField);

    IType dtoFormOrig = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(ScopeTestForm.class.getName());
    testApiOfScopeTestFormData(dtoFormOrig);

    IType dtoFormExtended = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(ExtendedScopeTestForm.class.getName());
    testApiOfExtendedScopeTestFormData(dtoFormExtended);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfAbstractScopeTestGroupBoxData(IType abstractScopeTestGroupBoxData) {
    SdkAssert.assertHasFlags(abstractScopeTestGroupBoxData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractScopeTestGroupBoxData, "Lorg.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;");
    Assert.assertEquals("annotation count", 1, abstractScopeTestGroupBoxData.annotations().list().size());
    SdkAssert.assertAnnotation(abstractScopeTestGroupBoxData, "javax.annotation.Generated");

    // fields of AbstractScopeTestGroupBoxData
    Assert.assertEquals("field count of 'formdata.shared.scope.field.AbstractScopeTestGroupBoxData'", 1, abstractScopeTestGroupBoxData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractScopeTestGroupBoxData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.scope.field.AbstractScopeTestGroupBoxData'", 1, abstractScopeTestGroupBoxData.methods().list().size());
    IMethod getProcess = SdkAssert.assertMethodExist(abstractScopeTestGroupBoxData, "getProcess", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getProcess, "Lformdata.shared.scope.field.AbstractScopeTestGroupBoxData$Process;");
    Assert.assertEquals("annotation count", 0, getProcess.annotations().list().size());

    Assert.assertEquals("inner types count of 'AbstractScopeTestGroupBoxData'", 1, abstractScopeTestGroupBoxData.innerTypes().list().size());
    // type Process
    IType process = SdkAssert.assertTypeExists(abstractScopeTestGroupBoxData, "Process");
    SdkAssert.assertHasFlags(process, 9);
    SdkAssert.assertHasSuperTypeSignature(process, "Lorg.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<Ljava.util.Set<Ljava.lang.Long;>;>;");
    Assert.assertEquals("annotation count", 0, process.annotations().list().size());

    // fields of Process
    Assert.assertEquals("field count of 'formdata.shared.scope.field.AbstractScopeTestGroupBoxData$Process'", 1, process.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(process, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID1.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.scope.field.AbstractScopeTestGroupBoxData$Process'", 0, process.methods().list().size());

    Assert.assertEquals("inner types count of 'Process'", 0, process.innerTypes().list().size());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfScopeTestFormData(IType scopeTestFormData) {
    SdkAssert.assertHasFlags(scopeTestFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(scopeTestFormData, "Lorg.eclipse.scout.rt.shared.data.form.AbstractFormData;");
    Assert.assertEquals("annotation count", 1, scopeTestFormData.annotations().list().size());
    SdkAssert.assertAnnotation(scopeTestFormData, "javax.annotation.Generated");

    // fields of ScopeTestFormData
    Assert.assertEquals("field count of 'formdata.shared.scope.orig.ScopeTestFormData'", 1, scopeTestFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(scopeTestFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.scope.orig.ScopeTestFormData'", 1, scopeTestFormData.methods().list().size());
    IMethod getProcessesBox = SdkAssert.assertMethodExist(scopeTestFormData, "getProcessesBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getProcessesBox, "Lformdata.shared.scope.orig.ScopeTestFormData$ProcessesBox;");
    Assert.assertEquals("annotation count", 0, getProcessesBox.annotations().list().size());

    Assert.assertEquals("inner types count of 'ScopeTestFormData'", 1, scopeTestFormData.innerTypes().list().size());
    // type ProcessesBox
    IType processesBox = SdkAssert.assertTypeExists(scopeTestFormData, "ProcessesBox");
    SdkAssert.assertHasFlags(processesBox, 9);
    SdkAssert.assertHasSuperTypeSignature(processesBox, "Lformdata.shared.scope.field.AbstractScopeTestGroupBoxData;");
    Assert.assertEquals("annotation count", 0, processesBox.annotations().list().size());

    // fields of ProcessesBox
    Assert.assertEquals("field count of 'formdata.shared.scope.orig.ScopeTestFormData$ProcessesBox'", 1, processesBox.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(processesBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID1.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.scope.orig.ScopeTestFormData$ProcessesBox'", 0, processesBox.methods().list().size());

    Assert.assertEquals("inner types count of 'ProcessesBox'", 0, processesBox.innerTypes().list().size());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfExtendedScopeTestFormData(IType extendedScopeTestFormData) {
    SdkAssert.assertHasFlags(extendedScopeTestFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(extendedScopeTestFormData, "Lformdata.shared.scope.orig.ScopeTestFormData;");
    Assert.assertEquals("annotation count", 2, extendedScopeTestFormData.annotations().list().size());
    SdkAssert.assertAnnotation(extendedScopeTestFormData, "org.eclipse.scout.rt.platform.Replace");
    SdkAssert.assertAnnotation(extendedScopeTestFormData, "javax.annotation.Generated");

    // fields of ExtendedScopeTestFormData
    Assert.assertEquals("field count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData'", 1, extendedScopeTestFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(extendedScopeTestFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData'", 2, extendedScopeTestFormData.methods().list().size());
    IMethod getAnliegenBox = SdkAssert.assertMethodExist(extendedScopeTestFormData, "getAnliegenBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getAnliegenBox, "Lformdata.shared.scope.extended.ExtendedScopeTestFormData$AnliegenBox;");
    Assert.assertEquals("annotation count", 0, getAnliegenBox.annotations().list().size());
    IMethod getExtendedProcess = SdkAssert.assertMethodExist(extendedScopeTestFormData, "getExtendedProcess", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getExtendedProcess, "Lformdata.shared.scope.extended.ExtendedScopeTestFormData$ExtendedProcess;");
    Assert.assertEquals("annotation count", 0, getExtendedProcess.annotations().list().size());

    Assert.assertEquals("inner types count of 'ExtendedScopeTestFormData'", 2, extendedScopeTestFormData.innerTypes().list().size());
    // type AnliegenBox
    IType anliegenBox = SdkAssert.assertTypeExists(extendedScopeTestFormData, "AnliegenBox");
    SdkAssert.assertHasFlags(anliegenBox, 9);
    SdkAssert.assertHasSuperTypeSignature(anliegenBox, "Lformdata.shared.scope.orig.ScopeTestFormData$ProcessesBox;");
    Assert.assertEquals("annotation count", 1, anliegenBox.annotations().list().size());
    SdkAssert.assertAnnotation(anliegenBox, "org.eclipse.scout.rt.platform.Replace");

    // fields of AnliegenBox
    Assert.assertEquals("field count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData$AnliegenBox'", 1, anliegenBox.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(anliegenBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID1.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData$AnliegenBox'", 0, anliegenBox.methods().list().size());

    Assert.assertEquals("inner types count of 'AnliegenBox'", 0, anliegenBox.innerTypes().list().size());
    // type ExtendedProcess
    IType extendedProcess = SdkAssert.assertTypeExists(extendedScopeTestFormData, "ExtendedProcess");
    SdkAssert.assertHasFlags(extendedProcess, 9);
    SdkAssert.assertHasSuperTypeSignature(extendedProcess, "Lformdata.shared.scope.field.AbstractScopeTestGroupBoxData$Process;");
    Assert.assertEquals("annotation count", 1, extendedProcess.annotations().list().size());
    SdkAssert.assertAnnotation(extendedProcess, "org.eclipse.scout.rt.platform.Replace");

    // fields of ExtendedProcess
    Assert.assertEquals("field count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData$ExtendedProcess'", 1, extendedProcess.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(extendedProcess, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID2.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData$ExtendedProcess'", 0, extendedProcess.methods().list().size());

    Assert.assertEquals("inner types count of 'ExtendedProcess'", 0, extendedProcess.innerTypes().list().size());
  }

}
