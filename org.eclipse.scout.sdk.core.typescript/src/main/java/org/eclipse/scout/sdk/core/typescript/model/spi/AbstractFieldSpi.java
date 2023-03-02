/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.spi;

import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.FieldImplementor;

public abstract class AbstractFieldSpi extends AbstractNodeElementSpi<IField> implements FieldSpi {

  protected AbstractFieldSpi(NodeModuleSpi module) {
    super(module);
  }

  @Override
  protected IField createApi() {
    return new FieldImplementor(this);
  }
}
