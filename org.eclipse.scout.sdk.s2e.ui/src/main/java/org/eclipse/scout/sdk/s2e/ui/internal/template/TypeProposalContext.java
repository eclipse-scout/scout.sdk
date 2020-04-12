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
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;

/**
 * <h3>{@link TypeProposalContext}</h3>
 *
 * @since 5.2.0
 */
public class TypeProposalContext {

  private final FinalValue<TypeDeclaration> m_declaringType;
  private final FinalValue<ITypeBinding> m_declaringTypeBinding;
  private String m_proposalIfcTypeFqn;
  private int m_pos;
  private List<String> m_defaultSuperClasses;
  private String m_defaultName;
  private String m_suffix;
  private String m_searchString;
  private Future<EclipseEnvironment> m_provider;
  private Future<CompilationUnit> m_compilationUnit;
  private ICompilationUnit m_icu;
  private ISourceRange m_surroundingTypeNameRange;

  public TypeProposalContext() {
    m_declaringType = new FinalValue<>();
    m_declaringTypeBinding = new FinalValue<>();
  }

  public TypeDeclaration getDeclaringType() {
    return m_declaringType.computeIfAbsentAndGet(() -> {
      CompilationUnit cu = Ensure.notNull(getCompilationUnit(), "No AST found for '{}'.", m_icu.getElementName());
      ASTNode surroundingTypeName = NodeFinder.perform(cu, getSurroundingTypeNameRange());
      return Ensure.notNull(ASTNodes.getParent(surroundingTypeName, TypeDeclaration.class));
    });
  }

  public int getInsertPosition() {
    return m_pos;
  }

  void setPosition(int pos) {
    m_pos = pos;
  }

  public List<String> getDefaultSuperClasses() {
    return m_defaultSuperClasses;
  }

  void setDefaultSuperClasses(Collection<String> defaultSuperClasses) {
    m_defaultSuperClasses = new ArrayList<>(defaultSuperClasses);
  }

  public IJavaEnvironment getJavaEnvironment() {
    return getProvider().toScoutJavaEnvironment(m_icu.getJavaProject());
  }

  public String getDefaultName() {
    return m_defaultName;
  }

  void setDefaultName(String defaultName) {
    m_defaultName = defaultName;
  }

  public String getSuffix() {
    return m_suffix;
  }

  void setSuffix(String suffix) {
    m_suffix = suffix;
  }

  public ITypeBinding getDeclaringTypeBinding() {
    return m_declaringTypeBinding.computeIfAbsentAndGet(() -> Ensure.notNull(getDeclaringType().resolveBinding(), "No type binding available for '{}'.", getDeclaringType()));
  }

  public EclipseEnvironment getProvider() {
    try {
      return m_provider.get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new SdkException(e);
    }
  }

  void setProvider(Future<EclipseEnvironment> provider) {
    m_provider = provider;
  }

  public String getProposalInterfaceFqn() {
    return m_proposalIfcTypeFqn;
  }

  void setProposalInterfaceFqn(String proposalIfcTypeFqn) {
    m_proposalIfcTypeFqn = proposalIfcTypeFqn;
  }

  public ICompilationUnit getIcu() {
    return m_icu;
  }

  void setIcu(ICompilationUnit icu) {
    m_icu = icu;
  }

  public String getSearchString() {
    return m_searchString;
  }

  void setSearchString(String searchString) {
    m_searchString = searchString;
  }

  public CompilationUnit getCompilationUnit() {
    try {
      return Ensure.notNull(m_compilationUnit).get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new SdkException(e);
    }
  }

  void setCompilationUnit(Future<CompilationUnit> compilationUnit) {
    m_compilationUnit = compilationUnit;
  }

  public ISourceRange getSurroundingTypeNameRange() {
    return m_surroundingTypeNameRange;
  }

  void setSurroundingTypeNameRange(ISourceRange surroundingTypeNameRange) {
    m_surroundingTypeNameRange = surroundingTypeNameRange;
  }
}
