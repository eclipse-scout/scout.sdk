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
package org.eclipse.scout.sdk.internal.test.types;

import java.io.File;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.type.IPropertyBean;
import org.eclipse.scout.sdk.util.type.PropertyBeanComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutPropertyBeanFilters;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases for the {@link TypeUtility}
 */
public class TypeUtilityTest extends AbstractScoutSdkTest {

  private IType m_type;
  private Set<? extends IPropertyBean> m_propertyBeans;

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/util/typeUtility", "a");
  }

  @Before
  public void setup1() {
    m_type = TypeUtility.getType("a.BeanProperties");
    m_propertyBeans = TypeUtility.getPropertyBeans(m_type, ScoutPropertyBeanFilters.getDtoPropertyFilter(), PropertyBeanComparators.getNameComparator());
  }

  @After
  public void tearDown() {
    m_type = null;
    m_propertyBeans = null;
  }

  @Test
  public void testGetPropertyBeans_ordinaryStringProperty() {
    checkBeanProperty("m_propertyString", "PropertyString", "java.lang.String", true, true);
  }

  @Test
  public void testGetPropertyBeans_missingFormDataAnnotationOnGetterStringProperty() {
    IField field = m_type.getField("m_notAnnotatedGetter");
    Assert.assertNotNull(field);

    IPropertyBean bean = findPropertyBean("NotAnnotatedGetter");
    Assert.assertNull(bean);
  }

  @Test
  public void testGetPropertyBeans_missingFormDataAnnotationOnSetterStringProperty() {
    IField field = m_type.getField("m_notAnnotatedSetter");
    Assert.assertNotNull(field);

    IPropertyBean bean = findPropertyBean("NotAnnotatedSetter");
    Assert.assertNull(bean);
  }

  @Test
  public void testGetPropertyBeans_missingGetterStringProperty() {
    IField field = m_type.getField("m_missingGetter");
    Assert.assertNotNull(field);

    IPropertyBean bean = findPropertyBean("MissingGetter");
    Assert.assertNull(bean);
  }

  @Test
  public void testGetPropertyBeans_missingSetterStringProperty() {
    IField field = m_type.getField("m_missingSetter");
    Assert.assertNotNull(field);

    IPropertyBean bean = findPropertyBean("MissingSetter");
    Assert.assertNull(bean);
  }

  @Test
  public void testGetPropertyBeans_noFieldPrefixStringProperty() {
    checkBeanProperty("noPrefix", "NoPrefix", "java.lang.String", true, true);
  }

  @Test
  public void testGetPropertyBeans_otherFieldPrefixStringProperty() {
    checkBeanProperty("o_otherPrefix", "OtherPrefix", "java.lang.String", true, true);
  }

  @Test
  public void testGetPropertyBeans_ordinaryLongProperty() {
    checkBeanProperty("m_propertyLong", "PropertyLong", "java.lang.Long", true, true);
  }

  @Test
  public void testGetPropertyBeans_ordinaryShortProperty() {
    checkBeanProperty("m_propertyShort", "PropertyShort", "S", false, true);
  }

  @Test
  public void testGetPropertyBeans_ordinaryIntProperty() {
    checkBeanProperty("propertyInt", "PropertyInt", "I", false, true);
  }

  @Test
  public void testGetPropertyBeans_ordinarySimpleBooleanProperty() {
    checkBeanProperty("m_propetySimpleBoolean", "PropetySimpleBoolean", "Z", false, true);
  }

  @Test
  public void testGetPropertyBeans_ordinaryObjectBooleanProperty() {
    checkBeanProperty("m_propertyObjectBoolean", "PropertyObjectBoolean", "java.lang.Boolean", true, true);
  }

  @Test
  public void testGetPropertyBeans_undeclaredFieldPrefixStringProperty() {
    checkBeanProperty("u_notDeclaredPrefix", "NotDeclaredPrefix", "java.lang.String", true, false);
  }

  @Test
  public void testGetPropertyBeans_ordinaryPreAndSuffixProperty() {
    checkBeanProperty("m_preAndSuffixString_suffix", "PreAndSuffixString", "java.lang.String", true, true);
  }

  @Test
  public void testGetPropertyBeans_preAndUndeclaredSuffixStringProperty() {
    checkBeanProperty("m_preAndUnknownSuffixString_unknownSuffix", "PreAndUnknownSuffixString_unknownSuffix", "java.lang.String", true, true);
  }

  private void checkBeanProperty(String fieldName, String beanName, String beanTypeSignature, boolean createTypeSignature, boolean expectField) {
    IField field = m_type.getField(fieldName);
    Assert.assertNotNull(field);

    IPropertyBean bean = findPropertyBean(beanName);
    Assert.assertNotNull(bean);

    Assert.assertEquals(beanName, bean.getBeanName());
    Assert.assertEquals(createTypeSignature ? Signature.createTypeSignature(beanTypeSignature, createTypeSignature) : beanTypeSignature, bean.getBeanSignature());
    Assert.assertEquals(m_type, bean.getDeclaringType());
    Assert.assertNotNull(bean.getReadMethod());
    Assert.assertNotNull(bean.getWriteMethod());
    if (expectField) {
      Assert.assertNotNull(bean.getField());
    }
    else {
      Assert.assertNull(bean.getField());
    }
  }

  private IPropertyBean findPropertyBean(String name) {
    for (IPropertyBean b : m_propertyBeans) {
      if (name.equals(b.getBeanName())) {
        return b;
      }
    }
    return null;
  }

  @Test
  public void testMethodFinder() throws Exception {
    IType methodTestType = TypeUtility.getType("a.MethodTestType");
    TypeUtility.getMethod(methodTestType, "setFile", CollectionUtility.arrayList(Signature.createTypeSignature(File.class.getName(), true)));
  }
}
