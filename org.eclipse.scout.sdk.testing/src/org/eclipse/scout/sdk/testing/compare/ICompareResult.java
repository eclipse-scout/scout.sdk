/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.testing.compare;

import java.util.List;

/**
 * <h3>{@link ICompareResult}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.9.0 15.03.2013
 */
public interface ICompareResult<T> {

  boolean isEqual();

  List<IDifference<T>> getDifferences();

  IDifference<T> getFirstDifference();

  public static interface IDifference<T> {
    T getValue01();

    T getValue02();
  }
}
