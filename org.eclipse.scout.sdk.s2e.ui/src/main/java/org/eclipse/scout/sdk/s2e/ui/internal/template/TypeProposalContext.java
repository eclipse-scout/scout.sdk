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
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;

/**
 * <h3>{@link TypeProposalContext}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class TypeProposalContext {

  private String m_proposalIfcTypeFqn;
  private TypeDeclaration m_declaringType;
  private int m_pos;
  private List<String> m_defaultSuperClasses;
  private String m_defaultName;
  private String m_suffix;
  private String m_searchString;
  private ITypeBinding m_declaringTypeBinding;
  private Future<IJavaEnvironmentProvider> m_provider;
  private Future<CompilationUnit> m_compilationUnit;
  private ICompilationUnit m_icu;

  public TypeDeclaration getDeclaringType() {
    if (m_declaringType == null) {
      CompilationUnit cu = Validate.notNull(getCompilationUnit(), "No AST found for '%s'.", m_icu.getElementName());
      ASTNode coveringNode = NodeFinder.perform(cu, m_pos, 0);
      Validate.isTrue(coveringNode instanceof TypeDeclaration, "No valid covering node found.");
      m_declaringType = (TypeDeclaration) coveringNode;
    }
    return m_declaringType;
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
    return getProvider().get(m_icu.getJavaProject());
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
    if (m_declaringTypeBinding == null) {
      TypeDeclaration declaringType = getDeclaringType();
      m_declaringTypeBinding = Validate.notNull(declaringType.resolveBinding(), "No type binding available for '%s'.", declaringType.getName().getFullyQualifiedName());
    }
    return m_declaringTypeBinding;
  }

  public IJavaEnvironmentProvider getProvider() {
    try {
      return m_provider.get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new SdkException(e);
    }
  }

  void setProvider(Future<IJavaEnvironmentProvider> provider) {
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
      return Validate.notNull(m_compilationUnit).get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new SdkException(e);
    }
  }

  void setCompilationUnit(Future<CompilationUnit> compilationUnit) {
    m_compilationUnit = compilationUnit;
  }
}
