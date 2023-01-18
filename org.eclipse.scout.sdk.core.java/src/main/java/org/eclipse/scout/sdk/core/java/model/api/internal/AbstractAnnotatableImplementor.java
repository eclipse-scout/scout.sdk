/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api.internal;

import org.eclipse.scout.sdk.core.java.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotatableSpi;

public abstract class AbstractAnnotatableImplementor<SPI extends AnnotatableSpi> extends AbstractJavaElementImplementor<SPI> implements IAnnotatable {
  protected AbstractAnnotatableImplementor(SPI spi) {
    super(spi);
  }
}
