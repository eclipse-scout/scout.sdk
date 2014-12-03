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
package org.eclipse.scout.sdk.workspace.type.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 *
 */
public class ConfigPropertyType {
  private final IType m_type;
  private final Map<String, ConfigurationMethod> m_configurationMethods;
  private final Deque<IType> m_affectedTypes;
  private final ITypeHierarchy m_superTypeHierarchy;

  public ConfigPropertyType(IType type) throws CoreException {
    m_type = type;
    m_superTypeHierarchy = TypeUtility.getSupertypeHierarchy(type);
    m_affectedTypes = m_superTypeHierarchy.getSuperClassStack(m_type);

    TreeMap<String, ConfigurationMethod> configurationMethods = new TreeMap<String, ConfigurationMethod>(new P_MethodNameComparator());
    for (IType t : m_affectedTypes) {
      for (IMethod m : t.getMethods()) {
        if (TypeUtility.exists(m) && !m.isConstructor()) {
          int flags = m.getFlags();
          if (!Flags.isPrivate(flags) && !Flags.isStatic(flags) && !Flags.isBridge(flags)) {
            String methodName = m.getElementName();
            ConfigurationMethod existing = configurationMethods.get(methodName);
            if (existing == null) {
              existing = ScoutTypeUtility.getConfigurationMethod(m_type, methodName, m_superTypeHierarchy);
              if (existing != null) {
                configurationMethods.put(methodName, existing);
              }
            }
          }
        }
      }
    }
    m_configurationMethods = configurationMethods;
  }

  public IType getType() {
    return m_type;
  }

  public ConfigurationMethod getConfigurationMethod(String name) {
    return m_configurationMethods.get(name);
  }

  public ConfigurationMethod getConfigurationMethod(IMethod method) {
    return m_configurationMethods.get(method.getElementName());
  }

  public List<ConfigurationMethod> getConfigurationMethods(int methodType) {
    Collection<ConfigurationMethod> values = m_configurationMethods.values();
    List<ConfigurationMethod> result = new ArrayList<ConfigurationMethod>(values.size());
    for (ConfigurationMethod m : values) {
      if (m.getMethodType() == methodType) {
        result.add(m);
      }
    }
    return result;
  }

  /**
   * @param declaringType
   * @return
   */
  public boolean isRelevantType(IType type) {
    return m_affectedTypes.contains(type);
  }

  public ConfigurationMethod updateIfChanged(IMethod method) throws CoreException {
    String methodName = method.getElementName();
    ConfigurationMethod newMethod = ScoutTypeUtility.getConfigurationMethod(m_type, methodName, m_superTypeHierarchy);
    if (newMethod != null) {
      ConfigurationMethod oldMethod = m_configurationMethods.get(methodName);
      if (!newMethod.equals(oldMethod)) {
        m_configurationMethods.put(methodName, newMethod);
        return newMethod;
      }
    }
    return null;
  }

  private class P_MethodNameComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
      if (o1 == null) {
        if (o2 == null) {
          return 0;
        }
        else {
          return -1;
        }
      }
      else {
        if (o2 == null) {
          return 1;
        }
        else {
          return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
      }
    }
  }
}
