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
package org.eclipse.scout.sdk.workspace;

import org.eclipse.core.resources.IProject;
import org.eclipse.scout.sdk.internal.workspace.ScoutProject;

/**
 *
 */
public interface IScoutWorkspace {
  /**
   * @param listener
   */
  void addWorkspaceListener(IScoutWorkspaceListener listener);

  /**
   * @param listener
   */
  void removeWorkspaceListener(IScoutWorkspaceListener listener);

  /**
   * @return
   */
  IScoutBundle[] getAllBundles();

  /**
   * @param project
   * @return
   */
  IScoutBundle getScoutBundle(IProject project);

  IScoutProject[] getRootProjects();

  /**
   * @param scoutProject
   * @return
   */
  IScoutProject getParentProject(ScoutProject scoutProject);

  IScoutProject getScoutProject(IScoutBundle bundle);

  /**
   * @param scoutProject
   * @return
   */
  IScoutProject[] getSubProjects(IScoutProject scoutProject);

  /**
   * @param projectName
   * @return
   */
  IScoutProject findScoutProject(String projectName);

  /**
   * @param p
   * @return
   */
  IScoutProject getScoutProject(IProject p);

}
