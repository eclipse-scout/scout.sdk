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

import java.util.EventListener;

import org.eclipse.scout.sdk.ScoutSdkCore;

/**
 * Listener that gets notified when the scout workspace changes
 *
 * @see ScoutSdkCore#getScoutWorkspace()
 * @see IScoutWorkspace
 */
public interface IScoutWorkspaceListener extends EventListener {

  /**
   * is called when the workspace changes
   *
   * @param event
   *          contains information about the event
   * @see ScoutWorkspaceEvent
   */
  void workspaceChanged(ScoutWorkspaceEvent event);

}
