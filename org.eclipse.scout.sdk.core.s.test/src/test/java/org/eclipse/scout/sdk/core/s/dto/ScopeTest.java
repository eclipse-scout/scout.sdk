/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.scope.extended.ExtendedScopeTestForm;
import formdata.client.scope.field.AbstractScopeTestGroupBox;
import formdata.client.scope.orig.ScopeTestForm;

/**
 * <h3>{@link ScopeTest}</h3>
 *
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
    createFormDataAssertNoCompileErrors(AbstractScopeTestGroupBox.class.getName(), ScopeTest::testApiOfAbstractScopeTestGroupBoxData);
    createFormDataAssertNoCompileErrors(ScopeTestForm.class.getName(), ScopeTest::testApiOfScopeTestFormData);
    createFormDataAssertNoCompileErrors(ExtendedScopeTestForm.class.getName(), ScopeTest::testApiOfExtendedScopeTestFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfAbstractScopeTestGroupBoxData(IType abstractScopeTestGroupBoxData) {
    assertHasFlags(abstractScopeTestGroupBoxData, 1025);
    assertHasSuperClass(abstractScopeTestGroupBoxData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData");
    assertEquals(1, abstractScopeTestGroupBoxData.annotations().stream().count(), "annotation count");
    assertAnnotation(abstractScopeTestGroupBoxData, "javax.annotation.Generated");

    // fields of AbstractScopeTestGroupBoxData
    assertEquals(1, abstractScopeTestGroupBoxData.fields().stream().count(), "field count of 'formdata.shared.scope.field.AbstractScopeTestGroupBoxData'");
    var serialVersionUID = assertFieldExist(abstractScopeTestGroupBoxData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, abstractScopeTestGroupBoxData.methods().stream().count(), "method count of 'formdata.shared.scope.field.AbstractScopeTestGroupBoxData'");
    var getProcess = assertMethodExist(abstractScopeTestGroupBoxData, "getProcess", new String[]{});
    assertMethodReturnType(getProcess, "formdata.shared.scope.field.AbstractScopeTestGroupBoxData$Process");
    assertEquals(0, getProcess.annotations().stream().count(), "annotation count");

    assertEquals(1, abstractScopeTestGroupBoxData.innerTypes().stream().count(), "inner types count of 'AbstractScopeTestGroupBoxData'");
    // type Process
    var process = assertTypeExists(abstractScopeTestGroupBoxData, "Process");
    assertHasFlags(process, 9);
    assertHasSuperClass(process, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.util.Set<java.lang.Long>>");
    assertEquals(0, process.annotations().stream().count(), "annotation count");

    // fields of Process
    assertEquals(1, process.fields().stream().count(), "field count of 'formdata.shared.scope.field.AbstractScopeTestGroupBoxData$Process'");
    var serialVersionUID1 = assertFieldExist(process, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, process.methods().stream().count(), "method count of 'formdata.shared.scope.field.AbstractScopeTestGroupBoxData$Process'");

    assertEquals(0, process.innerTypes().stream().count(), "inner types count of 'Process'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfScopeTestFormData(IType scopeTestFormData) {
    assertHasFlags(scopeTestFormData, 1);
    assertHasSuperClass(scopeTestFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");
    assertEquals(1, scopeTestFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(scopeTestFormData, "javax.annotation.Generated");

    // fields of ScopeTestFormData
    assertEquals(1, scopeTestFormData.fields().stream().count(), "field count of 'formdata.shared.scope.orig.ScopeTestFormData'");
    var serialVersionUID = assertFieldExist(scopeTestFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, scopeTestFormData.methods().stream().count(), "method count of 'formdata.shared.scope.orig.ScopeTestFormData'");
    var getProcessesBox = assertMethodExist(scopeTestFormData, "getProcessesBox", new String[]{});
    assertMethodReturnType(getProcessesBox, "formdata.shared.scope.orig.ScopeTestFormData$ProcessesBox");
    assertEquals(0, getProcessesBox.annotations().stream().count(), "annotation count");

    assertEquals(1, scopeTestFormData.innerTypes().stream().count(), "inner types count of 'ScopeTestFormData'");
    // type ProcessesBox
    var processesBox = assertTypeExists(scopeTestFormData, "ProcessesBox");
    assertHasFlags(processesBox, 9);
    assertHasSuperClass(processesBox, "formdata.shared.scope.field.AbstractScopeTestGroupBoxData");
    assertEquals(0, processesBox.annotations().stream().count(), "annotation count");

    // fields of ProcessesBox
    assertEquals(1, processesBox.fields().stream().count(), "field count of 'formdata.shared.scope.orig.ScopeTestFormData$ProcessesBox'");
    var serialVersionUID1 = assertFieldExist(processesBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, processesBox.methods().stream().count(), "method count of 'formdata.shared.scope.orig.ScopeTestFormData$ProcessesBox'");

    assertEquals(0, processesBox.innerTypes().stream().count(), "inner types count of 'ProcessesBox'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfExtendedScopeTestFormData(IType extendedScopeTestFormData) {
    assertHasFlags(extendedScopeTestFormData, 1);
    assertHasSuperClass(extendedScopeTestFormData, "formdata.shared.scope.orig.ScopeTestFormData");
    assertEquals(2, extendedScopeTestFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(extendedScopeTestFormData, "org.eclipse.scout.rt.platform.Replace");
    assertAnnotation(extendedScopeTestFormData, "javax.annotation.Generated");

    // fields of ExtendedScopeTestFormData
    assertEquals(1, extendedScopeTestFormData.fields().stream().count(), "field count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData'");
    var serialVersionUID = assertFieldExist(extendedScopeTestFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(2, extendedScopeTestFormData.methods().stream().count(), "method count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData'");
    var getAnliegenBox = assertMethodExist(extendedScopeTestFormData, "getAnliegenBox", new String[]{});
    assertMethodReturnType(getAnliegenBox, "formdata.shared.scope.extended.ExtendedScopeTestFormData$AnliegenBox");
    assertEquals(0, getAnliegenBox.annotations().stream().count(), "annotation count");
    var getExtendedProcess = assertMethodExist(extendedScopeTestFormData, "getExtendedProcess", new String[]{});
    assertMethodReturnType(getExtendedProcess, "formdata.shared.scope.extended.ExtendedScopeTestFormData$ExtendedProcess");
    assertEquals(0, getExtendedProcess.annotations().stream().count(), "annotation count");

    assertEquals(2, extendedScopeTestFormData.innerTypes().stream().count(), "inner types count of 'ExtendedScopeTestFormData'");
    // type AnliegenBox
    var anliegenBox = assertTypeExists(extendedScopeTestFormData, "AnliegenBox");
    assertHasFlags(anliegenBox, 9);
    assertHasSuperClass(anliegenBox, "formdata.shared.scope.orig.ScopeTestFormData$ProcessesBox");
    assertEquals(1, anliegenBox.annotations().stream().count(), "annotation count");
    assertAnnotation(anliegenBox, "org.eclipse.scout.rt.platform.Replace");

    // fields of AnliegenBox
    assertEquals(1, anliegenBox.fields().stream().count(), "field count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData$AnliegenBox'");
    var serialVersionUID1 = assertFieldExist(anliegenBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, anliegenBox.methods().stream().count(), "method count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData$AnliegenBox'");

    assertEquals(0, anliegenBox.innerTypes().stream().count(), "inner types count of 'AnliegenBox'");
    // type ExtendedProcess
    var extendedProcess = assertTypeExists(extendedScopeTestFormData, "ExtendedProcess");
    assertHasFlags(extendedProcess, 9);
    assertHasSuperClass(extendedProcess, "formdata.shared.scope.field.AbstractScopeTestGroupBoxData$Process");
    assertEquals(1, extendedProcess.annotations().stream().count(), "annotation count");
    assertAnnotation(extendedProcess, "org.eclipse.scout.rt.platform.Replace");

    // fields of ExtendedProcess
    assertEquals(1, extendedProcess.fields().stream().count(), "field count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData$ExtendedProcess'");
    var serialVersionUID2 = assertFieldExist(extendedProcess, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");

    assertEquals(0, extendedProcess.methods().stream().count(), "method count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData$ExtendedProcess'");

    assertEquals(0, extendedProcess.innerTypes().stream().count(), "inner types count of 'ExtendedProcess'");
  }

}
