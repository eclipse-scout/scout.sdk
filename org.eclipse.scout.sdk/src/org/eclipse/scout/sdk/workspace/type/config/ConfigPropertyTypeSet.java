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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CollectionUtility;

/**
 *
 */
public class ConfigPropertyTypeSet {
  private final Map<IType, ConfigPropertyType> m_propertyTypes;
  private final Map<String, ConfigurationMethodSet> m_commonConfigPropertyMethodSets;

  public ConfigPropertyTypeSet(List<IType> types) throws CoreException {
    m_propertyTypes = new HashMap<>();
    m_commonConfigPropertyMethodSets = new TreeMap<>();
    init(types);
  }

  private void init(List<IType> types) throws CoreException {
    ConfigPropertyType reference = null;
    for (IType t : types) {
      ConfigPropertyType value = new ConfigPropertyType(t);
      if (reference == null) {
        reference = value;
      }
      else {
        m_propertyTypes.put(t, value);
      }
    }
    // find common methods
    if (reference != null) {
      for (ConfigurationMethod m : reference.getConfigurationMethods(ConfigurationMethod.PROPERTY_METHOD)) {
        ConfigurationMethodSet mSet = new ConfigurationMethodSet(m.getMethodName(), m.getConfigAnnotationType());
        mSet.add(m);
        for (ConfigPropertyType t : m_propertyTypes.values()) {
          ConfigurationMethod localMethod = t.getConfigurationMethod(m.getMethodName());
          if (localMethod != null && m.getConfigAnnotationType().equals(localMethod.getConfigAnnotationType())) {
            mSet.add(localMethod);
          }
          else {
            mSet = null;
            break;
          }
        }
        if (mSet != null) {
          m_commonConfigPropertyMethodSets.put(m.getMethodName(), mSet);
        }
      }
      m_propertyTypes.put(reference.getType(), reference);
    }
  }

  public List<ConfigurationMethodSet> getCommonConfigPropertyMethodSets() {
    return CollectionUtility.arrayList(m_commonConfigPropertyMethodSets.values());
  }

  public boolean hasConfigPropertyMethods() {
    return m_commonConfigPropertyMethodSets.size() > 0;
  }

  public ConfigurationMethodSet getConfigurationMethodSet(String methodName) {
    return m_commonConfigPropertyMethodSets.get(methodName);
  }

  /**
   * @param declaringType
   * @return
   */
  public boolean isRelevantType(IType declaringType) {
    for (ConfigPropertyType type : m_propertyTypes.values()) {
      if (type.isRelevantType(declaringType)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param method
   * @return
   * @throws CoreException
   */
  public ConfigurationMethod updateIfChanged(IMethod method) throws CoreException {
    for (ConfigPropertyType type : m_propertyTypes.values()) {
      ConfigurationMethod oldMethod = type.getConfigurationMethod(method);
      ConfigurationMethod newConfigurationMethod = type.updateIfChanged(method);
      if (newConfigurationMethod != null) {
        ConfigurationMethodSet set = m_commonConfigPropertyMethodSets.get(newConfigurationMethod.getMethodName());
        set.remove(oldMethod);
        set.add(newConfigurationMethod);
        return newConfigurationMethod;
      }
    }
    return null;
  }

}
