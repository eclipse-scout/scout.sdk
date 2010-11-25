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

public class ScoutProjectEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_BUNDLE_ADDED = 1;
  public static final int TYPE_BUNDLE_REMOVED = 2;
  public static final int TYPE_BUNDLE_CHANGED = 3;

  private final IScoutBundle m_bundle;
  private final int m_eventType;

  public ScoutProjectEvent(IScoutProject source, int eventType, IScoutBundle bundle) {
    super(source);
    m_eventType = eventType;
    m_bundle = bundle;
  }

  @Override
  public IScoutProject getSource() {
    return (IScoutProject) super.getSource();
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public int getEventType() {
    return m_eventType;
  }

}
