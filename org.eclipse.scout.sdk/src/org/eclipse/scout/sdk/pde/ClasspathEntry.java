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
package org.eclipse.scout.sdk.pde;

import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;

/**
 * representation of an xml element of the (example) form:
 * <classpathentry
 * kind="lib|src|output|con"
 * path="..."
 * sourcepath="..."
 * exported="true|false"
 * combineaccessrule="true|false"
 * />
 */

public class ClasspathEntry {
  private SimpleXmlElement m_xml;
  private int m_kind;
  private String m_path;
  private String m_sourcepath;
  private boolean m_exported;
  private boolean m_combineaccessrules;

  public static final int KIND_UNKNOWN = 0;
  public static final int KIND_CON = 1;
  public static final int KIND_LIB = 2;
  public static final int KIND_OUTPUT = 3;
  public static final int KIND_SRC = 4;

  protected ClasspathEntry(SimpleXmlElement e) {
    m_xml = e;
    setKind(parseKind(e.getStringAttribute("kind")));
    setPath(e.getStringAttribute("path"));
    setSourcepath(e.getStringAttribute("sourcepath"));
    setExported(e.getBooleanAttribute("exported", "true", "false", false));
    setCombineaccessrules(e.getBooleanAttribute("combineaccessrules", "true", "false", false));
  }

  public void applyChanges() {
    setAttribute("kind", formatKind(getKind()));
    setAttribute("path", getPath());
    setAttribute("sourcepath", getSourcepath());
    setAttribute("exported", isExported() ? "true" : null);
    setAttribute("combineaccessrules", isCombineaccessrules() ? "true" : null);
  }

  private void setAttribute(String name, String value) {
    if (value != null) {
      value = value.trim();
      if (value.length() == 0) value = null;
    }
    if (value != null) {
      m_xml.setAttribute(name, value);
    }
    else {
      m_xml.removeAttribute(name);
    }
  }

  public boolean isExported() {
    return m_exported;
  }

  public void setExported(boolean exported) {
    m_exported = exported;
  }

  public int getKind() {
    return m_kind;
  }

  public void setKind(int kind) {
    switch (kind) {
      case KIND_UNKNOWN:
      case KIND_CON:
      case KIND_LIB:
      case KIND_OUTPUT:
      case KIND_SRC: {
        break;
      }
      default: {
        throw new IllegalArgumentException("invalid kind: " + kind);
      }
    }
    m_kind = kind;
  }

  public String getPath() {
    return m_path;
  }

  public void setPath(String path) {
    m_path = path;
  }

  public String getSourcepath() {
    return m_sourcepath;
  }

  public void setSourcepath(String sourcepath) {
    m_sourcepath = sourcepath;
  }

  public boolean isCombineaccessrules() {
    return m_combineaccessrules;
  }

  public void setCombineaccessrules(boolean combineaccessrules) {
    m_combineaccessrules = combineaccessrules;
  }

  public static String formatKind(int kind) {
    switch (kind) {
      case KIND_CON:
        return "con";
      case KIND_LIB:
        return "lib";
      case KIND_OUTPUT:
        return "output";
      case KIND_SRC:
        return "src";
      default:
        return "";
    }
  }

  public static int parseKind(String kindName) {
    if (kindName == null) kindName = "";
    kindName = kindName.trim().toLowerCase();
    if (kindName.equals("con")) return KIND_CON;
    if (kindName.equals("lib")) return KIND_LIB;
    if (kindName.equals("output")) return KIND_OUTPUT;
    if (kindName.equals("src")) return KIND_SRC;
    return KIND_UNKNOWN;
  }
}
