/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.model.sugar;

import java.lang.reflect.Array;

import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationValue;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link AbstractManagedAnnotation}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public abstract class AbstractManagedAnnotation {

  private IAnnotation m_ann;

  protected AbstractManagedAnnotation() {
  }

  protected void postConstruct(IAnnotation ann) {
    m_ann = ann;
  }

  /**
   * @param name
   * @param expectedType
   * @param optionalCustomDefaultValue
   *          if this value is omitted then the default value as declared in the original annotation type declaration is
   *          returned. If this value is set then it is used in case the actual annotation does not define a direct
   *          value for this attribute.
   * @return
   */
  @SuppressWarnings("unchecked")
  protected <T> T getValue(String name, Class<T> expectedType, Object optionalCustomDefaultValue) {
    IAnnotationValue av = m_ann.getValue(name);
    if (av == null || av.isSyntheticDefaultValue()) {
      if (optionalCustomDefaultValue != null && Array.getLength(optionalCustomDefaultValue) > 0) {
        return (T) Array.get(optionalCustomDefaultValue, 0);
      }
    }
    if (av == null) {
      throw new SdkException("annotation " + m_ann.getElementName() + " has no attribute named '" + name + "'");
    }
    //ManagedAnnotation array (from IAnnotation array)
    if (expectedType.isArray() && AbstractManagedAnnotation.class.isAssignableFrom(expectedType.getComponentType())) {
      IAnnotation[] a = av.getMetaValue().getObject(IAnnotation[].class);
      Class<? extends AbstractManagedAnnotation> componentType = (Class<? extends AbstractManagedAnnotation>) expectedType.getComponentType();
      T array = (T) Array.newInstance(componentType, a.length);
      for (int i = 0; i < a.length; i++) {
        Array.set(array, i, ManagedAnnotationUtil.wrap(a[i], componentType));
      }
      return array;
    }
    //ManagedAnnotation element (from IAnnotation)
    if (AbstractManagedAnnotation.class.isAssignableFrom(expectedType)) {
      Class<? extends AbstractManagedAnnotation> xType = (Class<? extends AbstractManagedAnnotation>) expectedType;
      return (T) ManagedAnnotationUtil.wrap(av.getMetaValue().getObject(IAnnotation.class), xType);
    }
    return av.getMetaValue().getObject(expectedType);
  }

  protected boolean isDefaultValue(String name) {
    IAnnotationValue av = m_ann.getValue(name);
    if (av == null) {
      return false;
    }
    return av.isSyntheticDefaultValue();
  }

  public IAnnotation unwrap() {
    return m_ann;
  }
}
