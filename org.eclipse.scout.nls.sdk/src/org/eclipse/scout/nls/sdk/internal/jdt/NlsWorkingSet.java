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
package org.eclipse.scout.nls.sdk.internal.jdt;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;

public class NlsWorkingSet implements IWorkingSet {

  private Collection<IProject> m_projects;

  public NlsWorkingSet(Collection<IProject> projects) {
    m_projects = projects;
  }

  public IAdaptable[] getElements() {
    return m_projects.toArray(new IAdaptable[m_projects.size()]);
  }

  public String getId() {
    return null;
  }

  @Deprecated
  public ImageDescriptor getImage() {
    return null;
  }

  public ImageDescriptor getImageDescriptor() {
    return null;
  }

  public String getLabel() {
    String name = "";
    for (IProject project : m_projects) {
      name = name + " -" + project.getName();
    }
    return name;
  }

  public String getName() {
    String name = "";
    for (IProject project : m_projects) {
      name = name + " " + project.getName();
    }
    return name;
  }

  public boolean isAggregateWorkingSet() {
    return false;
  }

  public boolean isEditable() {
    return false;
  }

  public boolean isEmpty() {
    return m_projects.isEmpty();
  }

  public boolean isSelfUpdating() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isVisible() {
    // TODO Auto-generated method stub
    return false;
  }

  public void setElements(IAdaptable[] elements) {
    // TODO Auto-generated method stub

  }

  public void setId(String id) {
    // TODO Auto-generated method stub

  }

  public void setLabel(String label) {
    // TODO Auto-generated method stub

  }

  public void setName(String name) {
    // TODO Auto-generated method stub

  }

  public String getFactoryId() {
    // TODO Auto-generated method stub
    return null;
  }

  public void saveState(IMemento memento) {
    // TODO Auto-generated method stub

  }

  public Object getAdapter(Class adapter) {
    // TODO Auto-generated method stub
    return null;
  }

  public IAdaptable[] adaptElements(IAdaptable[] objects) {
    // TODO Auto-generated method stub
    return null;
  }

}
