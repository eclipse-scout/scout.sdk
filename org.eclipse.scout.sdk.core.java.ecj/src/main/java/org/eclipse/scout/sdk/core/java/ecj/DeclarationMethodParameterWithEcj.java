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
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.createDeclarationAnnotations;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.getMethodFlags;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.hasDeprecatedAnnotation;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.resolveTypeOfArgument;

import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.java.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.java.model.api.internal.MethodParameterImplementor;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;

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
  private final FinalValue<SourceRange> m_source;
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
  public MethodParameterSpi internalFindNewElement() {
    var newMethod = getDeclaringMethod().internalFindNewElement();
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
    return m_dataType.computeIfAbsentAndGet(() -> bindingToType(javaEnvWithEcj(), resolveParameterType(this), () -> withNewElement(DeclarationMethodParameterWithEcj::resolveParameterType)));
  }

  protected static TypeBinding resolveParameterType(DeclarationMethodParameterWithEcj methodParam) {
    return resolveTypeOfArgument(methodParam.m_astNode, methodParam.getDeclaringMethod().getInternalMethodDeclaration().scope, methodParam.javaEnvWithEcj());
  }

  @Override
  public int getIndex() {
    return m_index;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = getMethodFlags(m_astNode.modifiers, false, hasDeprecatedAnnotation(getAnnotations()));
    }
    return m_flags;
  }

  @Override
  public List<DeclarationAnnotationWithEcj> getAnnotations() {
    return m_annotations.computeIfAbsentAndGet(() -> createDeclarationAnnotations(javaEnvWithEcj(), this, m_astNode.annotations));
  }

  @Override
  public SourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      var cu = m_declaringMethod.getDeclaringType().getCompilationUnit();
      var decl = m_astNode;
      return javaEnvWithEcj().getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
    });
  }
}
