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
package org.eclipse.scout.sdk.util.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;

/**
 * <h3>{@link IResourceFilter}</h3>
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 14.03.2012
 * @see ResourceFilters
 * @see IResourceProxy
 * @see IResourceProxyVisitor
 */
public interface IResourceFilter {

  /**
   * Visits the given resource.<br>
   * Only accessible resources are visited. For files and folders, this is equivalent to existing; for projects, this is
   * equivalent to existing and being open. The workspace root is always accessible. See
   * {@link IResource#isAccessible()}.
   * 
   * @param resource
   *          proxy for requesting information about the resource being visited;
   *          this object is only valid for the duration of the invocation of this
   *          method, and must not be used after this method has completed.<br>
   *          The concrete resource can be obtained using requestResource() and should only be called if really
   *          necessary -> performance.
   * @return true if the given resource is accepted by the filter. False otherwise.
   */
  boolean accept(IResourceProxy resource);

}
