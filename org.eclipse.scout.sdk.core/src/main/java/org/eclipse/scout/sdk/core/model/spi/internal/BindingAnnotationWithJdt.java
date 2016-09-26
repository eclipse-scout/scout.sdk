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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.AnnotationImplementor;
import org.eclipse.scout.sdk.core.model.spi.AnnotatableSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 *
 */
public class BindingAnnotationWithJdt extends AbstractJavaElementWithJdt<IAnnotation> implements AnnotationSpi {
  private final AnnotatableSpi m_owner;
  private final AnnotationBinding m_binding;
  private Map<String, AnnotationElementSpi> m_values;//sorted
  private TypeSpi m_type;
  private ISourceRange m_source;

  BindingAnnotationWithJdt(JavaEnvironmentWithJdt env, AnnotatableSpi owner, AnnotationBinding binding) {
    super(env);
    m_binding = Validate.notNull(binding);
    m_owner = Validate.notNull(owner);
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

  public AnnotationBinding getInternalBinding() {
    return m_binding;
  }

  @Override
  public TypeSpi getType() {
    if (m_type == null) {
      m_type = SpiWithJdtUtils.bindingToType(m_env, m_binding.getAnnotationType());
    }
    return m_type;
  }

  @Override
  public Map<String, AnnotationElementSpi> getValues() {
    if (m_values != null) {
      return m_values;
    }

    Map<String, ElementValuePair> defaultsMap = SpiWithJdtUtils.getBindingAnnotationSyntheticDefaultValues(m_env, m_binding.getAnnotationType());
    final Map<String, AnnotationElementSpi> result = new LinkedHashMap<>(defaultsMap.size());
    //fill keys only in correct sort order
    for (String name : defaultsMap.keySet()) {
      result.put(name, null);
    }
    //add declared values
    ElementValuePair[] bindingPairs = m_binding.getElementValuePairs();
    if (bindingPairs != null && bindingPairs.length > 0) {
      for (ElementValuePair bindingPair : bindingPairs) {
        BindingAnnotationElementWithJdt v = m_env.createBindingAnnotationValue(this, bindingPair, false);
        result.put(v.getElementName(), v);
      }
    }
    //add default values
    for (Map.Entry<String, ElementValuePair> e : defaultsMap.entrySet()) {
      if (result.get(e.getKey()) == null && e.getValue() != null) {
        result.put(e.getKey(), m_env.createBindingAnnotationValue(this, e.getValue(), true));
      }
    }
    m_values = Collections.unmodifiableMap(result);
    return m_values;
  }

  @Override
  public AnnotationElementSpi getValue(String name) {
    return getValues().get(name);
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
    if (m_source == null) {
      CompilationUnitSpi cu = SpiWithJdtUtils.declaringTypeOf(this).getCompilationUnit();
      Annotation decl = SpiWithJdtUtils.findAnnotationDeclaration(this);
      if (decl != null) {
        m_source = m_env.getSource(cu, decl.sourceStart, decl.declarationSourceEnd);
      }
      else {
        m_source = ISourceRange.NO_SOURCE;
      }
    }
    return m_source;
  }

}
