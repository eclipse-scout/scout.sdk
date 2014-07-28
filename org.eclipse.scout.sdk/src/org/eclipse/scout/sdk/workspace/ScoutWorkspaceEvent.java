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

import java.util.EventObject;

/**
 * A Scout workspace event that occurred on a single scout bundle
 *
 * @see IScoutWorkspace
 * @see IScoutBundle
 */
public class ScoutWorkspaceEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  /**
   * A bundle has changed its properties or parent bundles
   */
  public static final int TYPE_BUNDLE_CHANGED = 1;

  /**
   * A bundle was newly added
   */
  public static final int TYPE_BUNDLE_ADDED = 2;

  /**
   * a bundle was removed
   */
  public static final int TYPE_BUNDLE_REMOVED = 3;

  /**
   * the workspace has been initialized the first time
   */
  public static final int TYPE_WORKSPACE_INITIALIZED = 10;

  private final IScoutBundle m_scoutElement;
  private final int m_type;

  public ScoutWorkspaceEvent(IScoutWorkspace source, int type, IScoutBundle scoutElement) {
    super(source);
    m_type = type;
    m_scoutElement = scoutElement;
  }

  @Override
  public IScoutWorkspace getSource() {
    return (IScoutWorkspace) super.getSource();
  }

  public int getType() {
    return m_type;
  }

  /**
   * gets the scout bundle that belongs to this event
   *
   * @return
   */
  public IScoutBundle getScoutElement() {
    return m_scoutElement;
  }
}
