/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.imports;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;

/**
 * <h3>{@link WrappedImportCollector}</h3> Import collector wrapper.
 *
 * @since 5.2.0
 */
public class WrappedImportCollector implements IImportCollector {
  private final IImportCollector m_inner;

  public WrappedImportCollector(IImportCollector inner) {
    m_inner = inner;
  }

  @Override
  public Optional<IJavaBuilderContext> getContext() {
    return m_inner.getContext();
  }

  @Override
  public Optional<IJavaEnvironment> getJavaEnvironment() {
    return m_inner.getJavaEnvironment();
  }

  @Override
  public String getQualifier() {
    return m_inner.getQualifier();
  }

  @Override
  public void addStaticImport(CharSequence fqn) {
    m_inner.addStaticImport(fqn);
  }

  @Override
  public void addImport(CharSequence fqn) {
    m_inner.addImport(fqn);
  }

  @Override
  public void reserveElement(TypeReferenceDescriptor candidate) {
    m_inner.reserveElement(candidate);
  }

  @Override
  public String registerElement(TypeReferenceDescriptor candidate) {
    return m_inner.registerElement(candidate);
  }

  @Override
  public String checkExistingImports(TypeReferenceDescriptor candidate) {
    return m_inner.checkExistingImports(candidate);
  }

  @Override
  public String checkCurrentScope(TypeReferenceDescriptor candidate) {
    return m_inner.checkCurrentScope(candidate);
  }

  @Override
  public Stream<StringBuilder> createImportDeclarations() {
    return m_inner.createImportDeclarations();
  }

  @Override
  public Stream<StringBuilder> getStaticImports() {
    return m_inner.getStaticImports();
  }

  @Override
  public Stream<StringBuilder> getImports() {
    return m_inner.getImports();
  }

  @Override
  public Stream<StringBuilder> createImportDeclarations(boolean includeExisting) {
    return m_inner.createImportDeclarations(includeExisting);
  }
}
