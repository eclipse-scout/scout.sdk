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

import java.nio.file.Path;

import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement.ExportType;
import org.eclipse.scout.sdk.core.typescript.model.api.internal.FieldImplementor;
import org.eclipse.scout.sdk.core.util.Ensure;

public abstract class AbstractFieldSpi extends AbstractNodeElementSpi<IField> implements FieldSpi {

  private final ES6ClassSpi m_declaringClass;

  protected AbstractFieldSpi(NodeModuleSpi module, ES6ClassSpi declaringClass) {
    super(module);
    m_declaringClass = Ensure.notNull(declaringClass);
  }

  @Override
  public ExportType exportType() {
    return ExportType.NONE; // fields are not exported directly
  }

  @Override
  public ES6ClassSpi declaringClass() {
    return m_declaringClass;
  }

  @Override
  protected Path resolveContainingFile() {
    return declaringClass().containingFile().orElse(null);
  }

  @Override
  protected IField createApi() {
    return new FieldImplementor(this);
  }
}
