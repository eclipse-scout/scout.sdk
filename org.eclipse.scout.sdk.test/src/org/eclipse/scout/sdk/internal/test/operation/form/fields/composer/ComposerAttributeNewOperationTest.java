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
import org.eclipse.scout.sdk.operation.form.field.composer.ComposerAttributeNewOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.junit.Test;

/**
 * <h3>{@link ComposerAttributeNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2013
 */
public class ComposerAttributeNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewAttribute() throws Exception {
    IType composerField = TypeUtility.getType("sample.client.person.PersonForm.MainBox.ComposerField");
    ComposerAttributeNewOperation composerAttributeNewOp = new ComposerAttributeNewOperation("NewAttribute", composerField);
    composerAttributeNewOp.setSuperTypeSignature(Signature.createTypeSignature("org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute", true));
    executeBuildAssertNoCompileErrors(composerAttributeNewOp);
    IType attribute = composerAttributeNewOp.getCreatedAttribute();
    SdkAssert.assertExist(attribute);
    SdkAssert.assertPublic(attribute).assertNoMoreFlags();
    SdkAssert.assertSerialVersionUidExists(attribute);

    IType[] attributes = TypeUtility.getInnerTypesOrdered(composerField, TypeUtility.getType(RuntimeClasses.IDataModelAttribute), ScoutTypeComparators.getOrderAnnotationComparator());
    SdkAssert.assertEquals(2, attributes.length);
    SdkAssert.assertEquals(composerAttributeNewOp.getTypeName(), attributes[1].getElementName());

    // clean up
    JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
    delOp.addMember(attribute);
    executeBuildAssertNoCompileErrors(delOp);
    attributes = TypeUtility.getInnerTypesOrdered(composerField, TypeUtility.getType(RuntimeClasses.IDataModelAttribute), ScoutTypeComparators.getOrderAnnotationComparator());
    SdkAssert.assertEquals(1, attributes.length);
  }

}
