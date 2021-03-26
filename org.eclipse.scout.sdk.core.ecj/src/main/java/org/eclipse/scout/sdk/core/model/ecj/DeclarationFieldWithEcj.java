/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static java.util.Collections.emptyList;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.bindingToType;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.compileExpression;

import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.FieldImplementor;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

public class DeclarationFieldWithEcj extends AbstractMemberWithEcj<IField> implements FieldSpi {
  private final DeclarationTypeWithEcj m_declaringType;
  private final FieldDeclaration m_astNode;
  private final char[] m_name;
  private final FinalValue<String> m_nameAsString;
  private final FinalValue<TypeSpi> m_type;
  private final FinalValue<List<DeclarationAnnotationWithEcj>> m_annotations;
  private final FinalValue<IMetaValue> m_constRef;
  private final FinalValue<ISourceRange> m_source;
  private final FinalValue<ISourceRange> m_initSource;
  private final FinalValue<ISourceRange> m_javaDocSource;
  private int m_flags;

  protected DeclarationFieldWithEcj(JavaEnvironmentWithEcj env, DeclarationTypeWithEcj declaringType, FieldDeclaration astNode) {
    super(env);
    m_declaringType = Ensure.notNull(declaringType);
    m_astNode = Ensure.notNull(astNode);
    m_name = astNode.name != null ? astNode.name : CharOperation.NO_CHAR;//static { } has name =""
    m_flags = -1;
    m_nameAsString = new FinalValue<>();
    m_type = new FinalValue<>();
    m_annotations = new FinalValue<>();
    m_constRef = new FinalValue<>();
    m_source = new FinalValue<>();
    m_initSource = new FinalValue<>();
    m_javaDocSource = new FinalValue<>();
  }

  @Override
  public FieldSpi internalFindNewElement() {
    var newType = (TypeSpi) getDeclaringType().internalFindNewElement();
    if (newType == null) {
      return null;
    }
    return newType.getFields().stream()
        .filter(newField -> getElementName().equals(newField.getElementName()))
        .findFirst()
        .orElse(null);
  }

  @Override
  protected IField internalCreateApi() {
    return new FieldImplementor(this);
  }

  public FieldDeclaration getInternalFieldDeclaration() {
    return m_astNode;
  }

  @Override
  public DeclarationTypeWithEcj getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public IMetaValue getConstantValue() {
    return m_constRef.computeIfAbsentAndGet(() -> {
      Function<DeclarationFieldWithEcj, Object> constantValueFunc = f -> compileExpression(f.m_astNode.initialization, null, javaEnvWithEcj());
      return SpiWithEcjUtils.resolveCompiledValue(javaEnvWithEcj(), this, constantValueFunc.apply(this), () -> withNewElement(constantValueFunc));
    });
  }

  @Override
  public TypeSpi getDataType() {
    return m_type.computeIfAbsentAndGet(() -> {
      if (m_astNode.type == null) {
        // static { } section
        return javaEnvWithEcj().createVoidType();
      }

      Function<DeclarationFieldWithEcj, TypeBinding> dataTypeFunc = fd -> {
        var tb = fd.m_astNode.type.resolvedType;
        if (tb != null) {
          return tb;
        }
        synchronized (javaEnvWithEcj().lock()) {
          return fd.m_astNode.type.resolveType(SpiWithEcjUtils.classScopeOf(fd));
        }
      };
      return bindingToType(javaEnvWithEcj(), dataTypeFunc.apply(this), () -> withNewElement(dataTypeFunc));
    });
  }

  @Override
  public List<DeclarationAnnotationWithEcj> getAnnotations() {
    return m_annotations.computeIfAbsentAndGet(() -> SpiWithEcjUtils.createDeclarationAnnotations(javaEnvWithEcj(), this, m_astNode.annotations));
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = SpiWithEcjUtils.getTypeFlags(m_astNode.modifiers, null, SpiWithEcjUtils.hasDeprecatedAnnotation(getAnnotations()));
    }
    return m_flags;
  }

  @Override
  public String getElementName() {
    return m_nameAsString.computeIfAbsentAndGet(() -> new String(m_name));
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
    return m_source.computeIfAbsentAndGet(() -> javaEnvWithEcj().getSource(m_declaringType.getCompilationUnit(), m_astNode.declarationSourceStart, m_astNode.declarationSourceEnd));
  }

  @Override
  public ISourceRange getSourceOfInitializer() {
    return m_initSource.computeIfAbsentAndGet(() -> {
      var cu = m_declaringType.getCompilationUnit();
      if (m_astNode instanceof Initializer) {
        // static initializer
        return javaEnvWithEcj().getSource(cu, m_astNode.declarationSourceStart, m_astNode.declarationSourceEnd);
      }
      return SpiWithEcjUtils.createSourceRange(m_astNode.initialization, cu, javaEnvWithEcj());
    });
  }

  @Override
  public ISourceRange getJavaDoc() {
    return m_javaDocSource.computeIfAbsentAndGet(() -> SpiWithEcjUtils.createSourceRange(m_astNode.javadoc, m_declaringType.getCompilationUnit(), javaEnvWithEcj()));
  }
}
