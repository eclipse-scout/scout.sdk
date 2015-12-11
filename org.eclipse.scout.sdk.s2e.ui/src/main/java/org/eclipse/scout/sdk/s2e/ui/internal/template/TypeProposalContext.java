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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.s2e.trigger.IJavaEnvironmentProvider;

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
  private ITypeBinding m_declaringTypeBinding;
  private IJavaEnvironmentProvider m_provider;
  private ICompilationUnit m_icu;

  public TypeDeclaration getDeclaringType() {
    return m_declaringType;
  }

  public void setDeclaringType(TypeDeclaration declaringType) {
    m_declaringType = declaringType;
  }

  public int getInsertPosition() {
    return m_pos;
  }

  public void setPosition(int pos) {
    m_pos = pos;
  }

  public List<String> getDefaultSuperClasses() {
    return m_defaultSuperClasses;
  }

  public void setDefaultSuperClass(Collection<String> defaultSuperClasses) {
    m_defaultSuperClasses = new ArrayList<>(defaultSuperClasses);
  }

  public IJavaEnvironment getJavaEnvironment() {
    synchronized (m_provider) {
      return m_provider.get(m_icu.getJavaProject());
    }
  }

  public String getDefaultName() {
    return m_defaultName;
  }

  public void setDefaultName(String defaultName) {
    m_defaultName = defaultName;
  }

  public String getSuffix() {
    return m_suffix;
  }

  public void setSuffix(String suffix) {
    m_suffix = suffix;
  }

  public ITypeBinding getDeclaringTypeBinding() {
    return m_declaringTypeBinding;
  }

  public void setDeclaringTypeBinding(ITypeBinding declaringTypeBinding) {
    m_declaringTypeBinding = declaringTypeBinding;
  }

  public IJavaEnvironmentProvider getProvider() {
    return m_provider;
  }

  public void setProvider(IJavaEnvironmentProvider provider) {
    m_provider = provider;
  }

  public String getProposalInterfaceFqn() {
    return m_proposalIfcTypeFqn;
  }

  public void setProposalInterfaceFqn(String proposalIfcTypeFqn) {
    m_proposalIfcTypeFqn = proposalIfcTypeFqn;
  }

  public ICompilationUnit getIcu() {
    return m_icu;
  }

  public void setIcu(ICompilationUnit icu) {
    m_icu = icu;
  }
}
