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

import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link DefaultSuperClassModel}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.8.0 24.11.2012
 */
public class DefaultSuperClassModel implements Comparable<DefaultSuperClassModel> {
  private final static Pattern INTERFACE_REGEX = Pattern.compile(".*(\\.I|.Abstract)([^\\.]*)$");
  private final static Pattern SPLIT_REGEX = Pattern.compile("([A-Z])");

  final String interfaceFqn;
  final String defaultVal;
  final String label;
  final IScoutBundle scoutProject;

  private String[] m_proposals;
  private String[] m_proposalDisplayTexts;

  private int m_initialSelectedIndex;
  private int m_defaultIndex;

  DefaultSuperClassModel(String ifFqn, String def, IScoutBundle p) {
    interfaceFqn = ifFqn;
    defaultVal = def;
    scoutProject = p;
    label = getLabelTextFromInterfaceFqn(interfaceFqn);
  }

  public void load() {
    m_proposals = getTypeProposals(scoutProject, interfaceFqn, defaultVal);
    m_proposalDisplayTexts = getFqnDisplayTexts(m_proposals);

    m_initialSelectedIndex = indexOf(m_proposals, RuntimeClasses.getSuperTypeName(interfaceFqn, scoutProject.getJavaProject()));
    m_defaultIndex = indexOf(m_proposals, defaultVal);
  }

  public int getInitialSelectetdIndex() {
    return m_initialSelectedIndex;
  }

  public int getDefaultIndex() {
    return m_defaultIndex;
  }

  public String[] getProposalDisplayTexts() {
    return Arrays.copyOf(m_proposalDisplayTexts, m_proposalDisplayTexts.length);
  }

  public String[] getProposals() {
    return Arrays.copyOf(m_proposals, m_proposals.length);
  }

  public static int indexOf(String[] items, String search) {
    for (int i = 0; i < items.length; i++) {
      if (items[i].equals(search)) {
        return i;
      }
    }
    return -1;
  }

  private static String[] getTypeProposals(IScoutBundle p, String interfaceFqn, String defaultVal) {
    HashSet<String> ret = new HashSet<String>();
    ret.add(defaultVal);
    IType base = TypeUtility.getType(interfaceFqn);
    for (IScoutBundle b : p.getChildBundles(ScoutBundleFilters.getAllBundlesFilter(), true)) {
      IType[] proposals = ScoutTypeUtility.getAbstractTypesOnClasspath(base, b.getJavaProject());
      for (IType t : proposals) {
        if (TypeUtility.exists(t)) {
          ret.add(t.getFullyQualifiedName());
        }
      }
    }
    String[] arr = ret.toArray(new String[ret.size()]);
    Arrays.sort(arr);
    return arr;
  }

  private static String[] getFqnDisplayTexts(String[] items) {
    final int MAX_LEN = 38;
    final String DOTS = "...";
    String[] ret = new String[items.length];
    for (int i = 0; i < items.length; i++) {
      String cur = items[i];
      if (cur.length() > MAX_LEN) {
        int lastDot = cur.lastIndexOf('.') + 1;
        int lastSegLength = cur.length() - lastDot;
        int allowedTextLen = MAX_LEN - DOTS.length();
        if (lastSegLength >= (allowedTextLen - 1)) {
          ret[i] = DOTS + cur.substring(cur.length() - allowedTextLen);
        }
        else {
          String simpleName = cur.substring(cur.length() - lastSegLength);
          int firstSegLen = allowedTextLen - simpleName.length();
          String inject = DOTS;
          if (cur.charAt(firstSegLen - 1) == '.') {
            inject = DOTS.substring(1);
          }
          ret[i] = cur.substring(0, firstSegLen) + inject + simpleName;
        }
      }
      else {
        ret[i] = items[i];
      }
    }
    return ret;
  }

  private static String getLabelTextFromInterfaceFqn(String fqn) {
    Matcher m = INTERFACE_REGEX.matcher(fqn);
    if (m.find()) {
      return SPLIT_REGEX.matcher(m.group(2)).replaceAll(" $1").trim();
    }
    return fqn;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DefaultSuperClassModel)) {
      return false;
    }
    return label.equals(((DefaultSuperClassModel) obj).label);
  }

  @Override
  public int hashCode() {
    return label.hashCode();
  }

  @Override
  public int compareTo(DefaultSuperClassModel o) {
    return label.compareTo(o.label);
  }
}
