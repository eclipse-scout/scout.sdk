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
package org.eclipse.scout.sdk.internal.test.operation.form.fields.composer;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.form.field.composer.ComposerEntityNewOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.junit.Test;

/**
 * <h3>{@link ComposerEntityNewOperationTest}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class ComposerEntityNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewEntity() throws Exception {
    IType composerField = TypeUtility.getType("sample.client.person.PersonForm.MainBox.ComposerField");
    ComposerEntityNewOperation composerEntityNewOp = new ComposerEntityNewOperation("NewEntity", composerField);
    composerEntityNewOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity", true));
    executeBuildAssertNoCompileErrors(composerEntityNewOp);
    IType entity = composerEntityNewOp.getCreatedType();
    SdkAssert.assertExist(entity);

    testApiOfNewEntity();

    // clean up
    JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
    delOp.addMember(entity);
    executeBuildAssertNoCompileErrors(delOp);
    IType[] entities = TypeUtility.getInnerTypesOrdered(composerField, TypeUtility.getType(RuntimeClasses.IDataModelEntity), ScoutTypeComparators.getTypeNameComparator());
    SdkAssert.assertEquals(1, entities.length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfNewEntity() throws Exception {
    // type NewEntity
    IType newEntity = SdkAssert.assertTypeExists("sample.client.person.PersonForm$MainBox$ComposerField$NewEntity");
    SdkAssert.assertHasFlags(newEntity, 1);
    SdkAssert.assertHasSuperTypeSignature(newEntity, "QAbstractDataModelEntity;");

    // fields of NewEntity
    SdkAssert.assertEquals("field count of 'NewEntity'", 1, newEntity.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(newEntity, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'NewEntity'", 0, newEntity.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'NewEntity'", 0, newEntity.getTypes().length);
  }
}
