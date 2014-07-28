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
package org.eclipse.scout.sdk.ui.extensions.preferences;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link TargetPackageModel}</h3>
 *
 * @author Matthias Villiger
 * @since 3.9.0 17.12.2012
 */
public class TargetPackageModel implements Comparable<TargetPackageModel> {

  final String m_id;
  final String m_defaultVal;
  final IScoutBundle m_context;
  String m_curVal;
  String m_label;

  TargetPackageModel(String id, String defaultVal, IScoutBundle context) {
    m_id = id;
    m_defaultVal = defaultVal;
    m_context = context;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TargetPackageModel)) {
      return false;
    }
    return CompareUtility.equals(m_id, ((TargetPackageModel) obj).m_id);
  }

  @Override
  public int hashCode() {
    if (m_id == null) {
      return 0;
    }
    else {
      return m_id.hashCode();
    }
  }

  @Override
  public int compareTo(TargetPackageModel o) {
    return m_id.compareTo(o.m_id);
  }

  public void load() {
    m_curVal = DefaultTargetPackage.get(m_context, m_id);
    String[] tokens = getTokens();
    m_label = tokens[1];
  }

  private String[] getTokens() {
    String[] tokens = m_id.split("\\.");
    StringBuilder lbl = new StringBuilder();
    String group = null;
    boolean first = true;
    for (String s : tokens) {
      if (StringUtility.hasText(s)) {
        s = s.trim();
        if (first) {
          group = s;
          first = false;
        }
        else {
          lbl.append(Character.toUpperCase(s.charAt(0)));
          if (s.length() > 1) {
            lbl.append(s.substring(1));
          }
          lbl.append(" ");
        }
      }
    }

    return new String[]{group, lbl.toString()};
  }
}
