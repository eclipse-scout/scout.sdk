/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.model.api.internal.TypeParameterImplementor;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MemberSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 * <h3>{@link DeclarationTypeParameterWithJdt}</h3>
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class DeclarationTypeParameterWithJdt extends AbstractJavaElementWithJdt<ITypeParameter>implements TypeParameterSpi {
  private final AbstractMemberWithJdt<?> m_declaringMember;
  private final org.eclipse.jdt.internal.compiler.ast.TypeParameter m_astNode;
  private final int m_index;
  private final String m_name;
  private List<TypeSpi> m_bounds;

  DeclarationTypeParameterWithJdt(JavaEnvironmentWithJdt env, AbstractMemberWithJdt<?> declaringMember, org.eclipse.jdt.internal.compiler.ast.TypeParameter astNode, int index) {
    super(env);
    m_declaringMember = Validate.notNull(declaringMember);
    m_astNode = Validate.notNull(astNode);
    m_index = index;
    m_name = new String(m_astNode.name);
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    MemberSpi newMember = (MemberSpi) getDeclaringMember().internalFindNewElement(newEnv);
    if (newMember != null) {
      if (newMember.getTypeParameters().size() > m_index) {
        return newMember.getTypeParameters().get(m_index);
      }
    }
    return null;
  }

  @Override
  protected ITypeParameter internalCreateApi() {
    return new TypeParameterImplementor(this);
  }

  protected static char[] computeUniqueKey(org.eclipse.jdt.internal.compiler.ast.TypeParameter typeParam) {
    StringBuffer sig = new StringBuffer();
    typeParam.printStatement(0, sig);

    int sigLength = sig.length();
    char[] uniqueKey = new char[sigLength];
    sig.getChars(0, sigLength, uniqueKey, 0);
    return uniqueKey;
  }

  public org.eclipse.jdt.internal.compiler.ast.TypeParameter getInternalTypeParameter() {
    return m_astNode;
  }

  @Override
  public List<TypeSpi> getBounds() {
    if (m_bounds == null) {
      boolean hasType = m_astNode.type != null;
      boolean hasBounds = m_astNode.bounds != null && m_astNode.bounds.length > 0;
      int size = 0;
      if (hasType) {
        size++;
      }
      if (hasBounds) {
        size += m_astNode.bounds.length;
      }
      List<TypeSpi> result = new ArrayList<>(size);
      Scope scope = SpiWithJdtUtils.memberScopeOf(this);
      if (hasType) {
        if (m_astNode.type.resolvedType == null) {
          if (scope instanceof ClassScope) {
            m_astNode.type.resolveType((ClassScope) scope);
          }
          else {
            m_astNode.type.resolveType((BlockScope) scope);
          }
        }
        TypeSpi t = SpiWithJdtUtils.bindingToType(m_env, m_astNode.type.resolvedType);
        if (t != null) {
          result.add(t);
        }
      }
      if (hasBounds) {
        for (TypeReference r : m_astNode.bounds) {
          if (r.resolvedType == null) {
            if (scope instanceof ClassScope) {
              r.resolveType((ClassScope) scope);
            }
            else {
              r.resolveType((BlockScope) scope);
            }
          }
          TypeBinding b = r.resolvedType;
          if (b != null) {
            TypeSpi t = SpiWithJdtUtils.bindingToType(m_env, b);
            if (t != null) {
              result.add(t);
            }
          }
        }
      }
      m_bounds = result;
    }
    return m_bounds;

  }

  @Override
  public AbstractMemberWithJdt<?> getDeclaringMember() {
    return m_declaringMember;
  }

  @Override
  public String getElementName() {
    return m_name;
  }

  @Override
  public ISourceRange getSource() {
    CompilationUnitSpi cu = SpiWithJdtUtils.declaringTypeOf(this).getCompilationUnit();
    org.eclipse.jdt.internal.compiler.ast.TypeParameter decl = m_astNode;
    return m_env.getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
  }
}
