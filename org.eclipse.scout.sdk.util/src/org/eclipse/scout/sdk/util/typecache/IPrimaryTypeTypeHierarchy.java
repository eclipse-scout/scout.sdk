/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.util.typecache;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IRegion;

/**
 *
 */
public interface IPrimaryTypeTypeHierarchy extends ICachedTypeHierarchy {

  /**
   * @param additionalRegion
   *          an additional reason <b> not null</b>.
   * @return a type hierarchy combined out of the additional region and the current hierarchy.
   */
  ITypeHierarchy combinedTypeHierarchy(IRegion additionalRegion);

  ITypeHierarchy combinedTypeHierarchy(IJavaElement... additionalElements);
}
