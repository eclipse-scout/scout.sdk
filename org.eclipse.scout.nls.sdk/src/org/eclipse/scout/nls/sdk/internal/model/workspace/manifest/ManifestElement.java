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
package org.eclipse.scout.nls.sdk.internal.model.workspace.manifest;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.scout.nls.sdk.NlsCore;

public class ManifestElement {

  private String m_value;

  private HashMap<String, String> m_properties = new HashMap<String, String>();

  public ManifestElement(String value) {
    m_value = value;
  }

  ManifestElement() {

  }

  /**
   * Parses the whole value part of a manifest entry (e.g. org.eclipse.scout.nls.sdk;singleton:=true)
   * 
   * @param text
   */
  void parse(String text) {
    text = text.replaceAll("\n", "");
    String[] args = text.split(";", 2);
    m_value = args[0].trim();
    if (args.length > 1) {
      parseProperties(args[1]);
    }
  }

  /**
   * @param elementText
   *          something like singleton:=true;...
   */
  void parseProperties(String elementText) {
    elementText = elementText.replaceAll("\n", "");
    String[] args = elementText.split(";");
    for (int i = 0; i < args.length; i++) {
      addProperty(args[i]);
    }
  }

  private void addProperty(String property) {
    String[] args = property.split("=");
    if (args.length != 2) {
      NlsCore.logWarning("can not parse property: " + property + " of value: " + m_value);

    }
    Assert.isTrue(args.length == 2);
    m_properties.put(args[0].trim(), args[1].trim());
  }

  public String getValue() {
    return m_value;
  }

  public void addProperty(String key, String value) {
    m_properties.put(key, value);
  }

  public boolean hasProperties() {
    return m_properties.size() > 0;
  }

  public String getProperty(String key) {
    return m_properties.get(key);
  }

  public Map<String, String> getPropertyMap() {
    return new HashMap<String, String>(m_properties);
  }
}
