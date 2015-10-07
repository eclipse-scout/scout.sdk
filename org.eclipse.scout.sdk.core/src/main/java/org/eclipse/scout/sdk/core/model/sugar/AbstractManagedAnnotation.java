/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.model.sugar;

import java.lang.reflect.Array;

import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link AbstractManagedAnnotation}</h3> Base class for managed annotation implementations. <br>
 * <br>
 * <b>Important:</b><br>
 * Implementors must provide an empty constructor and a field with name "TYPE_NAME" to define the managed annotation
 * fully qualified name it supports!
 *
 * @author Ivan Motsch
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
   * Gets the annotation element value
   *
   * @param name
   *          The name of the element
   * @param expectedType
   *          The class into the value should be converted.
   * @param optionalCustomDefaultValue
   *          if this value is omitted then the default value as declared in the original annotation type declaration is
   *          returned.<br>
   *          If this value is set then it is used in case the actual annotation does not define a <b>direct</b> value
   *          for this attribute.
   * @return The element value.
   * @see IAnnotationElement#isDefault()
   */
  @SuppressWarnings("unchecked")
  protected <T> T getValue(String name, Class<T> expectedType, Object optionalCustomDefaultValue) {
    IAnnotationElement av = m_ann.element(name);
    if (av == null || av.isDefault()) {
      if (optionalCustomDefaultValue != null && Array.getLength(optionalCustomDefaultValue) > 0) {
        return (T) Array.get(optionalCustomDefaultValue, 0);
      }
    }
    if (av == null) {
      throw new SdkException("Annotation '" + m_ann.elementName() + "' has no attribute named '" + name + "'.");
    }
    //ManagedAnnotation array (from IAnnotation array)
    if (expectedType.isArray() && AbstractManagedAnnotation.class.isAssignableFrom(expectedType.getComponentType())) {
      IAnnotation[] a = av.value().get(IAnnotation[].class);
      Class<? extends AbstractManagedAnnotation> componentType = (Class<? extends AbstractManagedAnnotation>) expectedType.getComponentType();
      T array = (T) Array.newInstance(componentType, a.length);
      for (int i = 0; i < a.length; i++) {
        Array.set(array, i, a[i].wrap(componentType));
      }
      return array;
    }
    //ManagedAnnotation element (from IAnnotation)
    if (AbstractManagedAnnotation.class.isAssignableFrom(expectedType)) {
      Class<? extends AbstractManagedAnnotation> xType = (Class<? extends AbstractManagedAnnotation>) expectedType;
      return (T) av.value().get(IAnnotation.class).wrap(xType);
    }
    return av.value().get(expectedType);
  }

  /**
   * Gets if the given element name is specified in the annotation or if the default value from the annotation
   * declaration is used.
   *
   * @param name
   *          The name of the element.
   * @return <code>true</code> if the given element does not exist in the annotation, <code>false</code> if it is
   *         explicitly specified.
   */
  protected boolean isDefault(String name) {
    IAnnotationElement av = m_ann.element(name);
    if (av == null) {
      return false;
    }
    return av.isDefault();
  }

  /**
   * Unwraps the managed annotation into its underlying {@link IAnnotation}.
   *
   * @return The {@link IAnnotation} of this managed annotation.
   */
  public IAnnotation unwrap() {
    return m_ann;
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
      // security check:
      String typeName = typeName(managedAnnotationType);
      if (!a.type().name().equals(typeName)) {
        throw new IllegalArgumentException("Managed annotation '" + managedAnnotationType.getName() + "' supports '" + typeName + "' but annotation '" + a.type().name() + "' was passed.");
      }

      A annotation = managedAnnotationType.newInstance();
      annotation.postConstruct(a);
      return annotation;
    }
    catch (Exception e) {
      throw new IllegalArgumentException("create " + managedAnnotationType.getName() + " with " + a, e);
    }
  }

  /**
   * @return the value of the static field TYPE_NAME each managed annotation must have.
   */
  public static String typeName(Class<? extends AbstractManagedAnnotation> a) {
    try {
      return (String) a.getField("TYPE_NAME").get(null);
    }
    catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
      throw new SdkException("failed to read field " + a.getName() + ".TYPE_NAME. Each managed annotation must define its fully qualified name using a field called 'TYPE_NAME'.", e);
    }
  }
}
