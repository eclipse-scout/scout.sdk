/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.FieldImplementor;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 *
 */
public class BindingFieldWithEcj extends AbstractMemberWithEcj<IField> implements FieldSpi {
  private final AbstractTypeWithEcj m_declaringType;
  private final FieldBinding m_binding;
  private final FinalValue<String> m_name;
  private final FinalValue<TypeSpi> m_type;
  private final FinalValue<List<BindingAnnotationWithEcj>> m_annotations;
  private final FinalValue<IMetaValue> m_constRef;
  private final FinalValue<ISourceRange> m_source;
  private final FinalValue<ISourceRange> m_initializerSource;
  private final FinalValue<ISourceRange> m_javaDocSource;
  private int m_flags;

  protected BindingFieldWithEcj(JavaEnvironmentWithEcj env, AbstractTypeWithEcj declaringType, FieldBinding binding) {
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
  public JavaElementSpi internalFindNewElement() {
    TypeSpi newType = (TypeSpi) getDeclaringType().internalFindNewElement();
    if (newType != null) {
      for (FieldSpi newF : newType.getFields()) {
        if (getElementName().equals(newF.getElementName())) {
          return newF;
        }
      }
    }
    return null;
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
      m_flags = SpiWithEcjUtils.getTypeFlags(m_binding.modifiers, null, SpiWithEcjUtils.hasDeprecatedAnnotation(m_binding.getAnnotations()));
    }
    return m_flags;
  }

  @Override
  public TypeSpi getDataType() {
    return m_type.computeIfAbsentAndGet(() -> SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), m_binding.type));
  }

  @Override
  public List<BindingAnnotationWithEcj> getAnnotations() {
    return m_annotations.computeIfAbsentAndGet(() -> SpiWithEcjUtils.createBindingAnnotations(javaEnvWithEcj(), this, m_binding.getAnnotations()));
  }

  @Override
  public String getElementName() {
    return m_name.computeIfAbsentAndGet(() -> new String(m_binding.name));
  }

  @Override
  public IMetaValue getConstantValue() {
    return m_constRef.computeIfAbsentAndGet(() -> {
      IMetaValue resolvedValue = SpiWithEcjUtils.resolveCompiledValue(javaEnvWithEcj(), this, m_binding.constant());
      if (resolvedValue != null) {
        return resolvedValue;
      }

      FieldBinding origBinding = m_binding.original();
      ReferenceBinding refBinding = origBinding.declaringClass;
      if (refBinding instanceof SourceTypeBinding) {
        SourceTypeBinding stb = (SourceTypeBinding) refBinding;
        Expression initEx = stb.scope.referenceContext.declarationOf(origBinding).initialization;
        return SpiWithEcjUtils.resolveCompiledValue(javaEnvWithEcj(), this, SpiWithEcjUtils.compileExpression(initEx, SpiWithEcjUtils.classScopeOf(this)));
      }
      return null;
    });
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
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      FieldDeclaration decl = m_binding.sourceField();
      if (decl != null) {
        CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
        return javaEnvWithEcj().getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
      }
      return null;
    });
  }

  @Override
  public ISourceRange getSourceOfInitializer() {
    return m_initializerSource.computeIfAbsentAndGet(() -> {
      FieldDeclaration decl = m_binding.sourceField();
      if (decl != null) {
        Expression expr = decl.initialization;
        if (expr != null) {
          CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
          return javaEnvWithEcj().getSource(cu, expr.sourceStart, expr.sourceEnd);
        }
      }
      return null;
    });
  }

  @Override
  public ISourceRange getJavaDoc() {
    return m_javaDocSource.computeIfAbsentAndGet(() -> {
      FieldDeclaration decl = m_binding.sourceField();
      if (decl == null) {
        return null;
      }
      return SpiWithEcjUtils.getJavaDocSource(decl.javadoc, m_declaringType, javaEnvWithEcj());
    });
  }
}
