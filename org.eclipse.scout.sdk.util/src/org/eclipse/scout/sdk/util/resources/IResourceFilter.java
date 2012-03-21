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

/**
 * <h3>{@link IResourceFilter}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2012
 */
public interface IResourceFilter {

  boolean accept(IResource resource);

}
