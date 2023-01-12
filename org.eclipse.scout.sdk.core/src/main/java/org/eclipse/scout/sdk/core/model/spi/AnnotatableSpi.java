/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.spi;

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;

/**
 * <h3>{@link AnnotatableSpi}</h3>Represents a Java element that can be annotated.
 *
 * @see AnnotationSpi
 * @since 5.1.0
 */
public interface AnnotatableSpi extends JavaElementSpi {
  /**
   * Gets all the {@link AnnotationSpi}s that are defined for the receiver.<br>
   * The {@link List} iterates over the {@link AnnotationSpi}s in the order as they appear in the source or class file.
   *
   * @return A {@link List} containing all {@link AnnotationSpi}s of the receiver object. Never returns {@code null}.
   */
  List<? extends AnnotationSpi> getAnnotations();

  @Override
  IAnnotatable wrap();

}
