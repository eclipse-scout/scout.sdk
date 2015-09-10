/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.spi;

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;

/**
 * <h3>{@link AnnotatableSpi}</h3>Represents a Java element that can be annotated.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 * @see AnnotationSpi
 */
public interface AnnotatableSpi extends JavaElementSpi {
  /**
   * Gets all the {@link AnnotationSpi}s that are defined for the receiver.<br>
   * The {@link List} iterates over the {@link AnnotationSpi}s in the order as they appear in the source or class file.
   *
   * @return A {@link List} containing all {@link AnnotationSpi}s of the receiver object. Never returns
   *         <code>null</code>.
   */
  List<? extends AnnotationSpi> getAnnotations();

  @Override
  IAnnotatable wrap();

}
