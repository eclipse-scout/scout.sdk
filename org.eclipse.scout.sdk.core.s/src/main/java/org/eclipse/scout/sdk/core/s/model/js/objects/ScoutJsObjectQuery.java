/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.objects;

import java.util.function.Predicate;

import org.eclipse.scout.sdk.core.s.model.js.AbstractScoutJsElementQuery;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;

public class ScoutJsObjectQuery extends AbstractScoutJsElementQuery<IScoutJsObject, ScoutJsObjectQuery> {

  private String m_objectType;
  private Modifier m_requiredModifier;
  private Modifier m_notAllowedModifier;

  public ScoutJsObjectQuery(ScoutJsModel model) {
    super(model);
  }

  public ScoutJsObjectQuery withObjectType(String objectType) {
    m_objectType = objectType;
    return this;
  }

  protected String objectType() {
    return m_objectType;
  }

  public ScoutJsObjectQuery withoutModifier(Modifier modifier) {
    m_notAllowedModifier = modifier;
    return this;
  }

  protected Modifier getNotAllowedModifier() {
    return m_notAllowedModifier;
  }

  public ScoutJsObjectQuery withModifier(Modifier modifier) {
    m_requiredModifier = modifier;
    return this;
  }

  protected Modifier getRequiredModifier() {
    return m_requiredModifier;
  }

  @Override
  protected ScoutJsObjectSpliterator createSpliterator() {
    return new ScoutJsObjectSpliterator(model(), isIncludeSelf(), isIncludeDependencies());
  }

  @Override
  protected Predicate<IScoutJsObject> createFilter() {
    var result = super.createFilter();

    var objectType = objectType();
    if (objectType != null) {
      if (objectType.indexOf('.') < 0) {
        objectType = ScoutJsCoreConstants.NAMESPACE + '.' + objectType;
      }
      var objectTypeFinal = objectType;
      result = appendOrCreateFilter(result, o -> o.qualifiedName().equals(objectTypeFinal));
    }

    var requiredModifier = getRequiredModifier();
    if (requiredModifier != null) {
      result = appendOrCreateFilter(result, o -> o.declaringClass().hasModifier(requiredModifier));
    }

    var notAllowedModifier = getNotAllowedModifier();
    if (notAllowedModifier != null) {
      result = appendOrCreateFilter(result, o -> !o.declaringClass().hasModifier(notAllowedModifier));
    }

    return result;
  }

  private static Predicate<IScoutJsObject> appendOrCreateFilter(Predicate<IScoutJsObject> existing, Predicate<IScoutJsObject> toAppend) {
    if (existing == null) {
      return toAppend;
    }
    return existing.and(toAppend);
  }
}
