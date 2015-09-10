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

    Assert.assertEquals(3, childClassType.getMethods().size());

    // constructor
    IMethod constr = childClassType.getMethods().get(0);
    Assert.assertEquals(childClassType, constr.getDeclaringType());
    Assert.assertEquals(0, constr.getExceptionTypes().size());
    Assert.assertEquals(Flags.AccPublic, constr.getFlags());
    Assert.assertEquals(childClassType.getSimpleName(), constr.getElementName());
    Assert.assertEquals(0, constr.getParameters().size());
    Assert.assertNull(constr.getReturnType());
    Assert.assertTrue(constr.isConstructor());
    Assert.assertEquals(0, constr.getAnnotations().size());

    // methodInChildClass
    IMethod methodInChildClass = childClassType.getMethods().get(1);
    Assert.assertEquals(childClassType, methodInChildClass.getDeclaringType());
    Assert.assertEquals(1, methodInChildClass.getExceptionTypes().size());
    Assert.assertEquals(IOException.class.getName(), methodInChildClass.getExceptionTypes().get(0).getName());
    Assert.assertEquals(Flags.AccProtected | Flags.AccSynchronized, methodInChildClass.getFlags());
    Assert.assertEquals("methodInChildClass", methodInChildClass.getElementName());
    Assert.assertEquals(2, methodInChildClass.getParameters().size());
    Assert.assertEquals("boolean", methodInChildClass.getReturnType().getLeafComponentType().getName());
    Assert.assertTrue(methodInChildClass.getReturnType().isArray());
    Assert.assertFalse(methodInChildClass.isConstructor());
    Assert.assertEquals(1, methodInChildClass.getAnnotations().size());

    // firstCase
    IMethod firstCase = childClassType.getMethods().get(2);
    Assert.assertEquals(childClassType, firstCase.getDeclaringType());
    Assert.assertEquals(0, firstCase.getExceptionTypes().size());
    Assert.assertEquals(Flags.AccPrivate, firstCase.getFlags());
    Assert.assertEquals("firstCase", firstCase.getElementName());
    Assert.assertEquals(0, firstCase.getParameters().size());
    Assert.assertEquals(Set.class.getName(), firstCase.getReturnType().getLeafComponentType().getName());
    Assert.assertFalse(firstCase.isConstructor());
    Assert.assertEquals(1, firstCase.getAnnotations().size());
  }

  @Test
  public void testToString() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);
    Assert.assertFalse(StringUtils.isBlank(childClassType.getMethods().get(1).toString()));
  }

  @Test
  public void testBaseClassMethods() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    Assert.assertEquals(2, baseClassType.getMethods().size());

    // methodInBaseClass
    IMethod methodInBaseClass = baseClassType.getMethods().get(0);
    Assert.assertEquals(baseClassType, methodInBaseClass.getDeclaringType());
    Assert.assertEquals(2, methodInBaseClass.getExceptionTypes().size());
    Assert.assertEquals(IOError.class.getName(), methodInBaseClass.getExceptionTypes().get(0).getName());
    Assert.assertEquals(FileNotFoundException.class.getName(), methodInBaseClass.getExceptionTypes().get(1).getName());
    Assert.assertEquals(Flags.AccProtected, methodInBaseClass.getFlags());
    Assert.assertEquals("methodInBaseClass", methodInBaseClass.getElementName());
    Assert.assertEquals(1, methodInBaseClass.getParameters().size());
    Assert.assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), methodInBaseClass.getReturnType().getLeafComponentType().getName());
    Assert.assertEquals(2, methodInBaseClass.getReturnType().getArrayDimension());
    Assert.assertTrue(methodInBaseClass.getReturnType().isArray());
    Assert.assertFalse(methodInBaseClass.isConstructor());
    Assert.assertEquals(2, methodInBaseClass.getAnnotations().size());

    // method2InBaseClass
    IMethod method2InBaseClass = baseClassType.getMethods().get(1);
    Assert.assertEquals(baseClassType, method2InBaseClass.getDeclaringType());
    Assert.assertEquals(0, method2InBaseClass.getExceptionTypes().size());
    Assert.assertEquals(Flags.AccPublic | Flags.AccSynchronized | Flags.AccFinal, method2InBaseClass.getFlags());
    Assert.assertEquals("method2InBaseClass", method2InBaseClass.getElementName());
    Assert.assertEquals(0, method2InBaseClass.getParameters().size());
    Assert.assertEquals("void", method2InBaseClass.getReturnType().getName());
    Assert.assertFalse(method2InBaseClass.isConstructor());
    Assert.assertEquals(0, method2InBaseClass.getAnnotations().size());
  }
}
