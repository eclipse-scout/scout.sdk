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
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.MethodParameterImplementor;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 * <h3>{@link DeclarationMethodParameterWithJdt}</h3>
 *
 * @author Ivan Motsch
 * @since 3.8.0 2012-12-06
 */
public class DeclarationMethodParameterWithJdt extends AbstractJavaElementWithJdt<IMethodParameter> implements MethodParameterSpi {
  private final DeclarationMethodWithJdt m_declaringMethod;
  private final Argument m_astNode;
  private final int m_index;
  private String m_name;
  private TypeSpi m_dataType;
  private int m_flags;
  private List<DeclarationAnnotationWithJdt> m_annotations;
  private ISourceRange m_source;

  DeclarationMethodParameterWithJdt(JavaEnvironmentWithJdt env, DeclarationMethodWithJdt declaringMethod, Argument astNode, int index) {
    super(env);
    m_declaringMethod = Validate.notNull(declaringMethod);
    m_astNode = Validate.notNull(astNode);
    m_index = index;
    m_flags = -1;
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    MethodSpi newMethod = (MethodSpi) getDeclaringMethod().internalFindNewElement(newEnv);
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
  public DeclarationMethodWithJdt getDeclaringMethod() {
    return m_declaringMethod;
  }

  @Override
  public String getElementName() {
    if (m_name == null) {
      m_name = new String(m_astNode.name);
    }
    return m_name;
  }

  @Override
  public TypeSpi getDataType() {
    if (m_dataType == null) {
      if (m_astNode.type.resolvedType == null) {
        m_astNode.type.resolveType(m_declaringMethod.getInternalMethodDeclaration().scope);
      }
      m_dataType = SpiWithJdtUtils.bindingToType(m_env, m_astNode.type.resolvedType);
    }
    return m_dataType;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = SpiWithJdtUtils.getMethodFlags(m_astNode.modifiers, false, SpiWithJdtUtils.hasDeprecatedAnnotation(m_astNode.annotations));
    }
    return m_flags;
  }

  @Override
  public List<DeclarationAnnotationWithJdt> getAnnotations() {
    if (m_annotations != null) {
      return m_annotations;
    }
    m_annotations = SpiWithJdtUtils.createDeclarationAnnotations(m_env, this, m_astNode.annotations);
    return m_annotations;
  }

  @Override
  public ISourceRange getSource() {
    if (m_source == null) {
      CompilationUnitSpi cu = m_declaringMethod.getDeclaringType().getCompilationUnit();
      Argument decl = m_astNode;
      m_source = m_env.getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
    }
    return m_source;
  }
}
