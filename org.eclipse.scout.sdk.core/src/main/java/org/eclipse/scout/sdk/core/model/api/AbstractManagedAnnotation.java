/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
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

  protected static final String TYPE_NAME_FIELD_NAME = "TYPE_NAME";

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
   * @throws IllegalArgumentException
   *           If the element name given does not exist for this annotation
   * @see IAnnotationElement#isDefault()
   */
  @SuppressWarnings("unchecked")
  protected <T> T getValue(String name, Class<T> expectedType, Supplier<T> optionalCustomDefaultValueSupplier) {
    IAnnotationElement av = m_ann.element(name).orElseThrow(() -> elementNotExistingException(name));
    if (optionalCustomDefaultValueSupplier != null && av.isDefault()) {
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
   * Gets the annotation element with the name given as value of an {@link Enum}.
   *
   * @param elementName
   *          The name of the annotation element. Must exist for that annotation.
   * @param enumType
   *          The enum class from which the matching value should be returned. The match is done by name. So the given
   *          {@link Enum} must contain an element with the same name as in the source. Must not be {@code null}.
   * @param <T>
   *          The {@link Enum} type
   * @return The matching {@link Enum} value. Never returns {@code null}.
   * @see IAnnotationElement#isDefault()
   * @throws IllegalArgumentException
   *           If the element name given does not exist for this annotation or there is no element with the name used in
   *           the source code on the given {@link Enum}.
   */
  protected <T extends Enum<T>> T getValueAsEnum(String elementName, Class<T> enumType) {
    return getValueAsEnum(elementName, enumType, null);
  }

  /**
   * Gets the annotation element with the name given as value of an {@link Enum}.
   * 
   * @param elementName
   *          The name of the annotation element. Must exist for that annotation.
   * @param enumType
   *          The enum class from which the matching value should be returned. The match is done by name. So the given
   *          {@link Enum} must contain an element with the same name as in the source. Must not be {@code null}.
   * @param optionalCustomDefaultValueSupplier
   *          An optional {@link Supplier} which will be used to return a custom value in case the annotation element is
   *          not specified in the source (is the default value from the annotation definition).
   * @param <T>
   *          The {@link Enum} type
   * @return The matching {@link Enum} value. Never returns {@code null}.
   * @see IAnnotationElement#isDefault()
   * @throws IllegalArgumentException
   *           If the element name given does not exist for this annotation or there is no element with the name used in
   *           the source code on the given {@link Enum}.
   */
  protected <T extends Enum<T>> T getValueAsEnum(String elementName, Class<T> enumType, Supplier<T> optionalCustomDefaultValueSupplier) {
    if (optionalCustomDefaultValueSupplier != null && isDefault(elementName)) {
      return optionalCustomDefaultValueSupplier.get();
    }

    IField enumValueField = getValue(elementName, IField.class, null);
    if (enumValueField == null) {
      throw elementNotExistingException(elementName);
    }
    return Enum.valueOf(enumType, enumValueField.elementName());
  }

  protected SdkException elementNotExistingException(String name) {
    return new SdkException("Annotation '{}' has no attribute named '{}'.", m_ann.elementName(), name);
  }

  /**
   * Gets if the given element name is specified in the annotation or if the default value from the annotation
   * declaration is used.
   *
   * @param name
   *          The name of the element.
   * @return {@code true} if the given element does not exist in the annotation, {@code false} if it is explicitly
   *         specified.
   * @throws IllegalArgumentException
   *           if the name given does not exist for this annotation
   */
  protected boolean isDefault(String name) {
    return m_ann
        .element(name)
        .map(IAnnotationElement::isDefault)
        .orElseThrow(() -> elementNotExistingException(name));
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
