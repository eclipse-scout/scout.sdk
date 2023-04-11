/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.widgetmap;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.model.js.IScoutJsElement;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModels;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

public abstract class IdObjectTypeMap extends Type {

  private final IObjectLiteral m_model;
  private final FinalValue<Optional<ScoutJsModel>> m_scoutJsModel = new FinalValue<>();
  private final Map<String, Optional<IES6Class>> m_classes = new HashMap<>();
  private final FinalValue<Map<String, IdObjectType>> m_elements = new FinalValue<>();
  private final FinalValue<Set<IdObjectTypeMapReference>> m_idObjectTypeMapReferences = new FinalValue<>();

  protected IdObjectTypeMap(String name, IObjectLiteral model, Collection<String> usedNames) {
    super(Ensure.notNull(name), usedNames);
    m_model = Ensure.notNull(model);
  }

  public IObjectLiteral model() {
    return m_model;
  }

  @Override
  public Type withNewClassName(CharSequence name) {
    throw new UnsupportedOperationException();
  }

  public String name() {
    return newClassName().orElseThrow();
  }

  protected Optional<ScoutJsModel> scoutJsModel() {
    return m_scoutJsModel.setIfAbsentAndGet(ScoutJsModels.create(model().containingModule()));
  }

  protected Optional<IES6Class> widgetClass() {
    return scoutJsModel().map(ScoutJsModel::widgetClass);
  }

  protected Optional<IES6Class> classByObjectType(String objectType) {
    return m_classes.computeIfAbsent(objectType, ot -> scoutJsModel().flatMap(model -> model.findScoutObjects()
        .withIncludeDependencies(true)
        .withObjectType(ot)
        .first()
        .map(IScoutJsElement::declaringClass)));
  }

  public Map<String, IdObjectType> elements() {
    return m_elements.computeIfAbsentAndGet(this::parseElements);
  }

  protected abstract Map<String, IdObjectType> parseElements();

  public Set<IdObjectTypeMapReference> idObjectTypeMapReferences() {
    return m_idObjectTypeMapReferences.computeIfAbsentAndGet(this::parseIdObjectTypeMapReferences);
  }

  protected abstract Set<IdObjectTypeMapReference> parseIdObjectTypeMapReferences();

  protected void createDuplicateIdWarning(String id) {
    SdkLog.warning("Duplicate id '{}' in model '{}'.", id, model().containingFile().map(Path::toString).orElse(name()));
  }
}
