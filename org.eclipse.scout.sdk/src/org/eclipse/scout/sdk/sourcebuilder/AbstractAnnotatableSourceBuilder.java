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
package org.eclipse.scout.sdk.sourcebuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.util.signature.IImportValidator;

/**
 * <h3>{@link AbstractAnnotatableSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public abstract class AbstractAnnotatableSourceBuilder extends AbstractJavaElementSourceBuilder implements IAnnotatableSourceBuilder {

  private int m_flags;

  private List<IAnnotationSourceBuilder> m_annotationSourceBuilders;
  private Map<CompositeObject, IAnnotationSourceBuilder> m_sortedAnnotationSourceBuilders;

  public AbstractAnnotatableSourceBuilder(String elementName) {
    super(elementName);
    m_annotationSourceBuilders = new ArrayList<IAnnotationSourceBuilder>();
    m_sortedAnnotationSourceBuilders = new TreeMap<CompositeObject, IAnnotationSourceBuilder>();
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    super.createSource(source, lineDelimiter, ownerProject, validator);
    // annotations
    createAnnotations(source, lineDelimiter, ownerProject, validator);
  }

  protected void createAnnotations(StringBuilder sourceBuilder, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    for (IAnnotationSourceBuilder annotationOp : getAnnotationSourceBuilders()) {
      if (annotationOp != null) {
        // use as source builder
        annotationOp.createSource(sourceBuilder, lineDelimiter, ownerProject, validator);
        sourceBuilder.append(lineDelimiter);
      }
    }
  }

  public void setFlags(int flags) {
    m_flags = flags;
  }

  public int getFlags() {
    return m_flags;
  }

  public void addAnnotationSourceBuilder(IAnnotationSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("annotation source builder can not be null.");
    }
    if (!m_sortedAnnotationSourceBuilders.isEmpty()) {
      throw new IllegalStateException("This operation has already sorted annotation source builders. A mix between sorted and unsorted annotation source buiders is not supported.");
    }
    m_annotationSourceBuilders.add(builder);
  }

  public void addSortedAnnotationSourceBuilder(CompositeObject sortKey, IAnnotationSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("annotation source builder can not be null.");
    }
    if (sortKey == null) {
      throw new IllegalArgumentException("Sort key can not be null.");
    }
    if (!m_annotationSourceBuilders.isEmpty()) {
      throw new IllegalStateException("This operation has already unsorted annotation source builders. A mix between sorted and unsorted annotation builders is not supported.");
    }
    m_sortedAnnotationSourceBuilders.put(sortKey, builder);
  }

  public boolean removeAnnotationSourceBuilder(IAnnotationSourceBuilder childOp) {
    boolean removed = m_annotationSourceBuilders.remove(childOp);
    if (!removed) {
      Iterator<Entry<CompositeObject, IAnnotationSourceBuilder>> it = m_sortedAnnotationSourceBuilders.entrySet().iterator();
      while (it.hasNext()) {
        if (it.next().getValue().equals(childOp)) {
          it.remove();
          return true;
        }
      }
      return false;
    }
    return removed;
  }

  @Override
  public List<IAnnotationSourceBuilder> getAnnotationSourceBuilders() {
    List<IAnnotationSourceBuilder> ops = new ArrayList<IAnnotationSourceBuilder>(m_annotationSourceBuilders.size() + m_sortedAnnotationSourceBuilders.size());
    ops.addAll(m_annotationSourceBuilders);
    ops.addAll(m_sortedAnnotationSourceBuilders.values());
    return ops;
  }
}
