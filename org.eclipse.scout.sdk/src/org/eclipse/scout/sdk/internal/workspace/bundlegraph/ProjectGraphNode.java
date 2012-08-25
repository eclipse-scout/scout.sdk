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
package org.eclipse.scout.sdk.internal.workspace.bundlegraph;

import java.util.ArrayList;

import org.eclipse.scout.sdk.internal.workspace.ScoutProject;

/**
 *
 */
public class ProjectGraphNode {
  private ScoutProject m_project;
  private ArrayList<ProjectGraphNode> m_subProjects = new ArrayList<ProjectGraphNode>();
  private ProjectGraphNode m_parentProject;

  ProjectGraphNode(ScoutProject project, ProjectGraphNode parentProject) {
    m_project = project;
    m_parentProject = parentProject;
  }

  void addSubProject(ProjectGraphNode p) {
    m_subProjects.add(p);
  }

  void removeSubProject(ProjectGraphNode p) {
    m_subProjects.remove(p);
  }

  public ProjectGraphNode[] getSubProjects() {
    return m_subProjects.toArray(new ProjectGraphNode[m_subProjects.size()]);
  }

  public ProjectGraphNode getParentProject() {
    return m_parentProject;
  }

  public ScoutProject getScoutProject() {
    return m_project;
  }

}
