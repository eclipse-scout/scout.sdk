/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.m
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.util.signature.internal;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.sdk.util.signature.ITypeGenericMapping;

/**
 * <h3>{@link TypeGenericMapping}</h3> ...
 *
 * @author Andreas Hoegger
 * @since 3.9.0 20.03.2013
 */
public class TypeGenericMapping implements ITypeGenericMapping {

  private final String m_fullyQualifiedName;
  private final Map<String /*parameter name*/, String /*param signature*/> m_parameters;

  public TypeGenericMapping(String fullyQualliefiedName) {
    m_fullyQualifiedName = fullyQualliefiedName;
    m_parameters = new LinkedHashMap<String, String>();
  }

  @Override
  public String getFullyQualifiedName() {
    return m_fullyQualifiedName;
  }

  public void addParameter(String name, String signature) {
    m_parameters.put(name, signature);
  }

  @Override
  public String getParameterSignature(String paramName) {
    return m_parameters.get(paramName);
  }

  @Override
  public int getParameterCount() {
    return m_parameters.size();
  }

  @Override
  public String[] getParameter(int index) {
    int curIndex = 0;
    for (Entry<String, String> entry : m_parameters.entrySet()) {
      if (index == curIndex) {
        return new String[]{entry.getKey(), entry.getValue()};
      }
      curIndex++;
    }
    return null;
  }

  @Override
  public Map<String, String> getParameters() {
    return Collections.unmodifiableMap(m_parameters);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(getFullyQualifiedName());
    builder.append('{');
    if (!m_parameters.isEmpty()) {
      Iterator<Entry<String, String>> iterator = m_parameters.entrySet().iterator();
      Entry<String, String> e = iterator.next();
      builder.append(e.getKey()).append(" -> ").append(e.getValue());
      while (iterator.hasNext()) {
        e = iterator.next();
        builder.append(" | ").append(e.getKey()).append(" -> ").append(e.getValue());
      }
    }
    builder.append('}');
    return builder.toString();
  }
}
