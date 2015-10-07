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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
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

/**
 *
 */
public class BindingFieldWithJdt extends AbstractMemberWithJdt<IField> implements FieldSpi {
  private final AbstractTypeWithJdt m_declaringType;
  private final FieldBinding m_binding;
  private int m_flags;
  private String m_name;
  private TypeSpi m_type;
  private List<BindingAnnotationWithJdt> m_annotations;
  private AtomicReference<IMetaValue> m_constRef;
  private FieldSpi m_originalField;

  BindingFieldWithJdt(JavaEnvironmentWithJdt env, AbstractTypeWithJdt declaringType, FieldBinding binding) {
    super(env);
    m_declaringType = Validate.notNull(declaringType);
    m_binding = Validate.notNull(binding);
    m_flags = -1;
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    TypeSpi newType = (TypeSpi) getDeclaringType().internalFindNewElement(newEnv);
    if (newType != null) {
      for (FieldSpi newF : newType.getFields()) {
        if (this.getElementName().equals(newF.getElementName())) {
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
  public AbstractTypeWithJdt getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = SpiWithJdtUtils.getTypeFlags(m_binding.modifiers, null, SpiWithJdtUtils.hasDeprecatedAnnotation(m_binding.getAnnotations()));
    }
    return m_flags;
  }

  @Override
  public TypeSpi getDataType() {
    if (m_type == null) {
      m_type = SpiWithJdtUtils.bindingToType(m_env, m_binding.type);
    }
    return m_type;
  }

  @Override
  public List<BindingAnnotationWithJdt> getAnnotations() {
    if (m_annotations == null) {
      m_annotations = SpiWithJdtUtils.createBindingAnnotations(m_env, this, m_binding.getAnnotations());
    }
    return m_annotations;
  }

  @Override
  public String getElementName() {
    if (m_name == null) {
      m_name = new String(m_binding.name);
    }
    return m_name;
  }

  @Override
  public IMetaValue getConstantValue() {
    if (m_constRef == null) {
      IMetaValue resolvedValue = SpiWithJdtUtils.resolveCompiledValue(m_env, this, m_binding.constant());
      if (resolvedValue != null) {
        m_constRef = new AtomicReference<>(resolvedValue);
        return m_constRef.get();
      }

      FieldBinding origBinding = m_binding.original();
      ReferenceBinding refBinding = origBinding.declaringClass;
      if (refBinding instanceof SourceTypeBinding) {
        SourceTypeBinding stb = (SourceTypeBinding) refBinding;
        Expression initEx = stb.scope.referenceContext.declarationOf(origBinding).initialization;
        resolvedValue = SpiWithJdtUtils.resolveCompiledValue(m_env, this, SpiWithJdtUtils.compileExpression(initEx, SpiWithJdtUtils.classScopeOf(this)));
        if (resolvedValue != null) {
          m_constRef = new AtomicReference<>(resolvedValue);
          return m_constRef.get();
        }
      }
      m_constRef = new AtomicReference<>(null);
    }
    return m_constRef.get();
  }

  @Override
  public FieldSpi getOriginalField() {
    if (m_originalField == null) {
      FieldBinding ref = m_binding.original();
      if (ref == null || ref == m_binding) {
        m_originalField = this;
      }
      else {
        BindingTypeWithJdt refType = (BindingTypeWithJdt) m_declaringType.getOriginalType();
        m_originalField = m_env.createBindingField(refType, ref);
      }
    }
    return m_originalField;
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    return Collections.emptyList();
  }

  @Override
  public boolean hasTypeParameters() {
    return false;
  }

  @Override
  public ISourceRange getSource() {
    FieldDeclaration decl = m_binding.sourceField();
    if (decl != null) {
      CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
      return m_env.getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
    }
    return null;
  }

  @Override
  public ISourceRange getSourceOfInitializer() {
    FieldDeclaration decl = m_binding.sourceField();
    if (decl != null) {
      Expression expr = decl.initialization;
      if (expr != null) {
        CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
        return m_env.getSource(cu, expr.sourceStart, expr.sourceEnd);
      }
    }
    return null;
  }

  @Override
  public ISourceRange getJavaDoc() {
    FieldDeclaration decl = m_binding.sourceField();
    if (decl != null) {
      Javadoc doc = decl.javadoc;
      if (doc != null) {
        CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
        return m_env.getSource(cu, doc.sourceStart, doc.sourceEnd);
      }
    }
    return null;
  }

}
