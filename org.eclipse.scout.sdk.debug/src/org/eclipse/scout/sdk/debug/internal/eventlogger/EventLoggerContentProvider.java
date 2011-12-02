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
package org.eclipse.scout.sdk.debug.internal.eventlogger;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.sdk.debug.internal.Activator;
import org.eclipse.swt.graphics.Image;

/**
 *
 */
public class EventLoggerContentProvider implements ITreeContentProvider, ITableLabelProvider {
  private Event m_rootNode;

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    m_rootNode = (Event) newInput;
    viewer.refresh();
  }

  @Override
  public void dispose() {
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (m_rootNode != null) {
      return m_rootNode.getChildren();
    }
    return new Object[0];
  }

  @Override
  public Object getParent(Object element) {
    return null;
  }

  @Override
  public Object[] getChildren(Object parentElement) {
    if (parentElement instanceof Event) {
      return ((Event) parentElement).getChildren();
    }
    return new Object[0];
  }

  @Override
  public boolean hasChildren(Object element) {
    return !((Event) element).isLeaf();
  }

  @Override
  public void addListener(ILabelProviderListener listener) {
  }

  @Override
  public boolean isLabelProperty(Object element, String property) {
    return false;
  }

  @Override
  public void removeListener(ILabelProviderListener listener) {
  }

  @Override
  public Image getColumnImage(Object element, int columnIndex) {
    if (columnIndex == 0) {
      switch (((Event) element).getEventGroup()) {
        case RESOURCE_EVENT:
          return Activator.getImage("file.gif");
        case JDT_EVENT:
          return Activator.getImage("jline_obj.gif");
      }
    }
    return null;
  }

  @Override
  public String getColumnText(Object element, int columnIndex) {
    Event e = (Event) element;
    switch (columnIndex) {
      case 0:
        return (e.getEventType() == null) ? ("") : (e.getEventType().toString());
      case 1:
        return (e.getElementType() == null) ? ("") : (e.getElementType());
      case 2:
        return (e.getElement() == null) ? ("") : (e.getElement());
    }
    return "";
  }

}
