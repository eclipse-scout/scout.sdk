/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.marker.commands;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

public abstract class AbstractExecutableMarkerCommand implements IMarkerCommand {

  private boolean m_doExecute;
  private String m_problemName;
  private IMarker m_marker;
  private String m_solutionDescription;

  public AbstractExecutableMarkerCommand(String problemName, String solutionDescription) {
    m_problemName = problemName;
    m_solutionDescription = solutionDescription;
  }

  public AbstractExecutableMarkerCommand(String problemName) {
    m_problemName = problemName;
  }

  @Override
  public String getProblemName() {
    return m_problemName;
  }

  @Override
  public String getSolutionDescription() {
    return m_solutionDescription;
  }

  public void setSolutionDescription(String solutionDescription) {
    m_solutionDescription = solutionDescription;
  }

  @Override
  public boolean isDoExecute() {
    return m_doExecute;
  }

  @Override
  public void setDoExecute(boolean doExecute) {
    this.m_doExecute = doExecute;
  }

  @Override
  public boolean isExecutable() {
    return true;
  }

  @Override
  public IMarker getMarker() {
    return m_marker;
  }

  @Override
  public void setMarker(IMarker marker) {
    m_marker = marker;
  }

  @Override
  public boolean prepareForUi() throws CoreException {
    return true;
  }
}
