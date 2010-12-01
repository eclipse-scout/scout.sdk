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
package org.eclipse.scout.nls.sdk.internal.model;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.model.workspace.InheritedNlsEntry;
import org.eclipse.scout.nls.sdk.internal.ui.editor.NlsTable;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/** a wrapper to the Nls Model providing TableViewer methods */
public class NlsTableModel extends ViewerComparator implements IStructuredContentProvider, ITableLabelProvider, ITableColorProvider {

  private int m_sortIndex = -2;
  private boolean m_ascSorting = false;
  private final INlsProject m_project;
  private IReferenceProvider m_referenceProvider;

  public NlsTableModel(INlsProject project) {
    m_project = project;
  }

  public Object[] getElements(Object inputElement) {
    return getProject().getAllEntries();
    // ArrayList<INlsEntry> rows = new ArrayList<INlsEntry> (getProject().getAllRows().values());
    // INlsProject parentProject = getProject().getParent();
    // while(parentProject != null){
    // for(INlsEntry r : parentProject.getall){
    // rows.add(new InheritedNlsEntry(r));
    // }
    // parentProject = parentProject.getParent();
    // }
    // return rows.toArray(new INlsEntry[rows.size()]);//m_entries.values().toArray();//m_model.getAllKeys().toArray();
  }

  public void dispose() {
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  public Image getColumnImage(Object element, int columnIndex) {
    Image img = null;
    if (columnIndex == 0) {
      img = NlsCore.getImage(NlsCore.Text);
    }
    return img;

  }

  public String getColumnText(Object element, int columnIndex) {
    INlsEntry row = (INlsEntry) element;
    switch (columnIndex) {
      case 0: {
        if (getReferenceProvider() != null) {
          return "" + getReferenceProvider().getReferenceCount(row);
        }

        return "";
      }
      case 1: {
        return row.getKey();
      }
      default: {
        Language lang = getProject().getAllLanguages()[columnIndex - (NlsTable.AMOUNT_UTILITY_COLS + 1)];
        String text = row.getTranslation(lang);
        if (text == null) {
          return "";
        }
        return text;

      }
    }
  }

  public Color getBackground(Object element, int columnIndex) {
    // if(columnIndex < 2){
    // return null;
    // }
    // if(m_languageOrder.get(columnIndex - (NlsTable.AMOUNT_UTILITY_COLS +1)).isLocal()){
    // return null;
    // }
    // else{
    // return Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
    // }
    return null;
  }

  public Color getForeground(Object element, int columnIndex) {
    Color c = null;
    if (element instanceof InheritedNlsEntry) {
      c = NlsCore.getColor(NlsCore.COLOR_NLS_ROW_INACTIVE_FOREGROUND);
    }
    return c;
    // NlsTableRow row = (NlsTableRow) element;
    // if(columnIndex < 2){
    // return null;
    // }
    // if(m_languageOrder.get(columnIndex - (NlsTable.AMOUNT_UTILITY_COLS +1)).isLocal()){
    // return null;
    // }
    // else{
    // return Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
    // }
  }

  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    int index = m_sortIndex;
    if (index < NlsTable.AMOUNT_UTILITY_COLS) {
      index = NlsTable.AMOUNT_UTILITY_COLS;
    }
    if (m_ascSorting) {
      return getColumnText(e2, index).toLowerCase().compareTo(getColumnText(e1, index).toLowerCase());

    }
    else {
      return getColumnText(e1, index).toLowerCase().compareTo(getColumnText(e2, index).toLowerCase());
    }

  }

  public boolean isAscSorting() {
    return m_ascSorting;
  }

  public void setAscSorting(boolean sorting) {
    m_ascSorting = sorting;
  }

  public int getSortIndex() {
    return m_sortIndex;
  }

  public void setSortIndex(int index) {
    m_sortIndex = index;
  }

  public void addListener(ILabelProviderListener listener) {
    // TODO Auto-generated method stub

  }

  public boolean isLabelProperty(Object element, String property) {
    // TODO Auto-generated method stub
    return false;
  }

  public void removeListener(ILabelProviderListener listener) {
    // TODO Auto-generated method stub

  }

  public INlsProject getProject() {
    return m_project;
  }

  public void setReferenceProvider(IReferenceProvider referenceProvider) {
    m_referenceProvider = referenceProvider;
  }

  public IReferenceProvider getReferenceProvider() {
    return m_referenceProvider;
  }

}
