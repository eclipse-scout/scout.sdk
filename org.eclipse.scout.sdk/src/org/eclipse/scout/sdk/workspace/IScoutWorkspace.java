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
package org.eclipse.scout.sdk.workspace;

import org.eclipse.scout.sdk.ScoutSdkCore;

/**
 * The root of the scout SDK.
 *
 * @see ScoutSdkCore#getScoutWorkspace()
 */
public interface IScoutWorkspace {
  /**
   * adds a new workspace listener to the scout workspace
   *
   * @param listener
   * @see IScoutWorkspaceListener
   */
  void addWorkspaceListener(IScoutWorkspaceListener listener);

  /**
   * removes a workspace listener from the scout workspace
   *
   * @param listener
   * @see IScoutWorkspaceListener
   */
  void removeWorkspaceListener(IScoutWorkspaceListener listener);

  /**
   * gets the bundle graph containing all scout bundles of the workspace and the target platform
   *
   * @return the scout bundle graph
   * @see IScoutBundleGraph
   */
  IScoutBundleGraph getBundleGraph();

  /**
   * specifies if the {@link IScoutWorkspace} is initialized. This is true after the
   * {@link ScoutWorkspaceEvent#TYPE_WORKSPACE_INITIALIZED} has been fired until the workspace is disposed.
   *
   * @return true if the workspace is currently initialized, false otherwise.
   */
  boolean isInitialized();
}
