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
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel0;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel1;
import org.eclipse.scout.sdk.core.fixture.WildcardChildClass;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.testing.JavaEnvironmentBuilder;
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

    Assert.assertEquals(0, childClassType.arrayDimension());
    Assert.assertEquals(Flags.AccPublic, childClassType.flags());
    Assert.assertEquals(1, childClassType.annotations().list().size());
    Assert.assertEquals(CoreTestingUtils.getChildClassIcu(), childClassType.compilationUnit());
    Assert.assertNull(childClassType.declaringType());
    Assert.assertEquals(2, childClassType.fields().list().size());
    Assert.assertEquals(3, childClassType.methods().list().size());
    Assert.assertEquals(ChildClass.class.getName(), childClassType.name());
    Assert.assertEquals(ChildClass.class.getSimpleName(), childClassType.elementName());
    Assert.assertEquals(BaseClass.class.getName(), childClassType.superClass().name());
    Assert.assertFalse(childClassType.isParameterType());
    Assert.assertFalse(childClassType.isArray());
    Assert.assertTrue(childClassType.hasTypeParameters());

    // super interfaces
    Assert.assertEquals(1, childClassType.superInterfaces().size());
    Assert.assertEquals(InterfaceLevel0.class.getName(), childClassType.superInterfaces().get(0).name());

    // type parameters
    Assert.assertEquals(1, childClassType.typeParameters().size());
    ITypeParameter firstTypeParam = childClassType.typeParameters().get(0);
    Assert.assertNotNull(firstTypeParam);
    Assert.assertEquals(childClassType, firstTypeParam.declaringMember());
    Assert.assertEquals("X", firstTypeParam.elementName());
    Assert.assertEquals(3, firstTypeParam.bounds().size());
    Assert.assertEquals(AbstractList.class.getName(), firstTypeParam.bounds().get(0).name());
    Assert.assertEquals(Runnable.class.getName(), firstTypeParam.bounds().get(1).name());
    Assert.assertEquals(Serializable.class.getName(), firstTypeParam.bounds().get(2).name());

    // member types
    Assert.assertEquals(0, childClassType.innerTypes().list().size());

    // type arguments
    Assert.assertEquals(0, childClassType.typeArguments().size());
  }

  @Test
  public void testToString() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    Assert.assertFalse(StringUtils.isBlank(childClassType.toString()));
  }

  @Test
  public void testWildcard() {
    IType wildcardType = CoreTestingUtils.createJavaEnvironment().findType(WildcardChildClass.class.getName());
    IType returnType = wildcardType.methods().first().returnType();
    IType firstArg = returnType.typeArguments().get(0);
    Assert.assertTrue(firstArg.isWildcardType());
    Assert.assertEquals(BaseClass.class.getName(), firstArg.name());
    Assert.assertEquals(2, firstArg.typeArguments().size());
    Assert.assertEquals("Ljava.lang.Class<+Lorg.eclipse.scout.sdk.core.fixture.BaseClass<**>;>;", SignatureUtils.getTypeSignature(returnType));
  }

  @Test
  public void testPrimaryTypeSuper() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    Assert.assertEquals(0, baseClassType.arrayDimension());
    Assert.assertEquals(Flags.AccPublic, baseClassType.flags());
    Assert.assertEquals(1, baseClassType.annotations().list().size());
    Assert.assertTrue(baseClassType.compilationUnit().isSynthetic());
    Assert.assertNull(baseClassType.declaringType());
    Assert.assertEquals(2, baseClassType.fields().list().size());
    Assert.assertEquals(2, baseClassType.methods().list().size());
    Assert.assertEquals(BaseClass.class.getName(), baseClassType.name());
    Assert.assertEquals(BaseClass.class.getSimpleName(), baseClassType.elementName());
    Assert.assertEquals(Object.class.getName(), baseClassType.superClass().name());
    Assert.assertFalse(baseClassType.isParameterType());
    Assert.assertFalse(baseClassType.isArray());
    Assert.assertTrue(baseClassType.hasTypeParameters());

    // super interfaces
    Assert.assertEquals(1, baseClassType.superInterfaces().size());
    Assert.assertEquals(InterfaceLevel1.class.getName(), baseClassType.superInterfaces().get(0).name());

    // type parameters
    Assert.assertEquals(2, baseClassType.typeParameters().size());
    ITypeParameter firstTypeParam = baseClassType.typeParameters().get(0);
    Assert.assertNotNull(firstTypeParam);
    Assert.assertEquals(baseClassType, firstTypeParam.declaringMember());
    Assert.assertEquals("T", firstTypeParam.elementName());
    Assert.assertEquals(0, firstTypeParam.bounds().size());

    ITypeParameter secondTypeParam = baseClassType.typeParameters().get(1);
    Assert.assertNotNull(secondTypeParam);
    Assert.assertEquals(baseClassType, secondTypeParam.declaringMember());
    Assert.assertEquals("Z", secondTypeParam.elementName());
    Assert.assertEquals(0, secondTypeParam.bounds().size());

    // member types
    Assert.assertEquals(2, baseClassType.innerTypes().list().size());
    Assert.assertEquals(Flags.AccStatic, baseClassType.innerTypes().first().flags());
    Assert.assertEquals(baseClassType, baseClassType.innerTypes().first().declaringType());
    Assert.assertEquals(Flags.AccProtected, baseClassType.innerTypes().list().get(1).flags());
    Assert.assertEquals(baseClassType, baseClassType.innerTypes().list().get(1).declaringType());

    // type arguments
    List<IType> typeArguments = baseClassType.typeArguments();
    Assert.assertEquals(2, typeArguments.size());
    IType firstTypeArg = typeArguments.get(0);
    Assert.assertTrue(firstTypeArg.isParameterType());
    Assert.assertEquals(AbstractList.class.getName(), firstTypeArg.superClass().name());
    Assert.assertEquals(2, firstTypeArg.superInterfaces().size());
    Assert.assertEquals(Runnable.class.getName(), firstTypeArg.superInterfaces().get(0).name());
    Assert.assertEquals(Serializable.class.getName(), firstTypeArg.superInterfaces().get(1).name());

    IType secondTypeArg = typeArguments.get(1);
    Assert.assertFalse(secondTypeArg.isParameterType());
    Assert.assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), secondTypeArg.name());
  }

  @Test
  public void testInnerTypeDirectly() {
    //explicitly add target/classes, by default this would be ignored
    IJavaEnvironment env = new JavaEnvironmentBuilder()
        .withoutScoutSdk()
        .withoutAllSources()
        .withClassesFolder("target/classes")
        .build();

    IType innerClass2 = env.findType("org.eclipse.scout.sdk.core.fixture.BaseClass$InnerClass2");
    testInnerType(innerClass2);
  }

  @Test
  public void testInnerTypeFromDeclaringType() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    IType innerClass2 = baseClassType.innerTypes().list().get(1);
    testInnerType(innerClass2);
  }

  private static void testInnerType(IType innerClass2) {
    Assert.assertNotNull(innerClass2);

    Assert.assertEquals(0, innerClass2.arrayDimension());
    Assert.assertEquals(Flags.AccProtected, innerClass2.flags());
    Assert.assertEquals(0, innerClass2.annotations().list().size());
    Assert.assertTrue(innerClass2.compilationUnit().isSynthetic());
    Assert.assertEquals(CoreTestingUtils.getBaseClassType().name(), innerClass2.declaringType().name());
    Assert.assertEquals(1, innerClass2.fields().list().size());
    Assert.assertEquals(0, innerClass2.methods().list().size());
    Assert.assertEquals("org.eclipse.scout.sdk.core.fixture.BaseClass$InnerClass2", innerClass2.name());
    Assert.assertEquals("InnerClass2", innerClass2.elementName());
    Assert.assertEquals(ArrayList.class.getName(), innerClass2.superClass().name());
    Assert.assertFalse(innerClass2.isParameterType());
    Assert.assertFalse(innerClass2.isArray());
    Assert.assertFalse(innerClass2.hasTypeParameters());

    // super interfaces
    Assert.assertEquals(0, innerClass2.superInterfaces().size());

    // type parameters
    Assert.assertEquals(0, innerClass2.typeParameters().size());

    // member types
    Assert.assertEquals(0, innerClass2.innerTypes().list().size());

    // type arguments
    List<IType> typeArguments = innerClass2.typeArguments();
    Assert.assertEquals(0, typeArguments.size());

    // super type arguments
    List<IType> superTypeArguments = innerClass2.superClass().typeArguments();
    Assert.assertEquals(1, superTypeArguments.size());
    IType firstTypeArg = superTypeArguments.get(0);
    Assert.assertFalse(firstTypeArg.isParameterType());
    Assert.assertTrue(firstTypeArg.isArray());
    Assert.assertEquals(1, firstTypeArg.arrayDimension());
    Assert.assertEquals(BigDecimal.class.getName(), firstTypeArg.leafComponentType().name());
  }
}
