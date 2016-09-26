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
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.FieldImplementor;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

public class DeclarationFieldWithJdt extends AbstractMemberWithJdt<IField> implements FieldSpi {
  private final DeclarationTypeWithJdt m_declaringType;
  private final FieldDeclaration m_astNode;
  private final char[] m_name;
  private int m_flags;
  private String m_nameS;
  private TypeSpi m_type;
  private List<DeclarationAnnotationWithJdt> m_annotations;
  private AtomicReference<IMetaValue> m_constRef;
  private ISourceRange m_source;
  private ISourceRange m_initSource;
  private ISourceRange m_javaDocSource;

  DeclarationFieldWithJdt(JavaEnvironmentWithJdt env, DeclarationTypeWithJdt declaringType, FieldDeclaration astNode) {
    super(env);
    m_declaringType = Validate.notNull(declaringType);
    m_astNode = Validate.notNull(astNode);
    m_name = astNode.name != null ? astNode.name : new char[0];//static { } has name =""
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

  public FieldDeclaration getInternalFieldDeclaration() {
    return m_astNode;
  }

  @Override
  public DeclarationTypeWithJdt getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public IMetaValue getConstantValue() {
    if (m_constRef == null) {
      if (m_astNode.initialization != null) {
        Object compiledValue = SpiWithJdtUtils.compileExpression(m_astNode.initialization, null);
        IMetaValue resolvedValue = SpiWithJdtUtils.resolveCompiledValue(m_env, this, compiledValue);
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
  public TypeSpi getDataType() {
    if (m_type == null) {
      if (m_astNode.type == null) {
        //static{ } section
        m_type = m_env.createVoidType();
        return m_type;
      }
      TypeBinding tb = m_astNode.type.resolvedType;
      if (tb == null) {
        tb = m_astNode.type.resolveType(SpiWithJdtUtils.classScopeOf(this));
      }
      m_type = SpiWithJdtUtils.bindingToType(m_env, tb);
    }
    return m_type;
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
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = SpiWithJdtUtils.getTypeFlags(m_astNode.modifiers, null, SpiWithJdtUtils.hasDeprecatedAnnotation(m_astNode.annotations));
    }
    return m_flags;
  }

  @Override
  public String getElementName() {
    if (m_nameS == null) {
      m_nameS = new String(m_name);
    }
    return m_nameS;
  }

  @Override
  public FieldSpi getOriginalField() {
    return this;
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
    if (m_source == null) {
      CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
      FieldDeclaration decl = m_astNode;
      m_source = m_env.getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
    }
    return m_source;
  }

  @Override
  public ISourceRange getSourceOfInitializer() {
    if (m_initSource == null) {
      CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
      if (m_astNode instanceof Initializer) {
        // static initializer
        Initializer decl = (Initializer) m_astNode;
        m_initSource = m_env.getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
      }
      else {
        Expression decl = m_astNode.initialization;
        if (decl != null) {
          m_initSource = m_env.getSource(cu, decl.sourceStart, decl.sourceEnd);
        }
        else {
          m_initSource = ISourceRange.NO_SOURCE;
        }
      }
    }
    return m_initSource;
  }

  @Override
  public ISourceRange getJavaDoc() {
    if (m_javaDocSource == null) {
      FieldDeclaration decl = m_astNode;
      Javadoc doc = decl.javadoc;
      if (doc != null) {
        CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
        m_javaDocSource = m_env.getSource(cu, doc.sourceStart, doc.sourceEnd);
      }
      else {
        m_javaDocSource = ISourceRange.NO_SOURCE;
      }
    }
    return m_javaDocSource;
  }
}
