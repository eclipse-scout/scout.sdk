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
package org.eclipse.scout.nls.sdk.model.workspace;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;

/** <h4>INlsWorkspace</h4> */
public interface INlsWorkspace {

  /**
   * Gets a NLS hierarchy for the given input parameters.
   * If no extension for the given parameters is available, this method returns null.
   * 
   * @param args
   *          The arguments to be used when trying to find a INlsProject.
   * @return the NlsProject hierarchy for the given parameters or null.
   * @throws CoreException
   */
  INlsProject getNlsProject(Object[] args) throws CoreException;
}
