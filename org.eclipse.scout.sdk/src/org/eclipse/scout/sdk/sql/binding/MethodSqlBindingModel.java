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

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.core.IMethod;

/**
 * <h3>{@link MethodSqlBindingModel}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 28.02.2011
 */
public class MethodSqlBindingModel {
  private final IMethod m_method;
  private ArrayList<SQLStatement> m_statements;

  public MethodSqlBindingModel(IMethod method) {
    m_method = method;
    m_statements = new ArrayList<SQLStatement>();
  }

  /**
   * @return the method
   */
  public IMethod getMethod() {
    return m_method;
  }

  public void addStatement(SQLStatement statement) {
    statement.setModel(this);
    m_statements.add(statement);
  }

  public SQLStatement[] getStatements() {
    return m_statements.toArray(new SQLStatement[m_statements.size()]);
  }

  public boolean hasMarkers() {
    for (SQLStatement s : getStatements()) {
      if (s.hasMarkers()) {
        return true;
      }
    }
    return false;
  }

  public static class SQLStatement {
    private int m_offset;
    private int m_length;
    private HashMap<String, Marker> m_markers;
    private MethodSqlBindingModel m_model;

    public SQLStatement(int offset, int length) {
      m_offset = offset;
      m_length = length;
      m_markers = new HashMap<String, Marker>();
    }

    MethodSqlBindingModel getModel() {
      return m_model;
    }

    void setModel(MethodSqlBindingModel model) {
      m_model = model;
    }

    public IMethod getDeclaringMethod() {
      return getModel().getMethod();
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

    public boolean hasMarkers() {
      return !m_markers.isEmpty();
    }

    public Marker[] getMarkers() {
      return m_markers.values().toArray(new Marker[m_markers.size()]);
    }

    public void addMarker(String bindVar, Marker marker) {
      m_markers.put(bindVar, marker);
    }

  }

  public static class Marker {
    private final int m_severity;
    private final String m_bindVar;
    private SQLStatement m_statement;

    public Marker(String bindVar, int severity) {
      m_bindVar = bindVar;
      m_severity = severity;
    }

    /**
     * @return the bindVar
     */
    public String getBindVariable() {
      return m_bindVar;
    }

    void setStatement(SQLStatement statement) {
      m_statement = statement;
    }

    SQLStatement getStatement() {
      return m_statement;
    }

    public IMethod getDeclaringMethod() {
      return getStatement().getDeclaringMethod();
    }

    /**
     * @return the severity
     */
    public int getSeverity() {
      return m_severity;
    }

  }
}
