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
package org.eclipse.scout.sdk.core.testing.context;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * <h3>{@link AbstractContextExtension}</h3>
 * <p>
 * {@link ParameterResolver} extension helper that provides a context value. The value is configured using an
 * {@link Annotation}.
 *
 * @since 7.1.0
 */
public abstract class AbstractContextExtension<T, C extends Annotation> implements AfterEachCallback, AfterAllCallback, ParameterResolver {

  private final String m_contextKey;
  private final Class<? extends Annotation> m_annotationType;
  private final Class<T> m_contextType;

  protected AbstractContextExtension(String contextKey) {
    m_contextType = getTypeArgument(this, 0);
    m_annotationType = getTypeArgument(this, 1);
    m_contextKey = Ensure.notBlank(contextKey);
  }

  @SuppressWarnings("unchecked")
  private static <X> Class<X> getTypeArgument(Object reference, int index) {
    return (Class<X>) ((ParameterizedType) reference.getClass().getGenericSuperclass()).getActualTypeArguments()[index];
  }

  @Override
  public void afterAll(ExtensionContext context) {
    removeAndGet(context).ifPresent(this::close);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    removeAndGet(context).ifPresent(this::close);
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext context) {
    if (!isAnnotated(context.getRequiredTestMethod(), annotationType()) && !isAnnotated(context.getRequiredTestClass(), annotationType())) {
      return false;
    }
    return contextType().equals(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return getOrCreateContextFor(extensionContext.getRequiredTestMethod(), extensionContext);
  }

  @SuppressWarnings("unchecked")
  protected T getOrCreateContextFor(AnnotatedElement annotationOwner, ExtensionContext extensionContext) {
    return AnnotationSupport.findAnnotation(annotationOwner, annotationType())
        .map(annot -> getOrCreateContext((C) annot, extensionContext))
        .orElseGet(() -> extensionContext.getParent()
            .map(parentContext -> getOrCreateContextFor(extensionContext.getRequiredTestClass(), parentContext))
            .orElse(null));
  }

  /**
   * Called if a new context must be created.
   *
   * @param annotation
   *          The {@link Annotation} that configures the context instance.
   * @return The created instance. May be {@code null}.
   */
  protected abstract T annotationToContext(C annotation);

  /**
   * Called if a context is no longer needed. Clients may do any cleanup that is required.
   *
   * @param element
   *          The context. Is never {@code null}.
   * @see #closeResource(AutoCloseable)
   */
  protected abstract void close(T element);

  protected Store getStore(ExtensionContext context) {
    return context.getStore(Namespace.create(getClass(), context));
  }

  protected Optional<T> removeAndGet(ExtensionContext context) {
    return Optional.ofNullable(getStore(context).remove(contextKey(), contextType()));
  }

  protected T getOrCreateContext(C annot, ExtensionContext extensionContext) {
    return getStore(extensionContext)
        .getOrComputeIfAbsent(contextKey(), k -> annotationToContext(annot), contextType());
  }

  protected String contextKey() {
    return m_contextKey;
  }

  protected Class<T> contextType() {
    return m_contextType;
  }

  protected Class<? extends Annotation> annotationType() {
    return m_annotationType;
  }

  public static void closeResource(AutoCloseable c) {
    try {
      c.close();
    }
    catch (RuntimeException re) {
      throw re;
    }
    catch (Exception e) {
      throw new ParameterResolutionException("Unable to close resource.", e);
    }
  }
}
