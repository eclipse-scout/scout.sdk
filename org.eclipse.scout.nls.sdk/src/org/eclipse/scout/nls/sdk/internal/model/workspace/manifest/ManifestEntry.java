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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;

public class ManifestEntry {
  private String m_key;
  private List<ManifestElement> m_values = new LinkedList<ManifestElement>();

  /**
   * To create a new ManifestEntry with the specified key (e.g. Bundle-SymbolicName)
   * 
   * @param key
   */
  public ManifestEntry(String key) {
    m_key = key;
  }

  /**
   * @param textRepresenation
   *          something like Bundle-SymbolicName: org.eclipse.scout.nls.sdk; singleton:=true
   */
  void parse(String textRepresentation) {
    int seperatorIndex = textRepresentation.indexOf(":");
    m_key = textRepresentation.substring(0, seperatorIndex - 1).trim();
    String valueText = textRepresentation.substring(seperatorIndex + 1, textRepresentation.length());
    parseElements(valueText);
  }

  /**
   * @param textRepresenation
   *          something like org.eclipse.scout.nls.sdk; singleton:=true
   */
  void parseElements(String valuesText) {
    valuesText = valuesText.replaceAll("\n", "");
    for (String value : valuesText.split(",")) {
      ManifestElement element = new ManifestElement();
      element.parse(value);
      m_values.add(element);
    }
  }

  public void addElement(ManifestElement newElement) {
    boolean inserted = false;
    for (ManifestElement element : m_values) {
      if (element.getValue().equals(newElement.getValue())) {
        inserted = true;
        for (Entry<String, String> newProp : newElement.getPropertyMap().entrySet()) {
          element.addProperty(newProp.getKey(), newProp.getValue());

        }
      }
    }
    if (!inserted) {
      m_values.add(newElement);
    }
  }

  public String getKey() {
    return m_key;
  }

  public Collection<ManifestElement> getElements() {
    return new ArrayList<ManifestElement>(m_values);
  }

  public ManifestElement getFirstElement() {
    Assert.isTrue(m_values.size() > 0);
    return m_values.get(0);
  }

  public boolean containsElement(String name) {
    for (ManifestElement element : m_values) {
      if (element.getValue().equals(name)) {
        return true;
      }
    }
    return false;
  }

}
