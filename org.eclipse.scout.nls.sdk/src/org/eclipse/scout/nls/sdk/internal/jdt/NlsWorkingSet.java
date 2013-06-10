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

  @Override
  public IAdaptable[] getElements() {
    return m_projects.toArray(new IAdaptable[m_projects.size()]);
  }

  @Override
  public String getId() {
    return null;
  }

  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  public ImageDescriptor getImage() {
    return null;
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    return null;
  }

  @Override
  public String getLabel() {
    return getName(" -");
  }

  private String getName(String delim) {
    StringBuilder name = new StringBuilder();
    for (IProject project : m_projects) {
      name.append(delim);
      name.append(project.getName());
    }
    return name.toString();
  }

  @Override
  public String getName() {
    return getName(" ");
  }

  @Override
  public boolean isAggregateWorkingSet() {
    return false;
  }

  @Override
  public boolean isEditable() {
    return false;
  }

  @Override
  public boolean isEmpty() {
    return m_projects.isEmpty();
  }

  @Override
  public boolean isSelfUpdating() {
    return false;
  }

  @Override
  public boolean isVisible() {
    return false;
  }

  @Override
  public void setElements(IAdaptable[] elements) {
  }

  @Override
  public void setId(String id) {
  }

  @Override
  public void setLabel(String label) {
  }

  @Override
  public void setName(String name) {
  }

  @Override
  public String getFactoryId() {
    return null;
  }

  @Override
  public void saveState(IMemento memento) {
  }

  @Override
  public Object getAdapter(Class adapter) {
    return null;
  }

  @Override
  public IAdaptable[] adaptElements(IAdaptable[] objects) {
    return null;
  }
}
