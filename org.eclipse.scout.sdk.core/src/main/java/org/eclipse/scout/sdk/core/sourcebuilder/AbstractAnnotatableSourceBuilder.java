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
package org.eclipse.scout.sdk.core.sourcebuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link AbstractAnnotatableSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public abstract class AbstractAnnotatableSourceBuilder extends AbstractJavaElementSourceBuilder implements IAnnotatableSourceBuilder {
  private final List<IAnnotationSourceBuilder> m_annotations = new ArrayList<>();

  public AbstractAnnotatableSourceBuilder(IAnnotatable element) {
    super(element);
    for (IAnnotation a : element.annotations().list()) {
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

  protected void createAnnotations(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    List<IAnnotationSourceBuilder> annotations = getAnnotations();
    if (annotations.isEmpty()) {
      return;
    }

    // collect sources of all annotations
    List<String> annotationSources = new ArrayList<>(annotations.size());
    for (IAnnotationSourceBuilder sb : annotations) {
      if (sb != null) {
        StringBuilder annotBuilder = new StringBuilder();
        sb.createSource(annotBuilder, lineDelimiter, context, validator);
        annotationSources.add(annotBuilder.toString());
      }
    }

    // sort annotations by source length
    Collections.sort(annotationSources, new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        int result = Integer.compare(o1.length(), o2.length());
        if (result != 0) {
          return result;
        }
        return o1.compareTo(o2);
      }
    });

    // add annotation sources
    for (String annotSource : annotationSources) {
      source.append(annotSource);
      source.append(lineDelimiter);
    }
  }

  @Override
  public void addAnnotation(IAnnotationSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("annotation source builder can not be null.");
    }
    m_annotations.add(builder);
  }

  @Override
  public boolean removeAnnotation(String annotationFqn) {
    for (Iterator<IAnnotationSourceBuilder> it = m_annotations.iterator(); it.hasNext();) {
      if (annotationFqn.equals(it.next().getName())) {
        it.remove();
        return true;
      }
    }
    return false;
  }

  @Override
  public List<IAnnotationSourceBuilder> getAnnotations() {
    return Collections.unmodifiableList(m_annotations);
  }
}
