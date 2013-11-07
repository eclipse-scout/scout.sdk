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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.sdk.util.signature.ITypeGenericMapping;

/**
 * <h3>{@link TypeGenericMapping}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.9.0 20.03.2013
 */
public class TypeGenericMapping implements ITypeGenericMapping {

  private final String m_fullyQuallifiedName;
  private Map<String /*parameter name*/, String /*param signature*/> m_parameters;

  public TypeGenericMapping(String fullyQualliefiedName) {
    m_fullyQuallifiedName = fullyQualliefiedName;
    m_parameters = new HashMap<String, String>();
  }

  @Override
  public String getFullyQuallifiedName() {
    return m_fullyQuallifiedName;
  }

  public void addParameter(String name, String signature) {
    m_parameters.put(name, signature);
  }

  @Override
  public String getParameterSignature(String paramName) {
    return m_parameters.get(paramName);
  }

  @Override
  public Map<String, String> getParameters() {
    return Collections.unmodifiableMap(m_parameters);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(getFullyQuallifiedName());
    for (Entry<String, String> e : m_parameters.entrySet()) {
      builder.append("\n  ").append(e.getKey()).append(" -> ").append(e.getValue());
    }
    return builder.toString();
  }
}
