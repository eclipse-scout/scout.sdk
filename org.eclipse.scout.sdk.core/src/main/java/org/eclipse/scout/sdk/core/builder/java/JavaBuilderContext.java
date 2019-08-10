/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.builder.java;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.builder.IBuilderContext;
import org.eclipse.scout.sdk.core.imports.IImportValidator;
import org.eclipse.scout.sdk.core.imports.ImportCollector;
import org.eclipse.scout.sdk.core.imports.ImportValidator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.PropertySupport;

/**
 * <h3>{@link JavaBuilderContext}</h3>
 *
 * @since 6.1.0
 */
public class JavaBuilderContext implements IJavaBuilderContext {

  private final IImportValidator m_validator;
  private final IJavaEnvironment m_env;
  private final IBuilderContext m_inner;

  public JavaBuilderContext() {
    this(null, null, null);
  }

  public JavaBuilderContext(IBuilderContext inner) {
    this(inner, null, null);
  }

  public JavaBuilderContext(IJavaEnvironment env) {
    this(null, env, null);
  }

  public JavaBuilderContext(IBuilderContext inner, IJavaEnvironment env) {
    this(inner, env, null);
  }

  public JavaBuilderContext(IBuilderContext inner, IImportValidator validator) {
    this(inner, validator == null ? null : validator.importCollector().getJavaEnvironment(), validator);
  }

  protected JavaBuilderContext(IBuilderContext inner, IJavaEnvironment env, IImportValidator validator) {
    m_inner = Optional.ofNullable(inner).orElseGet(BuilderContext::new);
    m_env = env;
    m_validator = Optional.ofNullable(validator).orElseGet(() -> new ImportValidator(new ImportCollector(env)));
  }

  @Override
  public Optional<IJavaEnvironment> environment() {
    return Optional.ofNullable(m_env);
  }

  @Override
  public String lineDelimiter() {
    return builderContext().lineDelimiter();
  }

  @Override
  public PropertySupport properties() {
    return builderContext().properties();
  }

  @Override
  public IImportValidator validator() {
    return m_validator;
  }

  public IBuilderContext builderContext() {
    return m_inner;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int h = prime + (m_env == null ? 0 : m_env.hashCode());
    return prime * h + m_inner.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    JavaBuilderContext other = (JavaBuilderContext) obj;
    return Objects.equals(m_env, other.m_env)
        && Objects.equals(m_inner, other.m_inner);
  }
}
