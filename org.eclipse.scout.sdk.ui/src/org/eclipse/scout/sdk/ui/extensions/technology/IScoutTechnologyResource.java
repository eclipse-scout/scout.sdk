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
package org.eclipse.scout.sdk.ui.extensions.technology;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link IScoutTechnologyResource}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 10.02.2012
 */
public interface IScoutTechnologyResource {
  boolean getDefaultSelection();

  IScoutBundle getBundle();

  ImageDescriptor getBundleImage();

  IFile getResource();

  IScoutTechnologyHandler getHandler();

  void setHandler(IScoutTechnologyHandler handler);
}
