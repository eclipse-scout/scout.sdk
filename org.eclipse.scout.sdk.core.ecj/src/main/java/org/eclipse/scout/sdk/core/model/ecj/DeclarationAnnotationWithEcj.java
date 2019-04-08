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

import static org.eclipse.scout.sdk.core.model.ecj.BindingAnnotationWithEcj.buildAnnotationElementMap;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.AnnotationImplementor;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.AnnotatableSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 *
 */
public class DeclarationAnnotationWithEcj extends AbstractJavaElementWithEcj<IAnnotation> implements AnnotationSpi {
  private final AnnotatableSpi m_owner;
  private final Annotation m_astNode;
  private final TypeBinding m_typeBinding;
  private final FinalValue<Map<String, AnnotationElementSpi>> m_values;//sorted
  private final FinalValue<TypeSpi> m_type;
  private final FinalValue<ISourceRange> m_source;

  protected DeclarationAnnotationWithEcj(AbstractJavaEnvironment env, AnnotatableSpi owner, Annotation astNode) {
    super(env);
    m_astNode = Ensure.notNull(astNode);
    m_owner = Ensure.notNull(owner);
    m_typeBinding = m_astNode.type.resolveType(SpiWithEcjUtils.classScopeOf(m_owner));
    m_values = new FinalValue<>();
    m_type = new FinalValue<>();
    m_source = new FinalValue<>();
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    //not supported
    return null;
  }

  @Override
  protected IAnnotation internalCreateApi() {
    return new AnnotationImplementor(this);
  }

  public Annotation getInternalAstNode() {
    return m_astNode;
  }

  @Override
  public TypeSpi getType() {
    return m_type.computeIfAbsentAndGet(() -> SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), m_typeBinding));
  }

  @Override
  public Map<String, AnnotationElementSpi> getValues() {
    return m_values.computeIfAbsentAndGet(() -> buildAnnotationElementMap(m_astNode, this, javaEnvWithEcj()));
  }

  @Override
  public AnnotatableSpi getOwner() {
    return m_owner;
  }

  @Override
  public String getElementName() {
    return getType().getElementName();
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      CompilationUnitSpi cu = SpiWithEcjUtils.declaringTypeOf(this).getCompilationUnit();
      Annotation decl = m_astNode;
      return javaEnvWithEcj().getSource(cu, decl.sourceStart, decl.declarationSourceEnd);
    });
  }
}