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
package org.eclipse.scout.sdk.core.model;

import org.apache.commons.collections4.set.ListOrderedSet;

/**
 * <h3>{@link IAnnotatable}</h3>Represents a Java element that can be annotated.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 * @see IAnnotation
 */
public interface IAnnotatable {
  /**
   * Gets all the {@link IAnnotation}s that are defined for the receiver.<br>
   * The {@link ListOrderedSet} iterates over the {@link IAnnotation}s in the order as they appear in the source or
   * class file.
   *
   * @return A {@link ListOrderedSet} containing all {@link IAnnotation}s of the receiver object. Never returns
   *         <code>null</code>.
   */
  ListOrderedSet<IAnnotation> getAnnotations();
}
