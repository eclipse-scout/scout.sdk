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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;

/**
 * <h3>{@link ScoutBundleNodeGroup}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 11.02.2013
 */
public class ScoutBundleNodeGroup implements Comparable<ScoutBundleNodeGroup> {
  private final ScoutBundleNode m_definingBundle;
  private final Set<ScoutBundleNode> m_childBundles;
  private final Set<ScoutBundleNodeGroup> m_parentGroups;
  private final Set<ScoutBundleNodeGroup> m_childGroups;
  private final String m_groupName;

  public ScoutBundleNodeGroup(ScoutBundleNode definingBundle) {
    m_definingBundle = definingBundle;
    m_parentGroups = new HashSet<ScoutBundleNodeGroup>();
    m_childGroups = new HashSet<ScoutBundleNodeGroup>();
    m_childBundles = new HashSet<ScoutBundleNode>();
    m_groupName = getGroupName(definingBundle.getSymbolicName(), definingBundle.getType());
    m_childBundles.add(definingBundle);
  }

  private String getGroupName(String symbolicName, String type) {
    Matcher m = Pattern.compile("^(.*)\\.(" + type.toLowerCase() + ")(\\.(.*))?$").matcher(symbolicName.toLowerCase());
    if (m.find()) {
      String prefix = StringUtility.trim(m.group(1));
      String postfix = StringUtility.trim(m.group(4));

      StringBuilder builder = new StringBuilder();
      if (!StringUtility.isNullOrEmpty(prefix)) {
        builder.append(prefix);
      }
      if (!StringUtility.isNullOrEmpty(postfix)) {
        builder.append(" (").append(postfix).append(")");
      }
      return builder.toString();
    }
    else {
      return symbolicName;
    }
  }

  public void addChildBundle(ScoutBundleNode child) {
    m_childBundles.add(child);
  }

  public ScoutBundleNode getDefiningBundle() {
    return m_definingBundle;
  }

  public Set<ScoutBundleNodeGroup> getChildGroups() {
    return m_childGroups;
  }

  public void addChildGroup(ScoutBundleNodeGroup child) {
    m_childGroups.add(child);
    child.m_parentGroups.add(this);
  }

  public Set<ScoutBundleNode> getChildBundles() {
    return m_childBundles;
  }

  public Set<ScoutBundleNodeGroup> getParentGroups() {
    return m_parentGroups;
  }

  public boolean containsBundle(ScoutBundleNode node) {
    for (ScoutBundleNode n : getChildBundles()) {
      if (node.equals(n)) {
        return true;
      }
    }
    for (ScoutBundleNodeGroup grp : getChildGroups()) {
      if (grp.containsBundle(node)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return m_definingBundle.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return m_definingBundle.equals(obj);
  }

  @Override
  public String toString() {
    return m_definingBundle.toString();
  }

  public String getGroupName() {
    return m_groupName;
  }

  @Override
  public int compareTo(ScoutBundleNodeGroup o) {
    return getGroupName().compareTo(o.getGroupName());
  }
}
