/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertTypeExists;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.junit.jupiter.api.Test;

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
    createFormDataAssertNoCompileErrors("formdata.client.scope.field.AbstractScopeTestGroupBox", ScopeTest::testApiOfAbstractScopeTestGroupBoxData);
    createFormDataAssertNoCompileErrors("formdata.client.scope.orig.ScopeTestForm", ScopeTest::testApiOfScopeTestFormData);
    createFormDataAssertNoCompileErrors("formdata.client.scope.extended.ExtendedScopeTestForm", ScopeTest::testApiOfExtendedScopeTestFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfAbstractScopeTestGroupBoxData(IType abstractScopeTestGroupBoxData) {
    var scoutApi = abstractScopeTestGroupBoxData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(abstractScopeTestGroupBoxData, Flags.AccPublic | Flags.AccAbstract);
    assertHasSuperClass(abstractScopeTestGroupBoxData, scoutApi.AbstractFormFieldData());
    assertEquals(1, abstractScopeTestGroupBoxData.annotations().stream().count(), "annotation count");
    assertAnnotation(abstractScopeTestGroupBoxData, scoutApi.Generated());

    // fields of AbstractScopeTestGroupBoxData
    assertEquals(1, abstractScopeTestGroupBoxData.fields().stream().count(), "field count of 'formdata.shared.scope.field.AbstractScopeTestGroupBoxData'");
    var serialVersionUID = assertFieldExist(abstractScopeTestGroupBoxData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, abstractScopeTestGroupBoxData.methods().stream().count(), "method count of 'formdata.shared.scope.field.AbstractScopeTestGroupBoxData'");
    var getProcess = assertMethodExist(abstractScopeTestGroupBoxData, "getProcess");
    assertMethodReturnType(getProcess, "formdata.shared.scope.field.AbstractScopeTestGroupBoxData$Process");
    assertEquals(0, getProcess.annotations().stream().count(), "annotation count");

    assertEquals(1, abstractScopeTestGroupBoxData.innerTypes().stream().count(), "inner types count of 'AbstractScopeTestGroupBoxData'");
    // type Process
    var process = assertTypeExists(abstractScopeTestGroupBoxData, "Process");
    assertHasFlags(process, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(process, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.util.Set<java.lang.Long>>");
    assertEquals(0, process.annotations().stream().count(), "annotation count");

    // fields of Process
    assertEquals(1, process.fields().stream().count(), "field count of 'formdata.shared.scope.field.AbstractScopeTestGroupBoxData$Process'");
    var serialVersionUID1 = assertFieldExist(process, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, process.methods().stream().count(), "method count of 'formdata.shared.scope.field.AbstractScopeTestGroupBoxData$Process'");

    assertEquals(0, process.innerTypes().stream().count(), "inner types count of 'Process'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfScopeTestFormData(IType scopeTestFormData) {
    var scoutApi = scopeTestFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(scopeTestFormData, Flags.AccPublic);
    assertHasSuperClass(scopeTestFormData, scoutApi.AbstractFormData());
    assertEquals(1, scopeTestFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(scopeTestFormData, scoutApi.Generated());

    // fields of ScopeTestFormData
    assertEquals(1, scopeTestFormData.fields().stream().count(), "field count of 'formdata.shared.scope.orig.ScopeTestFormData'");
    var serialVersionUID = assertFieldExist(scopeTestFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, scopeTestFormData.methods().stream().count(), "method count of 'formdata.shared.scope.orig.ScopeTestFormData'");
    var getProcessesBox = assertMethodExist(scopeTestFormData, "getProcessesBox");
    assertMethodReturnType(getProcessesBox, "formdata.shared.scope.orig.ScopeTestFormData$ProcessesBox");
    assertEquals(0, getProcessesBox.annotations().stream().count(), "annotation count");

    assertEquals(1, scopeTestFormData.innerTypes().stream().count(), "inner types count of 'ScopeTestFormData'");
    // type ProcessesBox
    var processesBox = assertTypeExists(scopeTestFormData, "ProcessesBox");
    assertHasFlags(processesBox, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(processesBox, "formdata.shared.scope.field.AbstractScopeTestGroupBoxData");
    assertEquals(0, processesBox.annotations().stream().count(), "annotation count");

    // fields of ProcessesBox
    assertEquals(1, processesBox.fields().stream().count(), "field count of 'formdata.shared.scope.orig.ScopeTestFormData$ProcessesBox'");
    var serialVersionUID1 = assertFieldExist(processesBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, processesBox.methods().stream().count(), "method count of 'formdata.shared.scope.orig.ScopeTestFormData$ProcessesBox'");

    assertEquals(0, processesBox.innerTypes().stream().count(), "inner types count of 'ProcessesBox'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfExtendedScopeTestFormData(IType extendedScopeTestFormData) {
    var scoutApi = extendedScopeTestFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(extendedScopeTestFormData, Flags.AccPublic);
    assertHasSuperClass(extendedScopeTestFormData, "formdata.shared.scope.orig.ScopeTestFormData");
    assertEquals(2, extendedScopeTestFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(extendedScopeTestFormData, scoutApi.Replace());
    assertAnnotation(extendedScopeTestFormData, scoutApi.Generated());

    // fields of ExtendedScopeTestFormData
    assertEquals(1, extendedScopeTestFormData.fields().stream().count(), "field count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData'");
    var serialVersionUID = assertFieldExist(extendedScopeTestFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(2, extendedScopeTestFormData.methods().stream().count(), "method count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData'");
    var getAnliegenBox = assertMethodExist(extendedScopeTestFormData, "getAnliegenBox");
    assertMethodReturnType(getAnliegenBox, "formdata.shared.scope.extended.ExtendedScopeTestFormData$AnliegenBox");
    assertEquals(0, getAnliegenBox.annotations().stream().count(), "annotation count");
    var getExtendedProcess = assertMethodExist(extendedScopeTestFormData, "getExtendedProcess");
    assertMethodReturnType(getExtendedProcess, "formdata.shared.scope.extended.ExtendedScopeTestFormData$ExtendedProcess");
    assertEquals(0, getExtendedProcess.annotations().stream().count(), "annotation count");

    assertEquals(2, extendedScopeTestFormData.innerTypes().stream().count(), "inner types count of 'ExtendedScopeTestFormData'");
    // type AnliegenBox
    var anliegenBox = assertTypeExists(extendedScopeTestFormData, "AnliegenBox");
    assertHasFlags(anliegenBox, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(anliegenBox, "formdata.shared.scope.orig.ScopeTestFormData$ProcessesBox");
    assertEquals(1, anliegenBox.annotations().stream().count(), "annotation count");
    assertAnnotation(anliegenBox, scoutApi.Replace());

    // fields of AnliegenBox
    assertEquals(1, anliegenBox.fields().stream().count(), "field count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData$AnliegenBox'");
    var serialVersionUID1 = assertFieldExist(anliegenBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, anliegenBox.methods().stream().count(), "method count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData$AnliegenBox'");

    assertEquals(0, anliegenBox.innerTypes().stream().count(), "inner types count of 'AnliegenBox'");
    // type ExtendedProcess
    var extendedProcess = assertTypeExists(extendedScopeTestFormData, "ExtendedProcess");
    assertHasFlags(extendedProcess, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(extendedProcess, "formdata.shared.scope.field.AbstractScopeTestGroupBoxData$Process");
    assertEquals(1, extendedProcess.annotations().stream().count(), "annotation count");
    assertAnnotation(extendedProcess, scoutApi.Replace());

    // fields of ExtendedProcess
    assertEquals(1, extendedProcess.fields().stream().count(), "field count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData$ExtendedProcess'");
    var serialVersionUID2 = assertFieldExist(extendedProcess, "serialVersionUID");
    assertHasFlags(serialVersionUID2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");

    assertEquals(0, extendedProcess.methods().stream().count(), "method count of 'formdata.shared.scope.extended.ExtendedScopeTestFormData$ExtendedProcess'");

    assertEquals(0, extendedProcess.innerTypes().stream().count(), "inner types count of 'ExtendedProcess'");
  }
}
