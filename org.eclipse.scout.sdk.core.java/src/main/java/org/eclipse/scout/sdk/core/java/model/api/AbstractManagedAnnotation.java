/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

import java.lang.reflect.Array;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link AbstractManagedAnnotation}</h3> Base class for managed annotation implementations. <br>
 * <br>
 * <b>Important:</b><br>
 * Implementors must provide an empty constructor and a field with name {@value TYPE_NAME_FIELD_NAME} and data type
 * {@code ApiFunction<?, ITypeNameSupplier>} to define the managed annotation fully qualified name it supports!
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
      var annotation = managedAnnotationType.getConstructor().newInstance();
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
  @SuppressWarnings("unchecked")
  public static ApiFunction<?, ITypeNameSupplier> typeName(Class<? extends AbstractManagedAnnotation> a) {
    if (a == null) {
      return null;
    }

    try {
      var field = a.getDeclaredField(TYPE_NAME_FIELD_NAME);
      field.setAccessible(true);
      return Ensure.instanceOf(field.get(null), ApiFunction.class);
    }
    catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
      throw new SdkException("Failed to read field {} of {}. Each managed annotation must define its {} using a field called '{}' of type {}.",
          TYPE_NAME_FIELD_NAME, a, ITypeNameSupplier.class.getSimpleName(), TYPE_NAME_FIELD_NAME, e, ApiFunction.class.getSimpleName());
    }
  }

  protected void postConstruct(IAnnotation ann) {
    m_ann = ann;
  }

  protected <A extends IApiSpecification> String getNameFromApi(Class<A> apiDefinition, Function<A, String> nameSupplier) {
    var api = Ensure.notNull(m_ann.javaEnvironment().requireApi(apiDefinition));
    return Ensure.notBlank(nameSupplier.apply(api));
  }

  /**
   * Gets the annotation element value
   * 
   * @param apiDefinition
   *          The api type that defines element name. An instance of this API is passed to the nameSupplier. May be
   *          {@code null} in case the given nameSupplier can handle a {@code null} input.
   * @param nameSupplier
   *          A {@link Function} to be called to obtain the annotation element name to get the value from.
   * @param expectedType
   *          The class into the value should be converted.
   * @param optionalCustomDefaultValueSupplier
   *          if this value is omitted, then the default value as declared in the original annotation type declaration
   *          is returned.<br>
   *          If this {@link Supplier} is set, then it is used in case the actual annotation does not define a
   *          <b>direct</b> value for this attribute.
   * @param <A>
   *          The API type that contains the class name
   * @param <T>
   *          The data type of the element value
   * @return The element value.
   * @throws IllegalArgumentException
   *           If the element name given does not exist for this annotation
   * @see IAnnotationElement#isDefault()
   */
  protected <A extends IApiSpecification, T> T getValueFrom(Class<A> apiDefinition, Function<A, String> nameSupplier, Class<T> expectedType, Supplier<T> optionalCustomDefaultValueSupplier) {
    var name = getNameFromApi(apiDefinition, nameSupplier);
    return getValue(name, expectedType, optionalCustomDefaultValueSupplier);
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
    var av = m_ann.element(name).orElseThrow(() -> elementNotExistingException(name));
    if (optionalCustomDefaultValueSupplier != null && av.isDefault()) {
      return optionalCustomDefaultValueSupplier.get();
    }

    // ManagedAnnotation array (from IAnnotation array)
    if (expectedType.isArray() && AbstractManagedAnnotation.class.isAssignableFrom(expectedType.getComponentType())) {
      var a = av.value().as(IAnnotation[].class);
      var componentType = (Class<? extends AbstractManagedAnnotation>) expectedType.getComponentType();
      var array = (T) Array.newInstance(componentType, a.length);
      for (var i = 0; i < a.length; i++) {
        Array.set(array, i, a[i].wrap(componentType));
      }
      return array;
    }

    // ManagedAnnotation element (from IAnnotation)
    if (AbstractManagedAnnotation.class.isAssignableFrom(expectedType)) {
      var xType = (Class<? extends AbstractManagedAnnotation>) expectedType;
      return (T) av.value().as(IAnnotation.class).wrap(xType);
    }

    return av.value().as(expectedType);
  }

  /**
   * Gets the annotation element as enum value.
   * 
   * @param apiDefinition
   *          The api type that defines element name. An instance of this API is passed to the nameSupplier. May be
   *          {@code null} in case the given nameSupplier can handle a {@code null} input.
   * @param nameSupplier
   *          A {@link Function} to be called to obtain the annotation element name to get the value from.
   * @param enumType
   *          The class into the value should be converted.
   * @param <A>
   *          The API type that contains the class name
   * @param <T>
   *          The enum type of the element value
   * @return The element value.
   * @throws IllegalArgumentException
   *           If the element name given does not exist for this annotation
   * @see IAnnotationElement#isDefault()
   */
  protected <A extends IApiSpecification, T extends Enum<T>> T getValueAsEnumFrom(Class<A> apiDefinition, Function<A, String> nameSupplier, Class<T> enumType) {
    return getValueAsEnumFrom(apiDefinition, nameSupplier, enumType, null);
  }

  /**
   * Gets the annotation element as enum value.
   * 
   * @param apiDefinition
   *          The api type that defines element name. An instance of this API is passed to the nameSupplier. May be
   *          {@code null} in case the given nameSupplier can handle a {@code null} input.
   * @param nameSupplier
   *          A {@link Function} to be called to obtain the annotation element name to get the value from.
   * @param enumType
   *          The class into the value should be converted.
   * @param optionalCustomDefaultValueSupplier
   *          if this value is omitted then the default value as declared in the original annotation type declaration is
   *          returned.<br>
   *          If this {@link Supplier} is set then it is used in case the actual annotation does not define a
   *          <b>direct</b> value for this attribute.
   * @param <A>
   *          The API type that contains the class name
   * @param <T>
   *          The enum type of the element value
   * @return The element value.
   * @throws IllegalArgumentException
   *           If the element name given does not exist for this annotation
   * @see IAnnotationElement#isDefault()
   */
  protected <A extends IApiSpecification, T extends Enum<T>> T getValueAsEnumFrom(Class<A> apiDefinition, Function<A, String> nameSupplier, Class<T> enumType, Supplier<T> optionalCustomDefaultValueSupplier) {
    var name = getNameFromApi(apiDefinition, nameSupplier);
    return getValueAsEnum(name, enumType, optionalCustomDefaultValueSupplier);
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

    var enumValueField = getValue(elementName, IField.class, null);
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

  protected <A extends IApiSpecification> boolean isDefault(Class<A> apiDefinition, Function<A, String> nameSupplier) {
    return isDefault(getNameFromApi(apiDefinition, nameSupplier));
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
