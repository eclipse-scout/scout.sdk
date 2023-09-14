/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.prop;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;

/**
 * A computed possible value for a {@link ScoutJsProperty}. Use
 * {@link ScoutJsProperty#computePossibleValues(ScoutJsModel)} to get instances.
 */
public interface IScoutJsPropertyValue {
  /**
   * @return The value name.
   */
  String name();

  /**
   * @return The {@link ScoutJsProperty} to which this value matches.
   */
  ScoutJsProperty property();
}
