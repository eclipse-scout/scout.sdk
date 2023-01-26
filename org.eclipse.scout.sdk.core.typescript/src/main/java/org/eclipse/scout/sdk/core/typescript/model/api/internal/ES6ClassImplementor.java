/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.internal;

import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.AbstractNodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.query.FieldQuery;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;

public class ES6ClassImplementor extends AbstractNodeElement<ES6ClassSpi> implements IES6Class {
  public ES6ClassImplementor(ES6ClassSpi spi) {
    super(spi);
  }

  @Override
  public String name() {
    return spi().name();
  }

  @Override
  public FieldQuery fields() {
    return new FieldQuery(spi());
  }

  @Override
  public boolean isEnum() {
    return spi().isEnum();
  }

  @Override
  public Optional<IField> field(String name) {
    return fields()
        .withName(name)
        .first();
  }
}
