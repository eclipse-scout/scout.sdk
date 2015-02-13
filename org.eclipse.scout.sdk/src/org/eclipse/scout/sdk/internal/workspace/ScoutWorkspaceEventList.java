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
  private final List<ScoutWorkspaceEvent> m_events;
  private final HashSet<IScoutBundle> m_eventElements;

  public ScoutWorkspaceEventList(ScoutWorkspace source) {
    m_source = source;
    m_events = new ArrayList<>();
    m_eventElements = new HashSet<>();
  }

  public ScoutWorkspace getSource() {
    return m_source;
  }

  public synchronized void setEvent(int eventType, IScoutBundle scoutElement, boolean overrideExisting) {
    if (containsEventFor(scoutElement)) {
      if (overrideExisting) {
        addEvent(eventType, scoutElement);
      }
    }
    else {
      addEvent(eventType, scoutElement);
    }
  }

  private boolean containsEventFor(IScoutBundle member) {
    return m_eventElements.contains(member);
  }

  public synchronized void addEvent(int eventType, IScoutBundle scoutElement) {
    m_eventElements.add(scoutElement);
    m_events.add(new ScoutWorkspaceEvent(getSource(), eventType, scoutElement));
  }

  public synchronized ScoutWorkspaceEvent[] getAllEvents() {
    return m_events.toArray(new ScoutWorkspaceEvent[m_events.size()]);
  }
}
