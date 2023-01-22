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
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.compileExpression;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.createBindingAnnotations;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.createSourceRange;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.getTypeFlags;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.hasDeprecatedAnnotation;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.resolveCompiledValue;

import java.util.List;

import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.java.model.api.IField;
import org.eclipse.scout.sdk.core.java.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.java.model.api.internal.FieldImplementor;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class BindingFieldWithEcj extends AbstractMemberWithEcj<IField> implements FieldSpi {
  private final AbstractTypeWithEcj m_declaringType;
  private final FieldBinding m_binding;
  private final FinalValue<String> m_name;
  private final FinalValue<TypeSpi> m_type;
  private final FinalValue<List<BindingAnnotationWithEcj>> m_annotations;
  private final FinalValue<IMetaValue> m_constRef;
  private final FinalValue<SourceRange> m_source;
  private final FinalValue<SourceRange> m_initializerSource;
  private final FinalValue<SourceRange> m_javaDocSource;
  private int m_flags;

  protected BindingFieldWithEcj(AbstractJavaEnvironment env, AbstractTypeWithEcj declaringType, FieldBinding binding) {
    super(env);
    m_declaringType = Ensure.notNull(declaringType);
    m_binding = Ensure.notNull(binding);
    m_flags = -1;
    m_name = new FinalValue<>();
    m_type = new FinalValue<>();
    m_annotations = new FinalValue<>();
    m_constRef = new FinalValue<>();
    m_source = new FinalValue<>();
    m_initializerSource = new FinalValue<>();
    m_javaDocSource = new FinalValue<>();
  }

  @Override
  public FieldSpi internalFindNewElement() {
    var newType = (TypeSpi) getDeclaringType().internalFindNewElement();
    if (newType == null) {
      return null;
    }
    var thisElementName = getElementName();
    return newType.getFields().stream()
        .filter(newF -> thisElementName.equals(newF.getElementName()))
        .findFirst()
        .orElse(null);
  }

  @Override
  protected IField internalCreateApi() {
    return new FieldImplementor(this);
  }

  public FieldBinding getInternalBinding() {
    return m_binding;
  }

  @Override
  public AbstractTypeWithEcj getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = getTypeFlags(m_binding.modifiers, null, hasDeprecatedAnnotation(getAnnotations()));
    }
    return m_flags;
  }

  protected static TypeBinding getFieldType(BindingFieldWithEcj f) {
    return f.m_binding.type;
  }

  @Override
  public TypeSpi getDataType() {
    return m_type.computeIfAbsentAndGet(() -> bindingToType(javaEnvWithEcj(), getFieldType(this), () -> withNewElement(BindingFieldWithEcj::getFieldType)));
  }

  @Override
  public List<BindingAnnotationWithEcj> getAnnotations() {
    return m_annotations.computeIfAbsentAndGet(() -> createBindingAnnotations(this, m_binding));
  }

  @Override
  public String getElementName() {
    return m_name.computeIfAbsentAndGet(() -> new String(m_binding.name));
  }

  protected static Object getFieldValue(BindingFieldWithEcj f) {
    return f.m_binding.constant();
  }

  @Override
  public IMetaValue getConstantValue() {
    return m_constRef.computeIfAbsentAndGet(() -> {
      var resolvedValue = resolveCompiledValue(javaEnvWithEcj(), this, getFieldValue(this), () -> withNewElement(BindingFieldWithEcj::getFieldValue));
      if (resolvedValue != null) {
        return resolvedValue;
      }
      return resolveCompiledValue(javaEnvWithEcj(), this, resolveExpressionOf(this), () -> withNewElement(this::resolveExpressionOf));
    });
  }

  protected Object resolveExpressionOf(BindingFieldWithEcj field) {
    var origBinding = field.m_binding.original();
    var refBinding = origBinding.declaringClass;
    if (refBinding instanceof SourceTypeBinding stb) {
      var initEx = stb.scope.referenceContext.declarationOf(origBinding).initialization;
      return compileExpression(initEx, stb.scope, javaEnvWithEcj());
    }
    return null;
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    return emptyList();
  }

  @Override
  public boolean hasTypeParameters() {
    return false;
  }

  @Override
  public SourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      var decl = m_binding.sourceField();
      if (decl == null) {
        return null;
      }
      return javaEnvWithEcj().getSource(m_declaringType.getCompilationUnit(), decl.declarationSourceStart, decl.declarationSourceEnd);
    });
  }

  @Override
  public SourceRange getSourceOfInitializer() {
    return m_initializerSource.computeIfAbsentAndGet(() -> {
      var decl = m_binding.sourceField();
      if (decl == null) {
        return null;
      }
      return createSourceRange(decl.initialization, m_declaringType.getCompilationUnit(), javaEnvWithEcj());
    });
  }

  @Override
  public SourceRange getJavaDoc() {
    return m_javaDocSource.computeIfAbsentAndGet(() -> {
      var decl = m_binding.sourceField();
      if (decl == null) {
        return null;
      }
      return createSourceRange(decl.javadoc, m_declaringType.getCompilationUnit(), javaEnvWithEcj());
    });
  }
}
