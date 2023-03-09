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

public class ScoutJsObjectQuery extends AbstractQuery<IScoutJsObject> {

  private final ScoutJsModel m_model;

  private boolean m_includeDependencies;
  private String m_objectType;
  private Set<IES6Class> m_objectClasses;
  private IES6Class m_instanceOf;

  public ScoutJsObjectQuery(ScoutJsModel model) {
    m_model = model;
  }

  public ScoutJsObjectQuery withIncludeDependencies(boolean includeDependencies) {
    m_includeDependencies = includeDependencies;
    return this;
  }

  protected boolean isIncludeDependencies() {
    return m_includeDependencies;
  }

  public ScoutJsObjectQuery withObjectType(String objectType) {
    m_objectType = objectType;
    return this;
  }

  protected String objectType() {
    return m_objectType;
  }

  public ScoutJsObjectQuery withObjectClasses(Stream<IES6Class> declaringClasses) {
    if (declaringClasses == null) {
      m_objectClasses = null;
    }
    else {
      m_objectClasses = declaringClasses.collect(toUnmodifiableSet());
      if (m_objectClasses.isEmpty()) {
        m_objectClasses = null;
      }
    }
    return this;
  }

  public ScoutJsObjectQuery withObjectClasses(Collection<? extends IES6Class> declaringClasses) {
    if (declaringClasses == null || declaringClasses.isEmpty()) {
      m_objectClasses = null;
    }
    else {
      m_objectClasses = new HashSet<>(declaringClasses);
    }
    return this;
  }

  public ScoutJsObjectQuery withObjectClass(IES6Class declaringClass) {
    if (declaringClass == null) {
      m_objectClasses = null;
    }
    else {
      m_objectClasses = Collections.singleton(declaringClass);
    }
    return this;
  }

  protected Set<IES6Class> objectClasses() {
    return m_objectClasses;
  }

  public ScoutJsObjectQuery withInstanceOf(IES6Class superClass) {
    m_instanceOf = superClass;
    return this;
  }

  protected IES6Class instanceOf() {
    return m_instanceOf;
  }

  protected ScoutJsModel model() {
    return m_model;
  }

  @Override
  protected Stream<IScoutJsObject> createStream() {
    var filter = createFilter();
    var stream = StreamSupport.stream(new ScoutJsObjectSpliterator(model(), isIncludeDependencies()), false);
    if (filter == null) {
      return stream;
    }
    return stream.filter(filter);
  }

  protected Predicate<IScoutJsObject> createFilter() {
    Predicate<IScoutJsObject> result = null;

    var declaringClassFilter = objectClasses();
    if (declaringClassFilter != null) {
      result = o -> declaringClassFilter.contains(o.declaringClass());
    }

    var objectType = objectType();
    if (objectType != null) {
      if (objectType.indexOf('.') < 0) {
        objectType = ScoutJsCoreConstants.NAMESPACE + '.' + objectType;
      }
      var objectTypeFinal = objectType;
      Predicate<IScoutJsObject> objectTypeFilter = o -> o.qualifiedName().equals(objectTypeFinal);
      if (result == null) {
        result = objectTypeFilter;
      }
      else {
        result = result.and(objectTypeFilter);
      }
    }

    var instanceOf = instanceOf();
    if (instanceOf != null) {
      Predicate<IScoutJsObject> instanceOfFilter = o -> o.declaringClass().isInstanceOf(instanceOf);
      if (result == null) {
        result = instanceOfFilter;
      }
      else {
        result = result.and(instanceOfFilter);
      }
    }

    return result;
  }
}
