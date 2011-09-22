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
package org.eclipse.scout.sdk.ui.fields.table;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

public class FileTableContentProvider implements IStructuredContentProvider, ITableLabelProvider {
  private IFile[] m_elements = new IFile[0];

  public void setElements(IFile[] elements) {
    m_elements = elements;
  }

  public IFile[] getElements() {
    return m_elements;
  }

  @Override
  public Object[] getElements(Object inputElement) {
    return m_elements;
  }

  @Override
  public Image getColumnImage(Object element, int columnIndex) {
    Image img = null;
    if (columnIndex == 0) {
      img = ScoutSdkUi.getImage(ScoutSdkUi.File);
    }
    return img;
  }

  @Override
  public String getColumnText(Object element, int columnIndex) {
    IFile member = (IFile) element;
    switch (columnIndex) {
      case 0:
        return member.getName();
      case 1:
        return member.getProjectRelativePath().toString();
      default:
        return "";
    }
  }

  @Override
  public void dispose() {

  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
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

}
