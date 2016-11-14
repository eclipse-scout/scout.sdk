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
package org.eclipse.scout.sdk.core.importcollector;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.signature.SignatureDescriptor;

/**
 * <h3>{@link WrappedImportCollector}</h3> Import collector wrapper.
 *
 * @author Ivan Motsch
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
  public void addStaticImport(String fqn) {
    m_inner.addStaticImport(fqn);
  }

  @Override
  public void addImport(String fqn) {
    m_inner.addImport(fqn);
  }

  @Override
  public void reserveElement(SignatureDescriptor cand) {
    m_inner.reserveElement(cand);
  }

  @Override
  public String registerElement(SignatureDescriptor cand) {
    return m_inner.registerElement(cand);
  }

  @Override
  public String checkExistingImports(SignatureDescriptor cand) {
    return m_inner.checkExistingImports(cand);
  }

  @Override
  public String checkCurrentScope(SignatureDescriptor cand) {
    return m_inner.checkCurrentScope(cand);
  }

  @Override
  public List<String> createImportDeclarations() {
    return m_inner.createImportDeclarations();
  }

  @Override
  public Collection<String> getStaticImports() {
    return m_inner.getStaticImports();
  }

  @Override
  public Collection<String> getImports() {
    return m_inner.getImports();
  }

  @Override
  public List<String> createImportDeclarations(boolean includeExisting) {
    return m_inner.createImportDeclarations(includeExisting);
  }
}