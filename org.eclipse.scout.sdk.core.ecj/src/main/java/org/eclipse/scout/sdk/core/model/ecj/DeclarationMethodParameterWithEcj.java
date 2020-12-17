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
package org.eclipse.scout.sdk.core.model.ecj;

import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.MethodParameterImplementor;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 * <h3>{@link DeclarationMethodParameterWithEcj}</h3>
 *
 * @since 3.8.0 2012-12-06
 */
public class DeclarationMethodParameterWithEcj extends AbstractJavaElementWithEcj<IMethodParameter> implements MethodParameterSpi {
  private final DeclarationMethodWithEcj m_declaringMethod;
  private final Argument m_astNode;
  private final int m_index;
  private final FinalValue<String> m_name;
  private final FinalValue<TypeSpi> m_dataType;
  private final FinalValue<List<DeclarationAnnotationWithEcj>> m_annotations;
  private final FinalValue<ISourceRange> m_source;
  private int m_flags;

  protected DeclarationMethodParameterWithEcj(AbstractJavaEnvironment env, DeclarationMethodWithEcj declaringMethod, Argument astNode, int index) {
    super(env);
    m_declaringMethod = Ensure.notNull(declaringMethod);
    m_astNode = Ensure.notNull(astNode);
    m_index = index;
    m_flags = -1;
    m_name = new FinalValue<>();
    m_dataType = new FinalValue<>();
    m_annotations = new FinalValue<>();
    m_source = new FinalValue<>();
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    var newMethod = (MethodSpi) getDeclaringMethod().internalFindNewElement();
    if (newMethod != null && newMethod.getParameters().size() > m_index) {
      return newMethod.getParameters().get(m_index);
    }
    return null;
  }

  @Override
  protected IMethodParameter internalCreateApi() {
    return new MethodParameterImplementor(this);
  }

  public Argument getInternalArgument() {
    return m_astNode;
  }

  @Override
  public DeclarationMethodWithEcj getDeclaringMethod() {
    return m_declaringMethod;
  }

  @Override
  public String getElementName() {
    return m_name.computeIfAbsentAndGet(() -> new String(m_astNode.name));
  }

  @Override
  public TypeSpi getDataType() {
    return m_dataType.computeIfAbsentAndGet(() -> {
      if (m_astNode.type.resolvedType == null) {
        synchronized (javaEnvWithEcj().lock()) {
          m_astNode.type.resolveType(m_declaringMethod.getInternalMethodDeclaration().scope);
        }
      }
      return SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), m_astNode.type.resolvedType);
    });
  }

  @Override
  public int getIndex() {
    return m_index;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = SpiWithEcjUtils.getMethodFlags(m_astNode.modifiers, false, SpiWithEcjUtils.hasDeprecatedAnnotation(getAnnotations()));
    }
    return m_flags;
  }

  @Override
  public List<DeclarationAnnotationWithEcj> getAnnotations() {
    return m_annotations.computeIfAbsentAndGet(() -> SpiWithEcjUtils.createDeclarationAnnotations(javaEnvWithEcj(), this, m_astNode.annotations));
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      var cu = m_declaringMethod.getDeclaringType().getCompilationUnit();
      var decl = m_astNode;
      return javaEnvWithEcj().getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
    });
  }
}
