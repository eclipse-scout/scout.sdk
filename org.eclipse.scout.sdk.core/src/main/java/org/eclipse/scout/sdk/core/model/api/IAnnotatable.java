/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.api;

import org.eclipse.scout.sdk.core.generator.IAnnotatableGenerator;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.api.query.AnnotationQuery;
import org.eclipse.scout.sdk.core.model.spi.AnnotatableSpi;

/**
 * <h3>{@link IAnnotatable}</h3>Represents a Java element that can be annotated.
 *
 * @see IAnnotation
 * @since 5.1.0
 */
public interface IAnnotatable extends IJavaElement {

  @Override
  AnnotatableSpi unwrap();

  /**
   * @return A new {@link AnnotationQuery} that by default returns all {@link IAnnotation}s of the receiver
   *         {@link IAnnotatable}.
   */
  AnnotationQuery<IAnnotation> annotations();

  @Override
  IAnnotatableGenerator<?> toWorkingCopy();

  @Override
  IAnnotatableGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer);
}
