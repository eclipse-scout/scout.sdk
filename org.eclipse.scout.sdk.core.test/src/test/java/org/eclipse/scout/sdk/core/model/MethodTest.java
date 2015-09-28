/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model;

import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class MethodTest {

  @Test
  public void testChildClassMethods() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    Assert.assertEquals(3, childClassType.methods().list().size());

    // constructor
    IMethod constr = childClassType.methods().first();
    Assert.assertEquals(childClassType, constr.declaringType());
    Assert.assertEquals(0, constr.exceptionTypes().size());
    Assert.assertEquals(Flags.AccPublic, constr.flags());
    Assert.assertEquals(childClassType.elementName(), constr.elementName());
    Assert.assertEquals(0, constr.parameters().list().size());
    Assert.assertNull(constr.returnType());
    Assert.assertTrue(constr.isConstructor());
    Assert.assertEquals(0, constr.annotations().list().size());

    // methodInChildClass
    IMethod methodInChildClass = childClassType.methods().list().get(1);
    Assert.assertEquals(childClassType, methodInChildClass.declaringType());
    Assert.assertEquals(1, methodInChildClass.exceptionTypes().size());
    Assert.assertEquals(IOException.class.getName(), methodInChildClass.exceptionTypes().get(0).name());
    Assert.assertEquals(Flags.AccProtected | Flags.AccSynchronized, methodInChildClass.flags());
    Assert.assertEquals("methodInChildClass", methodInChildClass.elementName());
    Assert.assertEquals(2, methodInChildClass.parameters().list().size());
    Assert.assertEquals("boolean", methodInChildClass.returnType().leafComponentType().name());
    Assert.assertTrue(methodInChildClass.returnType().isArray());
    Assert.assertFalse(methodInChildClass.isConstructor());
    Assert.assertEquals(1, methodInChildClass.annotations().list().size());

    // firstCase
    IMethod firstCase = childClassType.methods().list().get(2);
    Assert.assertEquals(childClassType, firstCase.declaringType());
    Assert.assertEquals(0, firstCase.exceptionTypes().size());
    Assert.assertEquals(Flags.AccPrivate, firstCase.flags());
    Assert.assertEquals("firstCase", firstCase.elementName());
    Assert.assertEquals(0, firstCase.parameters().list().size());
    Assert.assertEquals(Set.class.getName(), firstCase.returnType().leafComponentType().name());
    Assert.assertFalse(firstCase.isConstructor());
    Assert.assertEquals(1, firstCase.annotations().list().size());
  }

  @Test
  public void testToString() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);
    Assert.assertFalse(StringUtils.isBlank(childClassType.methods().list().get(1).toString()));
  }

  @Test
  public void testBaseClassMethods() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    Assert.assertEquals(2, baseClassType.methods().list().size());

    // methodInBaseClass
    IMethod methodInBaseClass = baseClassType.methods().first();
    Assert.assertEquals(baseClassType, methodInBaseClass.declaringType());
    Assert.assertEquals(2, methodInBaseClass.exceptionTypes().size());
    Assert.assertEquals(IOError.class.getName(), methodInBaseClass.exceptionTypes().get(0).name());
    Assert.assertEquals(FileNotFoundException.class.getName(), methodInBaseClass.exceptionTypes().get(1).name());
    Assert.assertEquals(Flags.AccProtected, methodInBaseClass.flags());
    Assert.assertEquals("methodInBaseClass", methodInBaseClass.elementName());
    Assert.assertEquals(1, methodInBaseClass.parameters().list().size());
    Assert.assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), methodInBaseClass.returnType().leafComponentType().name());
    Assert.assertEquals(2, methodInBaseClass.returnType().arrayDimension());
    Assert.assertTrue(methodInBaseClass.returnType().isArray());
    Assert.assertFalse(methodInBaseClass.isConstructor());
    Assert.assertEquals(2, methodInBaseClass.annotations().list().size());

    // method2InBaseClass
    IMethod method2InBaseClass = baseClassType.methods().list().get(1);
    Assert.assertEquals(baseClassType, method2InBaseClass.declaringType());
    Assert.assertEquals(0, method2InBaseClass.exceptionTypes().size());
    Assert.assertEquals(Flags.AccPublic | Flags.AccSynchronized | Flags.AccFinal, method2InBaseClass.flags());
    Assert.assertEquals("method2InBaseClass", method2InBaseClass.elementName());
    Assert.assertEquals(0, method2InBaseClass.parameters().list().size());
    Assert.assertEquals("void", method2InBaseClass.returnType().name());
    Assert.assertFalse(method2InBaseClass.isConstructor());
    Assert.assertEquals(0, method2InBaseClass.annotations().list().size());
  }
}
