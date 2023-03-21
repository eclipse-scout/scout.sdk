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

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi;

public class FieldImplementor extends VariableImplementor<FieldSpi> implements IField {
  public FieldImplementor(FieldSpi spi) {
    super(spi);
  }

  @Override
  public boolean isOptional() {
    return spi().isOptional();
  }

  @Override
  public boolean hasModifier(Modifier modifier) {
    return modifier != null && spi().hasModifier(modifier);
  }

  @Override
  public IES6Class declaringClass() {
    return spi().declaringClass().api();
  }
}
