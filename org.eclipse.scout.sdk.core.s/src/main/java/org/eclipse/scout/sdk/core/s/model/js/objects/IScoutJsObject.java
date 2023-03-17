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

import java.util.List;
import java.util.Map;

import org.eclipse.scout.sdk.core.s.model.js.IScoutJsElement;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsPropertyQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.IFunction;

public interface IScoutJsObject extends IScoutJsElement {

  default String shortName() {
    if (ScoutJsCoreConstants.NAMESPACE.equals(scoutJsModel().namespace().orElse(null))) {
      return name();
    }
    return qualifiedName();
  }

  default String qualifiedName() {
    return IScoutJsElement.toQualifiedName(scoutJsModel().namespace().orElse(null), name());
  }

  default boolean hasProperty(String name) {
    return findProperties()
        .withSuperClasses(true)
        .withName(name)
        .existsAny();
  }

  Map<String, ScoutJsProperty> properties();

  default ScoutJsPropertyQuery findProperties() {
    return new ScoutJsPropertyQuery(this);
  }

  List<IFunction> _inits();
}
