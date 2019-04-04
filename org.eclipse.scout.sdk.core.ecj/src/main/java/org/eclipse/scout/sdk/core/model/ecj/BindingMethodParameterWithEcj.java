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
package org.eclipse.scout.sdk.core.model.ecj;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.MethodParameterImplementor;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 *
 */
public class BindingMethodParameterWithEcj extends AbstractJavaElementWithEcj<IMethodParameter> implements MethodParameterSpi {
  private final BindingMethodWithEcj m_declaringMethod;
  private final TypeBinding m_binding;
  private final int m_index;
  private final int m_flags;
  private final char[] m_name;
  private final FinalValue<TypeSpi> m_dataType;
  private final FinalValue<ISourceRange> m_source;
  private final FinalValue<String> m_nameAsString;
  private final FinalValue<List<BindingAnnotationWithEcj>> m_annotations;

  protected BindingMethodParameterWithEcj(AbstractJavaEnvironment env, BindingMethodWithEcj declaringMethod, TypeBinding binding, char[] name, int flags, int index) {
    super(env);
    m_index = index;
    m_flags = flags;
    m_name = Ensure.notNull(name);
    m_binding = Ensure.notNull(binding);
    m_declaringMethod = Ensure.notNull(declaringMethod);
    m_dataType = new FinalValue<>();
    m_source = new FinalValue<>();
    m_nameAsString = new FinalValue<>();
    m_annotations = new FinalValue<>();
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    MethodSpi newMethod = (MethodSpi) getDeclaringMethod().internalFindNewElement();
    if (newMethod != null && newMethod.getParameters().size() > m_index) {
      return newMethod.getParameters().get(m_index);
    }
    return null;
  }

  @Override
  protected IMethodParameter internalCreateApi() {
    return new MethodParameterImplementor(this);
  }

  public TypeBinding getInternalBinding() {
    return m_binding;
  }

  @Override
  public String getElementName() {
    return m_nameAsString.computeIfAbsentAndGet(() -> new String(m_name));
  }

  @Override
  public TypeSpi getDataType() {
    return m_dataType.computeIfAbsentAndGet(() -> SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), m_binding));
  }

  @Override
  public int getIndex() {
    return m_index;
  }

  @Override
  public int getFlags() {
    return m_flags;
  }

  @Override
  public BindingMethodWithEcj getDeclaringMethod() {
    return m_declaringMethod;
  }

  @Override
  public List<BindingAnnotationWithEcj> getAnnotations() {
    return m_annotations.computeIfAbsentAndGet(() -> {
      AnnotationBinding[][] a = m_declaringMethod.getInternalBinding().getParameterAnnotations();
      if (a == null || m_index >= a.length) {
        return emptyList();
      }
      return SpiWithEcjUtils.createBindingAnnotations(javaEnvWithEcj(), this, a[m_index]);
    });
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      AbstractMethodDeclaration declMethod = SpiWithEcjUtils.sourceMethodOf(m_declaringMethod);
      if (declMethod != null) {
        CompilationUnitSpi cu = m_declaringMethod.getDeclaringType().getCompilationUnit();
        Argument decl = declMethod.arguments[m_index];
        return javaEnvWithEcj().getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
      }
      return null;
    });
  }
}
