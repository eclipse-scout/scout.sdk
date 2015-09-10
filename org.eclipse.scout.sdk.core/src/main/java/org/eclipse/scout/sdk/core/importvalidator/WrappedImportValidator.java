package org.eclipse.scout.sdk.core.importvalidator;

import java.util.Collection;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;

public class WrappedImportValidator implements IImportValidator {
  private final IImportValidator m_inner;

  public WrappedImportValidator(IImportValidator inner) {
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
  public void reserveElement(ImportElementCandidate cand) {
    m_inner.reserveElement(cand);
  }

  @Override
  public String registerElement(ImportElementCandidate cand) {
    return m_inner.registerElement(cand);
  }

  @Override
  public String checkExistingImports(ImportElementCandidate cand) {
    return m_inner.checkExistingImports(cand);
  }

  @Override
  public String checkCurrentScope(ImportElementCandidate cand) {
    return m_inner.checkCurrentScope(cand);
  }

  @Override
  public Collection<String> createImportDeclarations() {
    return m_inner.createImportDeclarations();
  }

}
