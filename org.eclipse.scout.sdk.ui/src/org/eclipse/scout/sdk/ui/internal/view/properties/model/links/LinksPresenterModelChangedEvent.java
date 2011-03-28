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
package org.eclipse.scout.sdk.ui.internal.view.properties.model.links;

import java.util.EventObject;

/**
 * <h3>{@link LinksPresenterModelChangedEvent}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 17.03.2011
 */
public class LinksPresenterModelChangedEvent extends EventObject {
  public static final int GROUP_ADDED = 1;
  public static final int GROUP_REMOVED = 2;
  public static final int GROUP_CHANGED = 3;

  public static final int LINK_ADDED = 6;
  public static final int LINK_REMOVED = 7;
  public static final int LINK_CHANGED = 8;

  private int m_eventType;
  private LinkGroup m_linkGroup;
  private ILink m_link;

  public LinksPresenterModelChangedEvent(Object source, int eventType, LinkGroup group) {
    this(source, eventType, group, null);
  }

  public LinksPresenterModelChangedEvent(Object source, int eventType, LinkGroup group, ILink link) {
    super(source);
    m_eventType = eventType;
    m_linkGroup = group;
    m_link = link;
  }

  /**
   * @return the eventType
   */
  public int getEventType() {
    return m_eventType;
  }

  /**
   * @return the linkGroup
   */
  public LinkGroup getLinkGroup() {
    return m_linkGroup;
  }

  /**
   * @return the link
   */
  public ILink getLink() {
    return m_link;
  }

}
