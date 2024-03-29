/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.builder;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.builder.IBuilderContext;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.imports.IImportCollector;
import org.eclipse.scout.sdk.core.java.imports.IImportValidator;
import org.eclipse.scout.sdk.core.java.imports.ImportCollector;
import org.eclipse.scout.sdk.core.java.imports.ImportValidator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
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
    this(inner, Optional.ofNullable(validator)
        .map(IImportValidator::importCollector)
        .flatMap(IImportCollector::getJavaEnvironment)
        .orElse(null), validator);
  }

  protected JavaBuilderContext(IBuilderContext inner, IJavaEnvironment env, IImportValidator validator) {
    m_inner = Optional.ofNullable(inner).orElseGet(BuilderContext::new);
    m_env = env;
    m_validator = Optional.ofNullable(validator).orElseGet(() -> new ImportValidator(new ImportCollector(this)));
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

  @Override
  public <A extends IApiSpecification> Optional<A> api(Class<A> apiDefinition) {
    return environment().flatMap(env -> env.api(apiDefinition));
  }

  @Override
  public <A extends IApiSpecification> A requireApi(Class<A> apiDefinition) {
    return environment()
        .map(env -> env.requireApi(apiDefinition))
        .orElseThrow(() -> newFail("Cannot compute API because no Java environment is available."));
  }

  public IBuilderContext builderContext() {
    return m_inner;
  }

  @Override
  public int hashCode() {
    var prime = 31;
    var h = prime + (m_env == null ? 0 : m_env.hashCode());
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
    var other = (JavaBuilderContext) obj;
    return Objects.equals(m_env, other.m_env)
        && Objects.equals(m_inner, other.m_inner);
  }
}
