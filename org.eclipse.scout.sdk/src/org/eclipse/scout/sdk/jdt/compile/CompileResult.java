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
package org.eclipse.scout.sdk.jdt.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.scout.commons.CompositeObject;

/**
 * <h3>{@link CompileResult}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.9.0 18.03.2013
 */
public class CompileResult implements ICompileResult {

  private Map<CompositeObject, IMarker> m_markers;
  private int m_severity;

  public CompileResult() {
    m_markers = new TreeMap<CompositeObject, IMarker>();
  }

  /**
   * @param m
   */
  public void addMarker(IMarker m) {
    int severity = m.getAttribute(IMarker.SEVERITY, -1);
    m_markers.put(new CompositeObject(severity, m.getAttribute(IMarker.LOCATION, ""), m), m);
    m_severity = Math.max(severity, m_severity);
  }

  @Override
  public List<IMarker> getErrorMarkers() {
    List<IMarker> errorMarkers = new ArrayList<IMarker>();
    for (IMarker m : m_markers.values()) {
      if (m.getAttribute(IMarker.SEVERITY, -1) >= IMarker.SEVERITY_ERROR) {
        errorMarkers.add(m);
      }
    }
    return errorMarkers;
  }

  @Override
  public List<IMarker> getErrorWarnings() {
    return null;
  }

  @Override
  public int getSeverity() {
    return m_severity;
  }

}
