/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

import org.eclipse.scout.sdk.core.generator.IAnnotatableGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
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
