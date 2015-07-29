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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.CoreTestingUtils;
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel0;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel1;
import org.eclipse.scout.sdk.core.fixture.WildcardChildClass;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.testing.TestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class TypeTest {
  @Test
  public void testPrimaryType() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    Assert.assertEquals(0, childClassType.getArrayDimension());
    Assert.assertEquals(Flags.AccPublic, childClassType.getFlags());
    Assert.assertEquals(1, childClassType.getAnnotations().size());
    Assert.assertEquals(CoreTestingUtils.getChildClassIcu(), childClassType.getCompilationUnit());
    Assert.assertNull(childClassType.getDeclaringType());
    Assert.assertEquals(2, childClassType.getFields().size());
    Assert.assertEquals(3, childClassType.getMethods().size());
    Assert.assertEquals(ChildClass.class.getName(), childClassType.getName());
    Assert.assertEquals(ChildClass.class.getSimpleName(), childClassType.getSimpleName());
    Assert.assertEquals(BaseClass.class.getName(), childClassType.getSuperClass().getName());
    Assert.assertFalse(childClassType.isAnonymous());
    Assert.assertFalse(childClassType.isArray());
    Assert.assertTrue(childClassType.hasTypeParameters());

    // super interfaces
    Assert.assertEquals(1, childClassType.getSuperInterfaces().size());
    Assert.assertEquals(InterfaceLevel0.class.getName(), childClassType.getSuperInterfaces().get(0).getName());

    // type parameters
    Assert.assertEquals(1, childClassType.getTypeParameters().size());
    ITypeParameter firstTypeParam = childClassType.getTypeParameters().get(0);
    Assert.assertNotNull(firstTypeParam);
    Assert.assertEquals(childClassType, firstTypeParam.getType());
    Assert.assertEquals("X", firstTypeParam.getName());
    Assert.assertEquals(3, firstTypeParam.getBounds().size());
    Assert.assertEquals(AbstractList.class.getName(), firstTypeParam.getBounds().get(0).getName());
    Assert.assertEquals(Runnable.class.getName(), firstTypeParam.getBounds().get(1).getName());
    Assert.assertEquals(Serializable.class.getName(), firstTypeParam.getBounds().get(2).getName());

    // member types
    Assert.assertEquals(0, childClassType.getTypes().size());

    // type arguments
    Assert.assertEquals(0, childClassType.getTypeArguments().size());
  }

  @Test
  public void testToString() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    Assert.assertFalse(StringUtils.isBlank(childClassType.toString()));
  }

  @Test
  public void testWildcard() {
    IType wildcardType = TestingUtils.getType(WildcardChildClass.class.getName(), CoreTestingUtils.SOURCE_FOLDER);
    IType returnType = wildcardType.getMethods().get(1).getReturnType();
    IType firstArg = returnType.getTypeArguments().get(0);
    Assert.assertTrue(firstArg.isWildcardType());
    Assert.assertEquals(BaseClass.class.getName(), firstArg.getName());
    Assert.assertEquals(2, firstArg.getTypeArguments().size());
    Assert.assertEquals("Ljava.lang.Class<+Lorg.eclipse.scout.sdk.core.fixture.BaseClass<**>;>;", SignatureUtils.getResolvedSignature(returnType));
  }

  @Test
  public void testPrimaryTypeSuper() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    Assert.assertEquals(0, baseClassType.getArrayDimension());
    Assert.assertEquals(Flags.AccPublic, baseClassType.getFlags());
    Assert.assertEquals(1, baseClassType.getAnnotations().size());
    Assert.assertNull(baseClassType.getCompilationUnit());
    Assert.assertNull(baseClassType.getDeclaringType());
    Assert.assertEquals(2, baseClassType.getFields().size());
    Assert.assertEquals(3, baseClassType.getMethods().size());
    Assert.assertEquals(BaseClass.class.getName(), baseClassType.getName());
    Assert.assertEquals(BaseClass.class.getSimpleName(), baseClassType.getSimpleName());
    Assert.assertEquals(Object.class.getName(), baseClassType.getSuperClass().getName());
    Assert.assertFalse(baseClassType.isAnonymous());
    Assert.assertFalse(baseClassType.isArray());
    Assert.assertTrue(baseClassType.hasTypeParameters());

    // super interfaces
    Assert.assertEquals(1, baseClassType.getSuperInterfaces().size());
    Assert.assertEquals(InterfaceLevel1.class.getName(), baseClassType.getSuperInterfaces().get(0).getName());

    // type parameters
    Assert.assertEquals(2, baseClassType.getTypeParameters().size());
    ITypeParameter firstTypeParam = baseClassType.getTypeParameters().get(0);
    Assert.assertNotNull(firstTypeParam);
    Assert.assertEquals(baseClassType, firstTypeParam.getType());
    Assert.assertEquals("T", firstTypeParam.getName());
    Assert.assertEquals(0, firstTypeParam.getBounds().size());

    ITypeParameter secondTypeParam = baseClassType.getTypeParameters().get(1);
    Assert.assertNotNull(secondTypeParam);
    Assert.assertEquals(baseClassType, secondTypeParam.getType());
    Assert.assertEquals("Z", secondTypeParam.getName());
    Assert.assertEquals(0, secondTypeParam.getBounds().size());

    // member types
    Assert.assertEquals(2, baseClassType.getTypes().size());
    Assert.assertEquals(Flags.AccStatic, baseClassType.getTypes().get(0).getFlags());
    Assert.assertEquals(baseClassType, baseClassType.getTypes().get(0).getDeclaringType());
    Assert.assertEquals(Flags.AccProtected, baseClassType.getTypes().get(1).getFlags());
    Assert.assertEquals(baseClassType, baseClassType.getTypes().get(1).getDeclaringType());

    // type arguments
    List<IType> typeArguments = baseClassType.getTypeArguments();
    Assert.assertEquals(2, typeArguments.size());
    IType firstTypeArg = typeArguments.get(0);
    Assert.assertTrue(firstTypeArg.isAnonymous());
    Assert.assertEquals(AbstractList.class.getName(), firstTypeArg.getSuperClass().getName());
    Assert.assertEquals(2, firstTypeArg.getSuperInterfaces().size());
    Assert.assertEquals(Runnable.class.getName(), firstTypeArg.getSuperInterfaces().get(0).getName());
    Assert.assertEquals(Serializable.class.getName(), firstTypeArg.getSuperInterfaces().get(1).getName());

    IType secondTypeArg = typeArguments.get(1);
    Assert.assertFalse(secondTypeArg.isAnonymous());
    Assert.assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), secondTypeArg.getName());
  }

  @Test
  public void testInnerTypeDirectly() {
    IType innerClass2 = TestingUtils.getType("org.eclipse.scout.sdk.core.fixture.BaseClass$InnerClass2");
    testInnerType(innerClass2);
  }

  @Test
  public void testInnerTypeFromDeclaringType() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    IType innerClass2 = baseClassType.getTypes().get(1);
    testInnerType(innerClass2);
  }

  private static void testInnerType(IType innerClass2) {
    Assert.assertNotNull(innerClass2);

    Assert.assertEquals(0, innerClass2.getArrayDimension());
    Assert.assertEquals(Flags.AccProtected, innerClass2.getFlags());
    Assert.assertEquals(0, innerClass2.getAnnotations().size());
    Assert.assertNull(innerClass2.getCompilationUnit());
    Assert.assertEquals(CoreTestingUtils.getBaseClassType(), innerClass2.getDeclaringType());
    Assert.assertEquals(1, innerClass2.getFields().size());
    Assert.assertEquals(1, innerClass2.getMethods().size());
    Assert.assertEquals("org.eclipse.scout.sdk.core.fixture.BaseClass$InnerClass2", innerClass2.getName());
    Assert.assertEquals("InnerClass2", innerClass2.getSimpleName());
    Assert.assertEquals(ArrayList.class.getName(), innerClass2.getSuperClass().getName());
    Assert.assertFalse(innerClass2.isAnonymous());
    Assert.assertFalse(innerClass2.isArray());
    Assert.assertFalse(innerClass2.hasTypeParameters());

    // super interfaces
    Assert.assertEquals(0, innerClass2.getSuperInterfaces().size());

    // type parameters
    Assert.assertEquals(0, innerClass2.getTypeParameters().size());

    // member types
    Assert.assertEquals(0, innerClass2.getTypes().size());

    // type arguments
    List<IType> typeArguments = innerClass2.getTypeArguments();
    Assert.assertEquals(0, typeArguments.size());

    // super type arguments
    List<IType> superTypeArguments = innerClass2.getSuperClass().getTypeArguments();
    Assert.assertEquals(1, superTypeArguments.size());
    IType firstTypeArg = superTypeArguments.get(0);
    Assert.assertFalse(firstTypeArg.isAnonymous());
    Assert.assertTrue(firstTypeArg.isArray());
    Assert.assertEquals(1, firstTypeArg.getArrayDimension());
    Assert.assertEquals(BigDecimal.class.getName(), firstTypeArg.getName());
  }
}
