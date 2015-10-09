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
package org.eclipse.scout.sdk.core.model.sugar;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.fixture.AbstractBaseClass;
import org.eclipse.scout.sdk.core.fixture.AbstractChildClass;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel1;
import org.eclipse.scout.sdk.core.fixture.MarkerAnnotation;
import org.eclipse.scout.sdk.core.fixture.TestAnnotation;
import org.eclipse.scout.sdk.core.fixture.WildcardBaseClass;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IArrayMetaValue;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link QueryTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class QueryTest {

  @Test
  public void testSuperTypeQuery() {
    IType childClass = CoreTestingUtils.getChildClassType();
    Assert.assertEquals(3, childClass.superTypes().withFilter(new IFilter<IType>() {
      @Override
      public boolean evaluate(IType element) {
        return !element.isInterface();
      }
    }).list().size());
    Assert.assertEquals(3, childClass.superTypes().withFlags(Flags.AccInterface).list().size());
    Assert.assertEquals(2, childClass.superTypes().withMaxResultCount(2).list().size());
    Assert.assertEquals(1, childClass.superTypes().withName(InterfaceLevel1.class.getName()).list().size());
    Assert.assertEquals(1, childClass.superTypes().withSimpleName(InterfaceLevel1.class.getSimpleName()).list().size());
    Assert.assertEquals(5, childClass.superTypes().withSelf(false).list().size());
    Assert.assertEquals(3, childClass.superTypes().withSelf(false).withSuperClasses(false).list().size());
    Assert.assertEquals(4, childClass.superTypes().withSuperClasses(false).list().size());
    Assert.assertEquals(3, childClass.superTypes().withSuperInterfaces(false).list().size());
  }

  @Test
  public void testSuperMethodQuery() {
    IType acc = CoreTestingUtils.createJavaEnvironment().findType(AbstractChildClass.class.getName());
    Assert.assertEquals(2, acc.methods().withName("blub").first().superMethods().list().size());
    Assert.assertEquals(1, acc.methods().withName("blub").first().superMethods().withMaxResultCount(1).list().size());
    Assert.assertEquals(1, acc.methods().withName("blub").first().superMethods().withSelf(false).list().size());
    Assert.assertEquals(1, acc.methods().withName("blub").first().superMethods().withSuperClasses(false).list().size());
    Assert.assertEquals(1, acc.methods().withName("blub").first().superMethods().withFilter(new IFilter<IMethod>() {
      @Override
      public boolean evaluate(IMethod element) {
        return AbstractBaseClass.class.getName().equals(element.declaringType().name());
      }
    }).list().size());
  }

  @Test
  public void testTypeQuery() {
    IType base = CoreTestingUtils.getBaseClassType();
    Assert.assertEquals(1, base.innerTypes().withFilter(new IFilter<IType>() {
      @Override
      public boolean evaluate(IType element) {
        return "InnerClass2".equals(element.elementName());
      }
    }).list().size());
    Assert.assertEquals(1, base.innerTypes().withFlags(Flags.AccStatic).list().size());
    Assert.assertEquals(2, base.innerTypes().withInstanceOf(Collection.class.getName()).list().size());
    Assert.assertEquals(1, base.innerTypes().withMaxResultCount(1).list().size());
    Assert.assertEquals(1, base.innerTypes().withName("org.eclipse.scout.sdk.core.fixture.BaseClass$InnerClass2").list().size());
    Assert.assertEquals(1, base.innerTypes().withSimpleName("InnerClass2").list().size());

    IType abc = CoreTestingUtils.createJavaEnvironment().findType(AbstractBaseClass.class.getName());
    Assert.assertEquals(1, abc.innerTypes().withRecursiveInnerTypes(true).withSimpleName("InnerThree").list().size());
    Assert.assertEquals(0, abc.innerTypes().withRecursiveInnerTypes(false).withSimpleName("InnerThree").list().size());
  }

  @Test
  public void testMethodParameterQuery() {
    IMethod methodWithParams = CoreTestingUtils.createJavaEnvironment().findType(AbstractBaseClass.class.getName()).methods().withName("methodWithParams").first();
    Assert.assertEquals(1, methodWithParams.parameters().withDataType(String.class.getName()).list().size());
    Assert.assertEquals(TestAnnotation.class.getName(), methodWithParams.parameters().withName("firstParam").first().annotations().first().type().name());
    Assert.assertEquals(1, methodWithParams.parameters().withMaxResultCount(1).list().size());
    Assert.assertEquals(1, methodWithParams.parameters().withFilter(new IFilter<IMethodParameter>() {
      @Override
      public boolean evaluate(IMethodParameter element) {
        return element.annotations().existsAny();
      }
    }).list().size());
  }

  @Test
  public void testFieldQuery() {
    IType baseClass = CoreTestingUtils.getBaseClassType();
    IType childClass = CoreTestingUtils.getChildClassType();

    Assert.assertEquals(1, baseClass.fields().withFilter(new IFilter<IField>() {
      @Override
      public boolean evaluate(IField element) {
        return element.dataType().name().equals(IJavaRuntimeTypes.java_lang_Long);
      }
    }).list().size());
    Assert.assertEquals(1, childClass.fields().withFlags(Flags.AccProtected).list().size());
    Assert.assertEquals(1, childClass.fields().withMaxResultCount(1).list().size());
    Assert.assertEquals(1, childClass.fields().withName("m_test").list().size());
    Assert.assertEquals(3, childClass.fields().withSuperTypes(true).withFlags(Flags.AccPublic).list().size());
  }

  @Test
  public void testMethodQuery() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertEquals(1, childClassType.methods().withAnnotation(TestAnnotation.class.getName()).list().size());

    List<IMethod> list = childClassType.methods().withFilter(new IFilter<IMethod>() {
      @Override
      public boolean evaluate(IMethod element) {
        return !element.parameters().existsAny();
      }
    }).list();
    Assert.assertEquals(2, list.size());
    Assert.assertTrue(list.get(0).isConstructor());

    Assert.assertEquals(1, childClassType.methods().withFlags(Flags.AccProtected | Flags.AccSynchronized).list().size());
    Assert.assertEquals(1, childClassType.methods().withFlags(Flags.AccPrivate).list().size());
    Assert.assertEquals(2, childClassType.methods().withMaxResultCount(2).list().size());
    Assert.assertEquals(2, childClassType.methods().withName(Pattern.compile("[a-z0-9_]+class", Pattern.CASE_INSENSITIVE)).list().size());
    Assert.assertEquals(1, childClassType.methods().withName("firstCase").list().size());
    Assert.assertEquals(1, childClassType.methods().withSuperClasses(true).withName("method2InBaseClass").list().size());

    IType abstractBaseClass = CoreTestingUtils.createJavaEnvironment().findType(AbstractBaseClass.class.getName());
    Assert.assertEquals(1, abstractBaseClass.methods().withSuperInterfaces(true).withName("close").list().size());
    Assert.assertEquals(1, abstractBaseClass.methods().withSuperTypes(true).withName("close").list().size());
  }

  @Test
  public void testAnnotationQuery() {
    IJavaEnvironment javaEnvironment = CoreTestingUtils.createJavaEnvironment();
    IType acc = javaEnvironment.findType(AbstractChildClass.class.getName());
    IType childClass = CoreTestingUtils.getChildClassType();
    IType wbc = javaEnvironment.findType(WildcardBaseClass.class.getName());
    Assert.assertEquals(2, acc.methods().withName("blub").first().annotations().withSuperTypes(true).withName(MarkerAnnotation.class.getName()).list().size());
    Assert.assertEquals(2, childClass.annotations().withSuperClasses(true).withName(TestAnnotation.class.getName()).list().size());

    Assert.assertEquals(1, wbc.annotations().withFilter(new IFilter<IAnnotation>() {
      @Override
      public boolean evaluate(IAnnotation element) {
        return element.element("inner").value() instanceof IArrayMetaValue;
      }
    }).list().size());
    Assert.assertEquals(1, acc.methods().withName("blub").first().annotations().withMaxResultCount(1).list().size());

    IMethod methodInChildClass = childClass.methods().withName("methodInChildClass").first();
    Assert.assertEquals(1, methodInChildClass.annotations().withFilter(new IFilter<IAnnotation>() {
      @Override
      public boolean evaluate(IAnnotation element) {
        return element.elements().size() == 3;
      }
    }).list().size());
  }
}
