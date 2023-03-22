/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js;

import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.scout.sdk.core.model.query.AbstractQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;

public abstract class AbstractScoutJsElementQuery<E extends IScoutJsElement, TYPE extends AbstractScoutJsElementQuery<E, TYPE>> extends AbstractQuery<E> {

  private final ScoutJsModel m_model;

  private boolean m_includeDependencies;
  private Set<IES6Class> m_declaringClasses;

  protected AbstractScoutJsElementQuery(ScoutJsModel model) {
    m_model = model;
  }

  protected ScoutJsModel model() {
    return m_model;
  }

  @SuppressWarnings("unchecked")
  protected TYPE thisInstance() {
    return (TYPE) this;
  }

  public TYPE withIncludeDependencies(boolean includeDependencies) {
    m_includeDependencies = includeDependencies;
    return thisInstance();
  }

  protected boolean isIncludeDependencies() {
    return m_includeDependencies;
  }

  public TYPE withDeclaringClasses(Stream<IES6Class> declaringClasses) {
    if (declaringClasses == null) {
      m_declaringClasses = null;
    }
    else {
      m_declaringClasses = declaringClasses.collect(toUnmodifiableSet());
      if (m_declaringClasses.isEmpty()) {
        m_declaringClasses = null;
      }
    }
    return thisInstance();
  }

  public TYPE withDeclaringClasses(Collection<? extends IES6Class> declaringClasses) {
    if (declaringClasses == null || declaringClasses.isEmpty()) {
      m_declaringClasses = null;
    }
    else {
      m_declaringClasses = new HashSet<>(declaringClasses);
    }
    return thisInstance();
  }

  public TYPE withDeclaringClass(IES6Class declaringClass) {
    if (declaringClass == null) {
      m_declaringClasses = null;
    }
    else {
      m_declaringClasses = Collections.singleton(declaringClass);
    }
    return thisInstance();
  }

  protected Set<IES6Class> declaringClasses() {
    return m_declaringClasses;
  }

  @Override
  protected Stream<E> createStream() {
    var filter = createFilter();
    var stream = StreamSupport.stream(createSpliterator(), false);
    if (filter == null) {
      return stream;
    }
    return stream.filter(filter);
  }

  protected abstract AbstractScoutJsElementSpliterator<E> createSpliterator();

  protected Predicate<E> createFilter() {
    Predicate<E> result = null;

    var declaringClassFilter = declaringClasses();
    if (declaringClassFilter != null) {
      result = e -> declaringClassFilter.contains(e.declaringClass());
    }
    return result;
  }
}