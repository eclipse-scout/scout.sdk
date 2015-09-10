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
package org.eclipse.scout.sdk.core.sourcebuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link AbstractAnnotatableSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public abstract class AbstractAnnotatableSourceBuilder extends AbstractJavaElementSourceBuilder implements IAnnotatableSourceBuilder {
  private final List<IAnnotationSourceBuilder> m_annotations = new ArrayList<>();
  private final Map<CompositeObject, IAnnotationSourceBuilder> m_sortedAnnotations = new TreeMap<>();

  public AbstractAnnotatableSourceBuilder(IAnnotatable element) {
    super(element);
    for (IAnnotation a : element.getAnnotations()) {
      addAnnotation(new AnnotationSourceBuilder(a));
    }
  }

  public AbstractAnnotatableSourceBuilder(String elementName) {
    super(elementName);
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    super.createSource(source, lineDelimiter, context, validator);
    // annotations
    createAnnotations(source, lineDelimiter, context, validator);
  }

  protected void createAnnotations(StringBuilder sourceBuilder, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    for (IAnnotationSourceBuilder sb : getAnnotations()) {
      if (sb != null) {
        sb.createSource(sourceBuilder, lineDelimiter, context, validator);
        sourceBuilder.append(lineDelimiter);
      }
    }
  }

  @Override
  public void addAnnotation(IAnnotationSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("annotation source builder can not be null.");
    }
    if (!m_sortedAnnotations.isEmpty()) {
      throw new IllegalStateException("This operation has already sorted annotation source builders. A mix between sorted and unsorted annotation source buiders is not supported.");
    }
    m_annotations.add(builder);
  }

  @Override
  public void addSortedAnnotation(CompositeObject sortKey, IAnnotationSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("annotation source builder can not be null.");
    }
    if (sortKey == null) {
      throw new IllegalArgumentException("Sort key can not be null.");
    }
    if (!m_annotations.isEmpty()) {
      throw new IllegalStateException("This operation has already unsorted annotation source builders. A mix between sorted and unsorted annotation builders is not supported.");
    }
    m_sortedAnnotations.put(sortKey, builder);
  }

  @Override
  public boolean removeAnnotation(String elementName) {
    for (Iterator<IAnnotationSourceBuilder> it = m_annotations.iterator(); it.hasNext();) {
      if (elementName.equals(it.next().getElementName())) {
        it.remove();
        return true;
      }
    }
    return false;
  }

  @Override
  public List<IAnnotationSourceBuilder> getAnnotations() {
    List<IAnnotationSourceBuilder> ops = new ArrayList<>(m_annotations.size() + m_sortedAnnotations.size());
    ops.addAll(m_annotations);
    ops.addAll(m_sortedAnnotations.values());
    return ops;
  }
}
