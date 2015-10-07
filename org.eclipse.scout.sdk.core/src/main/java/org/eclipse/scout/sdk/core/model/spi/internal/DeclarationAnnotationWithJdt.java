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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.AnnotationImplementor;
import org.eclipse.scout.sdk.core.model.spi.AnnotatableSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 *
 */
public class DeclarationAnnotationWithJdt extends AbstractJavaElementWithJdt<IAnnotation> implements AnnotationSpi {
  private final AnnotatableSpi m_owner;
  private final org.eclipse.jdt.internal.compiler.ast.Annotation m_astNode;
  private final TypeBinding m_typeBinding;
  private Map<String, AnnotationElementSpi> m_values;//sorted
  private TypeSpi m_type;

  DeclarationAnnotationWithJdt(JavaEnvironmentWithJdt env, AnnotatableSpi owner, org.eclipse.jdt.internal.compiler.ast.Annotation astNode) {
    super(env);
    m_astNode = Validate.notNull(astNode);
    m_owner = Validate.notNull(owner);
    m_typeBinding = m_astNode.type.resolveType(SpiWithJdtUtils.classScopeOf(m_owner));
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    //not supported
    return null;
  }

  @Override
  protected IAnnotation internalCreateApi() {
    return new AnnotationImplementor(this);
  }

  public org.eclipse.jdt.internal.compiler.ast.Annotation getInternalAstNode() {
    return m_astNode;
  }

  @Override
  public TypeSpi getType() {
    if (m_type == null) {
      m_type = SpiWithJdtUtils.bindingToType(m_env, m_typeBinding);
    }
    return m_type;
  }

  @Override
  public AnnotationElementSpi getValue(String name) {
    return getValues().get(name);
  }

  @Override
  public Map<String, AnnotationElementSpi> getValues() {
    if (m_values == null) {
      Map<String, MemberValuePair> defaultsMap = SpiWithJdtUtils.getDeclarationAnnotationSyntheticDefaultValues(m_env, m_typeBinding);
      final Map<String, AnnotationElementSpi> result = new LinkedHashMap<>(defaultsMap.size());
      //fill keys only in correct sort order
      for (String name : defaultsMap.keySet()) {
        result.put(name, null);
      }
      //add declared values
      MemberValuePair[] memberValuePairs = m_astNode.memberValuePairs();
      if (memberValuePairs != null && memberValuePairs.length > 0) {
        for (MemberValuePair p : memberValuePairs) {
          DeclarationAnnotationElementWithJdt v = m_env.createDeclarationAnnotationValue(this, p, false);
          result.put(v.getElementName(), v);
        }
      }
      //add default values
      for (Map.Entry<String, MemberValuePair> e : defaultsMap.entrySet()) {
        if (result.get(e.getKey()) == null && e.getValue() != null) {
          result.put(e.getKey(), m_env.createDeclarationAnnotationValue(this, e.getValue(), true));
        }
      }
      m_values = result;
    }
    return m_values;
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
    CompilationUnitSpi cu = SpiWithJdtUtils.declaringTypeOf(this).getCompilationUnit();
    org.eclipse.jdt.internal.compiler.ast.Annotation decl = m_astNode;
    return m_env.getSource(cu, decl.sourceStart, decl.declarationSourceEnd);
  }

}
