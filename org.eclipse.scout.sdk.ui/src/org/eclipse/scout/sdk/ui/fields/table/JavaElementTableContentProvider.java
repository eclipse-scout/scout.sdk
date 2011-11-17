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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

public class JavaElementTableContentProvider implements IStructuredContentProvider, ITableLabelProvider {
  private IJavaElement[] m_elements = new IJavaElement[0];

  public void setElements(IJavaElement[] elements) {
    m_elements = elements;
  }

  public IJavaElement[] getElements() {
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
      try {
        switch (((IJavaElement) element).getElementType()) {
          case IJavaElement.TYPE:
            if (((IType) element).isInterface()) {
              img = ScoutSdkUi.getImage(ScoutSdkUi.Interface);
            }
            else {
              img = ScoutSdkUi.getImage(ScoutSdkUi.Class);
            }
            break;
          case IJavaElement.METHOD:
            img = ScoutSdkUi.getImage(ScoutSdkUi.Public);
            break;
          case IJavaElement.FIELD:
            img = ScoutSdkUi.getImage(ScoutSdkUi.FieldPrivate);
            break;
          default:
            img = ScoutSdkUi.getImage(ScoutSdkUi.Default);
            break;
        }
      }
      catch (JavaModelException e) {
        ScoutSdkUi.logWarning(e);
      }
    }
    return img;
  }

  @Override
  public String getColumnText(Object element, int columnIndex) {
    IJavaElement member = (IJavaElement) element;
    switch (columnIndex) {
      case 0:
        return member.getElementName();
      case 1:
        if (member.getElementType() == IJavaElement.TYPE) {
          return ((IType) member).getPackageFragment().getElementName();
        }
        else if (member.getElementType() == IJavaElement.METHOD) {
          return ((IMethod) member).getDeclaringType().getFullyQualifiedName();

        }
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
