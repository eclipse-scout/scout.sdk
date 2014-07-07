/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.workspace.type.config.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.IType;

/**
 * <h3>{@link MenuTypesConfig}</h3>
 *
 * @author Matthias Villiger
 * @since 4.0.0 03.06.2014
 */
public class MenuTypesConfig implements Cloneable {
  private final Map<IType, Set<String>> m_activeMenuTypes;

  public MenuTypesConfig() {
    this(new HashMap<IType, Set<String>>());
  }

  private MenuTypesConfig(Map<IType, Set<String>> data) {
    m_activeMenuTypes = data;
  }

  @Override
  public MenuTypesConfig clone() {
    return new MenuTypesConfig(getAll());
  }

  public void add(IType menuType, String val) {
    Set<String> set = m_activeMenuTypes.get(menuType);
    if (set == null) {
      set = new TreeSet<String>();
      m_activeMenuTypes.put(menuType, set);
    }
    set.add(val);
  }

  public void remove(IType menuType, String val) {
    Set<String> set = m_activeMenuTypes.get(menuType);
    if (set != null) {
      set.remove(val);
    }
  }

  public Set<String> getValuesFor(IType menuType) {
    Set<String> set = m_activeMenuTypes.get(menuType);
    if (set != null) {
      return new TreeSet<String>(set);
    }
    else {
      return new TreeSet<String>();
    }
  }

  public Map<IType, Set<String>> getAll() {
    Map<IType, Set<String>> ret = new HashMap<IType, Set<String>>(m_activeMenuTypes.size());
    for (Entry<IType, Set<String>> entry : m_activeMenuTypes.entrySet()) {
      ret.put(entry.getKey(), new TreeSet<String>(entry.getValue()));
    }
    return ret;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_activeMenuTypes == null) ? 0 : m_activeMenuTypes.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof MenuTypesConfig)) {
      return false;
    }
    MenuTypesConfig other = (MenuTypesConfig) obj;
    if (m_activeMenuTypes == null) {
      if (other.m_activeMenuTypes != null) {
        return false;
      }
    }
    else if (!m_activeMenuTypes.equals(other.m_activeMenuTypes)) {
      return false;
    }
    return true;
  }
}
