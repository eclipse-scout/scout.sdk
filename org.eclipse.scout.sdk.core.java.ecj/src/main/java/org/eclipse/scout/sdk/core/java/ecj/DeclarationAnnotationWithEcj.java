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
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.classScopeOf;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.declaringTypeOf;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.findNewAnnotationIn;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.internal.AnnotationImplementor;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotatableSpi;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class DeclarationAnnotationWithEcj extends AbstractJavaElementWithEcj<IAnnotation> implements AnnotationSpi {
  private final AnnotatableSpi m_owner;
  private final Annotation m_astNode;
  private final FinalValue<Map<String, AnnotationElementSpi>> m_values; // sorted
  private final FinalValue<TypeSpi> m_type;
  private final FinalValue<SourceRange> m_source;

  protected DeclarationAnnotationWithEcj(AbstractJavaEnvironment env, AnnotatableSpi owner, Annotation astNode) {
    super(env);
    m_astNode = Ensure.notNull(astNode);
    m_owner = Ensure.notNull(owner);
    m_values = new FinalValue<>();
    m_type = new FinalValue<>();
    m_source = new FinalValue<>();
  }

  @Override
  public AnnotationSpi internalFindNewElement() {
    return findNewAnnotationIn(getOwner(), getElementName());
  }

  @Override
  protected IAnnotation internalCreateApi() {
    return new AnnotationImplementor(this);
  }

  public Annotation getInternalAstNode() {
    return m_astNode;
  }

  protected static TypeBinding getAnnotationTypeBinding(DeclarationAnnotationWithEcj a) {
    return a.m_astNode.type.resolveType(classScopeOf(a.getOwner()));
  }

  @Override
  public TypeSpi getType() {
    return m_type.computeIfAbsentAndGet(() -> bindingToType(javaEnvWithEcj(), getAnnotationTypeBinding(this), () -> withNewElement(DeclarationAnnotationWithEcj::getAnnotationTypeBinding)));
  }

  @Override
  public Map<String, AnnotationElementSpi> getValues() {
    return m_values.computeIfAbsentAndGet(() -> BindingAnnotationWithEcj.buildAnnotationElementMap(m_astNode, this, javaEnvWithEcj()));
  }

  @Override
  public AnnotatableSpi getOwner() {
    return m_owner;
  }

  @Override
  public String getElementName() {
    return getType().getElementName();
  }

  Annotation annotationDeclaration() {
    return m_astNode;
  }

  @Override
  public SourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      var cu = declaringTypeOf(this).getCompilationUnit();
      var decl = m_astNode;
      return javaEnvWithEcj().getSource(cu, decl.sourceStart, decl.declarationSourceEnd);
    });
  }
}
