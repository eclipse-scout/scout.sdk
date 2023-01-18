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

import static java.util.Collections.emptyList;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.bindingToType;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.createBindingAnnotations;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.sourceMethodOf;

import java.util.List;

import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.java.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.java.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.java.model.api.internal.MethodParameterImplementor;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

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

  public TypeBinding getInternalBinding() {
    return m_binding;
  }

  @Override
  public String getElementName() {
    return m_nameAsString.computeIfAbsentAndGet(() -> new String(m_name));
  }

  protected static TypeBinding getParameterType(BindingMethodParameterWithEcj mp) {
    return mp.m_binding;
  }

  @Override
  public TypeSpi getDataType() {
    return m_dataType.computeIfAbsentAndGet(() -> bindingToType(javaEnvWithEcj(), getParameterType(this), () -> withNewElement(BindingMethodParameterWithEcj::getParameterType)));
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
    return m_annotations.computeIfAbsentAndGet(this::computeAnnotations);
  }

  private List<BindingAnnotationWithEcj> computeAnnotations() {
    AnnotationBinding[][] annotations;
    synchronized (javaEnvWithEcj().lock()) {
      annotations = m_declaringMethod.getInternalBinding().getParameterAnnotations();
    }
    if (annotations == null || m_index >= annotations.length) {
      return emptyList();
    }
    return createBindingAnnotations(this, annotations[m_index]);
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      var declMethod = sourceMethodOf(m_declaringMethod);
      if (declMethod != null) {
        var cu = m_declaringMethod.getDeclaringType().getCompilationUnit();
        var decl = declMethod.arguments[m_index];
        return javaEnvWithEcj().getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
      }
      return null;
    });
  }
}
