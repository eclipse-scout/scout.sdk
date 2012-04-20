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
package org.eclipse.scout.sdk.ui.fields.proposal.javaelement;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.styled.SearchRangeStyledLabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link JavaElementLabelProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 09.02.2012
 */
public class JavaElementLabelProvider extends SearchRangeStyledLabelProvider implements ITableLabelProvider {

  @Override
  public String getText(Object element) {
    if (element instanceof IJavaElement) {
      return ((IJavaElement) element).getElementName();
    }
    return null;
  }

  @Override
  public String getTextSelected(Object element) {
    StringBuilder textBuilder = new StringBuilder(getText(element));
    if (element instanceof IJavaElement) {
      IJavaElement javaElement = (IJavaElement) element;
      IJavaElement parentElement = javaElement.getParent();
      if (parentElement != null) {
        switch (javaElement.getElementType()) {
          case IJavaElement.TYPE:
            textBuilder.append(" (");
            switch (parentElement.getElementType()) {
              case IJavaElement.TYPE:
                textBuilder.append(((IType) parentElement).getFullyQualifiedName());
                break;
              default:
                textBuilder.append(parentElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT).getElementName());
            }
            textBuilder.append(")");
            break;
          case IJavaElement.METHOD:
          case IJavaElement.FIELD:
            if (parentElement.getElementType() == IJavaElement.TYPE) {
              textBuilder.append(" (");
              textBuilder.append(((IType) parentElement).getFullyQualifiedName());
              textBuilder.append(")");
            }
            break;
        }
      }
    }
    return textBuilder.toString();
  }

  @Override
  public Image getImage(Object element) {
    if (element instanceof IJavaElement) {
      return ScoutSdkUi.getImage((IJavaElement) element);
    }
    else {
      return null;
    }
  }

  @Override
  public Image getImageSelected(Object element) {
    return getImage(element);
  }

  @Override
  public String getColumnText(Object element, int columnIndex) {
    return getText(element);
  }

  @Override
  public Image getColumnImage(Object element, int columnIndex) {
    return getImage(element);
  }
}
