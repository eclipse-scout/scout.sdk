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
 * @author aho
 * @since 3.8.0 14.03.2013
 */
public class ComposerEntityNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewEntity() throws Exception {
    IType composerField = TypeUtility.getType("sample.client.person.PersonForm.MainBox.ComposerField");
    ComposerEntityNewOperation composerEntityNewOp = new ComposerEntityNewOperation("NewEntity", composerField);
    composerEntityNewOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity", true));
    executeBuildAssertNoCompileErrors(composerEntityNewOp);
    IType entity = composerEntityNewOp.getCreatedEntry();
    SdkAssert.assertExist(entity);
    SdkAssert.assertPublic(entity).assertNoMoreFlags();
    SdkAssert.assertSerialVersionUidExists(entity);

    IType[] entities = TypeUtility.getInnerTypesOrdered(composerField, TypeUtility.getType(RuntimeClasses.IDataModelEntity), ScoutTypeComparators.getOrderAnnotationComparator());
    SdkAssert.assertEquals(2, entities.length);
    SdkAssert.assertEquals(composerEntityNewOp.getTypeName(), entities[1].getElementName());

    // clean up
    JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
    delOp.addMember(entity);
    executeBuildAssertNoCompileErrors(delOp);
    entities = TypeUtility.getInnerTypesOrdered(composerField, TypeUtility.getType(RuntimeClasses.IDataModelEntity), ScoutTypeComparators.getOrderAnnotationComparator());
    SdkAssert.assertEquals(1, entities.length);
  }

}
