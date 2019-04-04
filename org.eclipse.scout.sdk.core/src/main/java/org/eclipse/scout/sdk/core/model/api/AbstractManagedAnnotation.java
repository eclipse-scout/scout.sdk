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
package org.eclipse.scout.sdk.core.model.api;

import java.lang.reflect.Array;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link AbstractManagedAnnotation}</h3> Base class for managed annotation implementations. <br>
 * <br>
 * <b>Important:</b><br>
 * Implementors must provide an empty constructor and a field with name "TYPE_NAME" to define the managed annotation
 * fully qualified name it supports!
 *
 * @since 5.1.0
 */
public abstract class AbstractManagedAnnotation {

  public static final String TYPE_NAME_FIELD_NAME = "TYPE_NAME";

  private IAnnotation m_ann;

  protected AbstractManagedAnnotation() {
  }

  /**
   * Wraps an {@link IAnnotation} into a managed annotation of the given type.
   *
   * @param a
   *          The {@link IAnnotation} to wrap.
   * @param managedAnnotationType
   *          The managed annotation class
   * @return The wrapped annotation.
   */
  public static <A extends AbstractManagedAnnotation> A wrap(IAnnotation a, Class<A> managedAnnotationType) {
    try {
      A annotation = managedAnnotationType.getConstructor().newInstance();
      annotation.postConstruct(a);
      return annotation;
    }
    catch (ReflectiveOperationException e) {
      throw new IllegalArgumentException("create " + managedAnnotationType.getName() + " with " + a, e);
    }
  }

  /**
   * @return the value of the static field {@value #TYPE_NAME_FIELD_NAME} each managed annotation must have.
   */
  public static String typeName(Class<? extends AbstractManagedAnnotation> a) {
    if (a == null) {
      return null;
    }

    try {
      return (String) a.getField(TYPE_NAME_FIELD_NAME).get(null);
    }
    catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
      throw new SdkException("Failed to read field {} of {}. Each managed annotation must define its fully qualified name using a field called '{}'.", TYPE_NAME_FIELD_NAME, a, TYPE_NAME_FIELD_NAME, e);
    }
  }

  protected void postConstruct(IAnnotation ann) {
    m_ann = ann;
  }

  /**
   * Gets the annotation element value
   *
   * @param name
   *          The name of the element
   * @param expectedType
   *          The class into the value should be converted.
   * @param optionalCustomDefaultValueSupplier
   *          if this value is omitted then the default value as declared in the original annotation type declaration is
   *          returned.<br>
   *          If this {@link Supplier} is set then it is used in case the actual annotation does not define a
   *          <b>direct</b> value for this attribute.
   * @return The element value.
   * @see IAnnotationElement#isDefault()
   */
  @SuppressWarnings("unchecked")
  protected <T> T getValue(String name, Class<T> expectedType, Supplier<T> optionalCustomDefaultValueSupplier) {
    IAnnotationElement av = m_ann.element(name).orElseThrow(() -> new SdkException("Annotation '{}' has no attribute named '{}'.", m_ann.elementName(), name));
    if (av.isDefault() && optionalCustomDefaultValueSupplier != null) {
      return optionalCustomDefaultValueSupplier.get();
    }

    // ManagedAnnotation array (from IAnnotation array)
    if (expectedType.isArray() && AbstractManagedAnnotation.class.isAssignableFrom(expectedType.getComponentType())) {
      IAnnotation[] a = av.value().as(IAnnotation[].class);
      Class<? extends AbstractManagedAnnotation> componentType = (Class<? extends AbstractManagedAnnotation>) expectedType.getComponentType();
      T array = (T) Array.newInstance(componentType, a.length);
      for (int i = 0; i < a.length; i++) {
        Array.set(array, i, a[i].wrap(componentType));
      }
      return array;
    }

    // ManagedAnnotation element (from IAnnotation)
    if (AbstractManagedAnnotation.class.isAssignableFrom(expectedType)) {
      Class<? extends AbstractManagedAnnotation> xType = (Class<? extends AbstractManagedAnnotation>) expectedType;
      return (T) av.value().as(IAnnotation.class).wrap(xType);
    }

    return av.value().as(expectedType);
  }

  /**
   * Gets if the given element name is specified in the annotation or if the default value from the annotation
   * declaration is used.
   *
   * @param name
   *          The name of the element.
   * @return {@code true} if the given element does not exist in the annotation, {@code false} if it is explicitly
   *         specified.
   */
  protected boolean isDefault(String name) {
    return m_ann
        .element(name)
        .map(IAnnotationElement::isDefault)
        .orElse(Boolean.FALSE);
  }

  /**
   * Unwraps the managed annotation into its underlying {@link IAnnotation}.
   *
   * @return The {@link IAnnotation} of this managed annotation.
   */
  public IAnnotation unwrap() {
    return m_ann;
  }
}
