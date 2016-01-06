/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.ui.editor;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.nls.INlsIcons;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.internal.model.InheritedNlsEntry;
import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.scout.sdk.s2e.nls.model.Language;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link NlsTableModel}</h3> a wrapper to the Nls Model providing TableViewer methods
 *
 * @author Andreas Hoegger
 * @since 1.1.0
 */
public class NlsTableModel extends ViewerComparator implements IStructuredContentProvider, ITableLabelProvider, ITableColorProvider {

  private int m_sortIndex = -2;
  private boolean m_ascSorting = false;
  private final INlsProject m_projects;
  private IReferenceProvider m_referenceProvider;

  public NlsTableModel(INlsProject projects) {
    m_projects = projects;
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (getProjects() == null) {
      return new Object[]{};
    }
    return getProjects().getAllEntries().toArray();
  }

  @Override
  public void dispose() {
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  @Override
  public Image getColumnImage(Object element, int columnIndex) {
    Image img = null;
    if (columnIndex == 0) {
      img = NlsCore.getImage(INlsIcons.TEXT);
    }
    return img;
  }

  /**
   * Gets the language that belongs to the given column index of the table.
   *
   * @param colIndex
   *          The zero based column index of the table.
   * @return The language of the column if the given index has a language. Null otherwise (e.g. when the index of the
   *         key column is passed or when the index is out of bounds).
   */
  public Language getLanguageOfColumn(int colIndex) {
    List<Language> allLanguages = getProjects().getAllLanguages();
    int offset = (NlsTable.AMOUNT_UTILITY_COLS + 1);
    if (allLanguages != null && colIndex >= offset && allLanguages.size() > 0 && (allLanguages.size() + offset) > colIndex) {
      return allLanguages.get(colIndex - offset);
    }
    return null;
  }

  @Override
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
        String text = "";
        if (columnIndex > 0) {
          Language lang = getLanguageOfColumn(columnIndex);
          if (lang != null) {
            text = row.getTranslation(lang);
            if (text == null) {
              return "";
            }
          }
        }
        return text.replace("\n", " ").replace("\r", "");
      }
    }
  }

  @Override
  public Color getBackground(Object element, int columnIndex) {
    return null;
  }

  @Override
  public Color getForeground(Object element, int columnIndex) {
    Color c = null;
    if (element instanceof InheritedNlsEntry) {
      c = NlsCore.getColor(NlsCore.COLOR_NLS_ROW_INACTIVE_FOREGROUND);
    }
    return c;
  }

  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    int index = m_sortIndex;
    Object first = null, second = null;
    if (m_ascSorting) {
      first = e2;
      second = e1;
    }
    else {
      first = e1;
      second = e2;
    }

    String a = getColumnText(first, index);
    String b = getColumnText(second, index);

    if (Objects.equals(a, b)) {
      return 0;
    }
    if (a == null) {
      return -1;
    }
    if (b == null) {
      return 1;
    }

    if (index == 0 && a.length() > 0 && b.length() > 0) {
      // sort by NLS entry usage (numeric)
      try {
        Integer numA = Integer.parseInt(a);
        Integer numB = Integer.parseInt(b);
        return numA.compareTo(numB);
      }
      catch (NumberFormatException e) {
        SdkLog.info("no valid number '{}' or '{}'.", a, b, e);
      }
    }

    return a.compareToIgnoreCase(b);
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

  public INlsProject getProjects() {
    return m_projects;
  }

  public void setReferenceProvider(IReferenceProvider referenceProvider) {
    m_referenceProvider = referenceProvider;
  }

  public IReferenceProvider getReferenceProvider() {
    return m_referenceProvider;
  }
}
