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
 *
 */
public class ScoutWorkspaceEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_BUNDLE_CHANGED = 1;
  public static final int TYPE_BUNDLE_ADDED = 2;
  public static final int TYPE_BUNDLE_REMOVED = 3;

  public static final int TYPE_PROJECT_CHANGED = 4;
  public static final int TYPE_PROJECT_ADDED = 5;
  public static final int TYPE_PROJECT_REMOVED = 6;

  public static final int TYPE_WORKSPACE_INITIALIZED = 10;

  private final IScoutElement m_scoutElement;
  private final int m_type;

  /**
   * @param source
   */
  public ScoutWorkspaceEvent(IScoutWorkspace source, int type, IScoutElement scoutElement) {
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

  public IScoutElement getScoutElement() {
    return m_scoutElement;
  }
}
