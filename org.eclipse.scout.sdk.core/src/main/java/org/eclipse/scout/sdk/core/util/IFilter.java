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
package org.eclipse.scout.sdk.core.util;

/**
 * <h3>{@link IFilter}</h3> Defines a functor interface implemented by classes that evaluate a predicate test on an
 * element.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IFilter<E> {

  /**
   * Evaluate the {@link IFilter} by executing against the given element.
   * 
   * @param element
   *          The element to evaluate the {@link IFilter} against.
   * @return <code>true</code> if the filter accepts the given element. <code>false</code> otherwise.
   */
  boolean evaluate(E element);
}
