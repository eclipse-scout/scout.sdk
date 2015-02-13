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

import java.util.ArrayList;

/**
 * <h3>ILinkGroup</h3> A group having a name and an order number.
 * The order number decides where to place the current group within a context of link groups.
 *
 * @see LinksPresenterModel#getOrderedNotEmtyGroups()
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2010
 */
public class LinkGroup {

  private int m_order;
  private String m_name;

  private ArrayList<ILink> m_links = new ArrayList<>();

  public LinkGroup(String name, int order) {
    m_name = name;
    m_order = order;
  }

  /**
   * @return
   */
  public boolean isEmpty() {
    return m_links.isEmpty();
  }

  /**
   * @return
   */
  public int getOrder() {
    return m_order;
  }

  public String getName() {
    return m_name;
  }

  public void addLink(ILink link) {
    m_links.add(link);
  }

  public boolean removeLink(ILink link) {
    return m_links.remove(link);
  }

  public ILink[] getLinks() {
    return m_links.toArray(new ILink[m_links.size()]);
  }
}
