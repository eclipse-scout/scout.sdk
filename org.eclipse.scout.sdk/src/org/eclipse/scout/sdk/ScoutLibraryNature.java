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
package org.eclipse.scout.sdk;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

/**
 * <h3>{@link ScoutLibraryNature}</h3> The
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 31.11.2007
 */
public class ScoutLibraryNature extends PlatformObject implements IProjectNature {

  private IProject m_project;

  @Override
  public void configure() throws CoreException {
  }

  @Override
  public void deconfigure() throws CoreException {
  }

  @Override
  public IProject getProject() {
    return m_project;
  }

  @Override
  public void setProject(IProject project) {
    System.out.println("set scout library nature to '" + project.getName() + "'");
    m_project = project;

  }

}
