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
package org.eclipse.scout.sdk.ui.internal.view.icons;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.scout.sdk.ui.fields.proposal.IconProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * <h3>IconTableContentProvider</h3> ...
 */
public class IconTableContentProvider extends ViewerComparator implements IStructuredContentProvider, ITableLabelProvider, ITableColorProvider {

  private IconProposal[] m_icons;

  public IconTableContentProvider() {
    this(new IconProposal[]{});
  }

  public IconTableContentProvider(IconProposal[] icons) {
    setIcons(icons);
  }

  public void setIcons(IconProposal[] icons) {
    m_icons = icons;
  }

  @Override
  public Object[] getElements(Object inputElement) {
    return m_icons;
  }

  @Override
  public Image getColumnImage(Object element, int columnIndex) {
    Image img = null;
    if (columnIndex == 0) {
      img = ((IconProposal) element).getImage();
    }
    return img;
  }

  @Override
  public String getColumnText(Object element, int columnIndex) {
    switch (columnIndex) {
      case 1:
        return ((IconProposal) element).getImageDescription().getId();
      case 2:
        return ((IconProposal) element).getImageDescription().getIconName();
      default:
        return "";
    }
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
  public void dispose() {
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  @Override
  public Color getBackground(Object element, int columnIndex) {
    return null;
  }

  @Override
  public Color getForeground(Object element, int columnIndex) {
    Color c = null;
    if (element instanceof IconProposal) {
      if (((IconProposal) element).getImageDescription().isInherited()) {
        c = ScoutSdkUi.getColor(ScoutSdkUi.COLOR_INACTIVE_FOREGROUND);
      }
    }
    return c;
  }

  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    Table table = (Table) viewer.getControl();
    TableColumn sortCol = table.getSortColumn();
    boolean sortAsc = table.getSortDirection() == SWT.UP;
    int columnIndex = (sortCol != null ? table.indexOf(sortCol) : 1);
    int c = super.compare(viewer, getColumnText(e1, columnIndex), getColumnText(e2, columnIndex));
    if (!sortAsc) {
      c = -c;
    }
    return c;
  }

}
