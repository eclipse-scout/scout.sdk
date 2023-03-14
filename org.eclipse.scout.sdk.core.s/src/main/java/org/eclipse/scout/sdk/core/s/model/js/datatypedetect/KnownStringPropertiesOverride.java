/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.datatypedetect;

import static java.util.Collections.emptyMap;

import java.util.Map;
import java.util.Optional;

import org.eclipse.scout.sdk.core.s.model.js.IScoutJsObject;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsPropertyType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;

public class KnownStringPropertiesOverride implements IPropertyDataTypeOverride {

  private final IScoutJsObject m_owner;
  private final IDataType m_stringType;

  public KnownStringPropertiesOverride(IScoutJsObject owner, IDataType stringType) {
    m_owner = owner;
    m_stringType = stringType;
  }

  @Override
  public Optional<ScoutJsPropertyType> getOverrideFor(ScoutJsProperty property) {
    if (ScoutJsCoreConstants.SCOUT_JS_CORE_MODULE_NAME.equals(m_owner.declaringClass().containingModule().name())
        && ScoutJsCoreConstants.PROPERTY_NAME_ID.equals(property.field().name())) {
      return Optional.of(new ScoutJsPropertyType(m_stringType, property));
    }
    return Optional.empty();
  }

  @Override
  public void markUsed(String propertyName) {
    // nop
  }

  @Override
  public Map<String, IDataType> unused() {
    return emptyMap();
  }
}
