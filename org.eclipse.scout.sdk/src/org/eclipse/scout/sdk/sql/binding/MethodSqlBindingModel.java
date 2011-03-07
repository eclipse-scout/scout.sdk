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
package org.eclipse.scout.sdk.sql.binding;

import java.util.HashMap;

import org.eclipse.jdt.core.IMethod;

/**
 * <h3>{@link MethodSqlBindingModel}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 28.02.2011
 */
public class MethodSqlBindingModel {
  public HashMap<String, Marker> m_missingBindVars;
  private final IMethod m_method;

  public MethodSqlBindingModel(IMethod method) {
    m_method = method;
    m_missingBindVars = new HashMap<String, Marker>();
  }

  /**
   * @return the method
   */
  public IMethod getMethod() {
    return m_method;
  }

  public void add(String bindVar, Marker marker) {
    m_missingBindVars.put(bindVar, marker);
  }

  public Marker[] getMissingMarkers() {
    return m_missingBindVars.values().toArray(new Marker[m_missingBindVars.size()]);
  }

  public boolean hasMissingBindVars() {
    return !m_missingBindVars.isEmpty();
  }

  public static class Marker {
    private final int m_offset;
    private final int m_length;
    private final int m_severity;
    private final String m_bindVar;
    private final IMethod m_serviceMethod;

    public Marker(String bindVar, int severity, int offset, int length, IMethod serviceMethod) {
      m_bindVar = bindVar;
      m_severity = severity;
      m_offset = offset;
      m_length = length;
      m_serviceMethod = serviceMethod;
    }

    /**
     * @return the bindVar
     */
    public String getBindVariable() {
      return m_bindVar;
    }

    /**
     * @return the severity
     */
    public int getSeverity() {
      return m_severity;
    }

    /**
     * @return the offset
     */
    public int getOffset() {
      return m_offset;
    }

    /**
     * @return the length
     */
    public int getLength() {
      return m_length;
    }

    /**
     * @return the serviceMethod
     */
    public IMethod getServiceMethod() {
      return m_serviceMethod;
    }
  }
}
