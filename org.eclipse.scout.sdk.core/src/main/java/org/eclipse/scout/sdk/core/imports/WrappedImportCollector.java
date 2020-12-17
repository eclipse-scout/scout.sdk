/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.imports;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;

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
  public IJavaEnvironment getJavaEnvironment() {
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
  public void reserveElement(TypeReferenceDescriptor cand) {
    m_inner.reserveElement(cand);
  }

  @Override
  public String registerElement(TypeReferenceDescriptor cand) {
    return m_inner.registerElement(cand);
  }

  @Override
  public String checkExistingImports(TypeReferenceDescriptor cand) {
    return m_inner.checkExistingImports(cand);
  }

  @Override
  public String checkCurrentScope(TypeReferenceDescriptor cand) {
    return m_inner.checkCurrentScope(cand);
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
