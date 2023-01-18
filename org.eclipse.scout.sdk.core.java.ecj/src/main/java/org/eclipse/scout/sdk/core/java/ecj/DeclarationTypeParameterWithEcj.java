/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.bindingToType;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.bindingsToTypes;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.declaringTypeOf;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.memberScopeOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.java.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.java.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.java.model.api.internal.TypeParameterImplementor;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.MemberSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 * <h3>{@link DeclarationTypeParameterWithEcj}</h3>
 *
 * @since 5.1.0
 */
public class DeclarationTypeParameterWithEcj extends AbstractJavaElementWithEcj<ITypeParameter> implements TypeParameterSpi {
  private final AbstractMemberWithEcj<?> m_declaringMember;
  private final TypeParameter m_astNode;
  private final int m_index;
  private final String m_name;
  private final FinalValue<List<TypeSpi>> m_bounds;
  private final FinalValue<ISourceRange> m_source;

  protected DeclarationTypeParameterWithEcj(AbstractJavaEnvironment env, AbstractMemberWithEcj<?> declaringMember, TypeParameter astNode, int index) {
    super(env);
    m_declaringMember = Ensure.notNull(declaringMember);
    m_astNode = Ensure.notNull(astNode);
    m_index = index;
    m_name = new String(m_astNode.name);
    m_bounds = new FinalValue<>();
    m_source = new FinalValue<>();
  }

  protected TypeBinding ensureResolvedType(Scope scope, Expression r) {
    var resolvedType = r.resolvedType;
    if (resolvedType != null) {
      return resolvedType;
    }

    synchronized (javaEnvWithEcj().lock()) {
      if (scope instanceof ClassScope) {
        r.resolveType((ClassScope) scope);
      }
      else {
        r.resolveType((BlockScope) scope);
      }
    }
    return r.resolvedType;
  }

  @Override
  public TypeParameterSpi internalFindNewElement() {
    var newMember = (MemberSpi) getDeclaringMember().internalFindNewElement();
    if (newMember != null && newMember.getTypeParameters().size() > m_index) {
      return newMember.getTypeParameters().get(m_index);
    }
    return null;
  }

  @Override
  protected ITypeParameter internalCreateApi() {
    return new TypeParameterImplementor(this);
  }

  public TypeParameter getInternalTypeParameter() {
    return m_astNode;
  }

  @Override
  public List<TypeSpi> getBounds() {
    return m_bounds.computeIfAbsentAndGet(this::computeBounds);
  }

  protected List<TypeSpi> computeBounds() {
    var hasType = m_astNode.type != null;
    var hasBounds = m_astNode.bounds != null && m_astNode.bounds.length > 0;
    var size = 0;
    if (hasType) {
      size++;
    }
    if (hasBounds) {
      size += m_astNode.bounds.length;
    }
    List<TypeSpi> bounds = new ArrayList<>(size);
    if (hasType) {
      var t = bindingToType(javaEnvWithEcj(), resolveType(this), () -> withNewElement(this::resolveType));
      if (t != null) {
        bounds.add(t);
      }
    }
    if (hasBounds) {
      bounds.addAll(bindingsToTypes(javaEnvWithEcj(), resolveBounds(this), () -> withNewElement(this::resolveBounds)));
    }
    return bounds;
  }

  protected TypeBinding resolveType(DeclarationTypeParameterWithEcj parameter) {
    var scope = memberScopeOf(parameter);
    return ensureResolvedType(scope, parameter.m_astNode.type);
  }

  protected TypeBinding[] resolveBounds(DeclarationTypeParameterWithEcj parameter) {
    var scope = memberScopeOf(parameter);
    return Arrays.stream(parameter.m_astNode.bounds)
        .map(b -> ensureResolvedType(scope, b))
        .toArray(TypeBinding[]::new);
  }

  @Override
  public AbstractMemberWithEcj<?> getDeclaringMember() {
    return m_declaringMember;
  }

  @Override
  public String getElementName() {
    return m_name;
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      var cu = declaringTypeOf(this).getCompilationUnit();
      var decl = m_astNode;
      return javaEnvWithEcj().getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
    });
  }
}
