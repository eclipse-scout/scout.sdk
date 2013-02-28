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
package org.eclipse.scout.sdk.internal.workspace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

/**
 *
 */
public class ScoutWorkspaceEventList {

  private final ScoutWorkspace m_source;
  private List<ScoutWorkspaceEvent> m_events = new ArrayList<ScoutWorkspaceEvent>();
  private HashSet<IScoutBundle> m_eventElements = new HashSet<IScoutBundle>();
  private Object lock = new Object();

  public ScoutWorkspaceEventList(ScoutWorkspace source) {
    m_source = source;
  }

  public ScoutWorkspace getSource() {
    return m_source;
  }

  public void setEvent(int eventType, IScoutBundle scoutElement, boolean overrideExisting) {
    if (containsEventFor(scoutElement)) {
      if (overrideExisting) {
        addEvent(eventType, scoutElement);
      }
    }
    else {
      addEvent(eventType, scoutElement);
    }
  }

  public boolean containsEventFor(IScoutBundle member) {
    synchronized (lock) {
      return m_eventElements.contains(member);
    }
  }

  public void addEvent(int eventType, IScoutBundle scoutElement) {
    synchronized (lock) {
      m_eventElements.add(scoutElement);
      m_events.add(new ScoutWorkspaceEvent(getSource(), eventType, scoutElement));
    }
  }

  public ScoutWorkspaceEvent[] getAllEvents() {
    synchronized (lock) {
      return m_events.toArray(new ScoutWorkspaceEvent[m_events.size()]);
    }
  }

}
