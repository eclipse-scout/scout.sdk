/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.sourcebuilder.annotation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.AbstractJavaElementSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ExpressionSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link AnnotationSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public class AnnotationSourceBuilder extends AbstractJavaElementSourceBuilder implements IAnnotationSourceBuilder {

  private final String m_name;
  private final LinkedHashMap<String, ISourceBuilder> m_values = new LinkedHashMap<>();

  public AnnotationSourceBuilder(IAnnotation element) {
    super(element);
    m_name = element.type().name();
    for (IAnnotationElement av : element.elements().values()) {
      if (av.isDefault()) {
        continue;
      }
      putValue(av.elementName(), ExpressionSourceBuilderFactory.createFromMetaValue(av.value()));
    }
  }

  /**
   * @param name
   *          is the fully qualified name of the annotation type
   */
  public AnnotationSourceBuilder(String name) {
    super(Signature.getSimpleName(name));
    m_name = name;
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    super.createSource(source, lineDelimiter, context, validator);
    if (StringUtils.isEmpty(getName())) {
      throw new IllegalArgumentException("name required!");
    }

    source.append("@" + validator.useName(getName()));
    if (m_values.size() > 0) {
      source.append('(');
      if (m_values.size() == 1 && m_values.containsKey("value")) {
        //single value annotation
        ISourceBuilder v = m_values.values().iterator().next();
        v.createSource(source, lineDelimiter, context, validator);
      }
      else {
        for (Map.Entry<String, ISourceBuilder> e : m_values.entrySet()) {
          source.append(e.getKey());
          source.append(" = ");
          e.getValue().createSource(source, lineDelimiter, context, validator);
          source.append(", ");
        }
        source.setLength(source.length() - 2);
      }
      source.append(')');
    }
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public IAnnotationSourceBuilder putValue(String name, String javaSource) {
    putValue(name, new RawSourceBuilder(javaSource));
    return this;
  }

  @Override
  public IAnnotationSourceBuilder putValue(String name, ISourceBuilder value) {
    m_values.put(name, value);
    return this;
  }

  @Override
  public boolean removeValue(String name) {
    return m_values.remove(name) != null;
  }

  @Override
  public ISourceBuilder getValue(String name) {
    return m_values.get(name);
  }

  @Override
  public Map<String, ISourceBuilder> getValues() {
    return Collections.unmodifiableMap(m_values);
  }

}
