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
package org.eclipse.scout.sdk.operation.project.template;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link EmptyTemplateOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2011
 */
public class EmptyTemplateOperation implements IScoutProjectTemplateOperation {

  IScoutProject m_scoutProject;

  @Override
  public String getDescription() {

    return "Creates an empty Scout applicaiton.";
  }

  @Override
  public String getTemplateName() {
    return "An empty application.";
  }

  @Override
  public String getOperationName() {
    return "Apply empty template...";
  }

  @Override
  public void validate() throws IllegalArgumentException {

  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {

  }

  /**
   * @return the scoutProject
   */
  public IScoutProject getScoutProject() {
    return m_scoutProject;
  }

  /**
   * @param scoutProject
   *          the scoutProject to set
   */
  public void setScoutProject(IScoutProject scoutProject) {
    m_scoutProject = scoutProject;
  }

}
