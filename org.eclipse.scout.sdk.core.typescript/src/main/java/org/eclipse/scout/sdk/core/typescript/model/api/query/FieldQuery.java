/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.query;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.query.AbstractQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.util.Ensure;

public class FieldQuery extends AbstractQuery<IField> {

  private final ES6ClassSpi m_es6ClassSpi;
  private String m_name;

  public FieldQuery(ES6ClassSpi es6Class) {
    m_es6ClassSpi = Ensure.notNull(es6Class);
  }

  /**
   * Limits the {@link IField} to the one with the given name.
   *
   * @param name
   *          The name to search. Default is not filtering on name.
   * @return this
   */
  public FieldQuery withName(String name) {
    m_name = name;
    return this;
  }

  protected String getName() {
    return m_name;
  }

  protected ES6ClassSpi es6Class() {
    return m_es6ClassSpi;
  }

  @Override
  protected Stream<IField> createStream() {
    var name = getName();
    if (name == null) {
      return es6Class().fields().stream()
          .map(FieldSpi::api);
    }

    return es6Class().fields().stream()
        .filter(field -> name.equals(field.name()))
        .map(FieldSpi::api);
  }
}
