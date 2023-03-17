/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.enums;

import org.eclipse.scout.sdk.core.s.model.js.AbstractScoutJsElementSpliterator;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;

public class ScoutJsEnumSpliterator extends AbstractScoutJsElementSpliterator<IScoutJsEnum> {

  public ScoutJsEnumSpliterator(ScoutJsModel model, boolean includeDependencies) {
    super(model, includeDependencies, ScoutJsModel::exportedScoutEnums);
  }
}
