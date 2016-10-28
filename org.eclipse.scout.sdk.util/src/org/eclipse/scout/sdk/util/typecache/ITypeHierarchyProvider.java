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
package org.eclipse.scout.sdk.util.typecache;

/**
 * <h3>{@link ITypeHierarchyProvider}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 03.07.2014
 */
public interface ITypeHierarchyProvider {
  /**
   * @return The {@link ITypeHierarchy}.
   */
  ITypeHierarchy getTypeHierarchy();
}