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

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;

public class LinksPresenterModel {

  private HashMap<String, LinkGroup> m_groups = new HashMap<>();
  private LinkGroup m_globalLinks = new LinkGroup(Texts.get("Global"), 0);

  public LinkGroup[] getOrderedNotEmtyGroups() {
    TreeSet<LinkGroup> orderedGroups = new TreeSet<>(new P_GroupComparator());
    for (LinkGroup group : m_groups.values()) {
      if (!group.isEmpty()) {
        orderedGroups.add(group);
      }
    }
    return orderedGroups.toArray(new LinkGroup[orderedGroups.size()]);
  }

  public LinkGroup getGroup(String name) {
    if (StringUtility.isNullOrEmpty(name)) {
      throw new IllegalArgumentException("name can not be null.");
    }
    return m_groups.get(name);
  }

  public LinkGroup getOrCreateGroup(String name, int order) {
    if (StringUtility.isNullOrEmpty(name)) {
      throw new IllegalArgumentException("name can not be null.");
    }
    LinkGroup linkGroup = m_groups.get(name);
    if (linkGroup == null) {
      linkGroup = new LinkGroup(name, order);
      m_groups.put(name, linkGroup);
    }
    return linkGroup;
  }

  public ILink[] getOrderdGlobalLinks() {
    return m_globalLinks.getLinks();
  }

  public void addGlobalLink(ILink link) {
    m_globalLinks.addLink(link);
  }

  public boolean isEmpty() {
    return getOrderedNotEmtyGroups().length == 0 && m_globalLinks.isEmpty();
  }

  private class P_GroupComparator implements Comparator<LinkGroup> {
    @Override
    public int compare(LinkGroup o1, LinkGroup o2) {
      if (o1.getOrder() == o2.getOrder()) {
        return o1.getName().compareTo(o2.getName());
      }
      return o1.getOrder() - o2.getOrder();
    }
  } // end class P_GroupComparator

}
