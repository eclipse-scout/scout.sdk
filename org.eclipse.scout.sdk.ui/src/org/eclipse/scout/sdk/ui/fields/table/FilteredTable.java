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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class FilteredTable extends Composite {

  private Text m_filterField;
  private Button m_clearFilter;
  private TableViewer m_tableViewer;
  private Table m_table;
  private P_TableFilter m_tableFilter;
  private final int m_style;

  public FilteredTable(Composite parent, int style) {
    super(parent, SWT.NONE);
    m_style = style;
    m_tableFilter = new P_TableFilter();
    GridLayout layout = new GridLayout(1, true);
    layout.horizontalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    setLayout(layout);
    createContent(this);
  }

  protected void createContent(Composite parent) {

    Control filterControl = createFilterControl(parent);
    m_table = new AutoResizeColumnTable(parent, m_style);

    m_tableViewer = new TableViewer(m_table) {

      @Override
      protected Object[] getSortedChildren(Object parentNode) {
        // ensure no separator at beginning/end or siblings
        ArrayList<Object> cleanedElements = new ArrayList<Object>(Arrays.asList(super.getSortedChildren(parentNode)));
        boolean removeNextSeperator = true;
        for (Iterator<Object> it = cleanedElements.iterator(); it.hasNext();) {
          Object cur = it.next();
          if (cur instanceof ISeparator) {
            if (removeNextSeperator || !it.hasNext()) {
              it.remove();
            }
            removeNextSeperator = true;
          }
          else {
            removeNextSeperator = false;
          }
        }
        return cleanedElements.toArray(new Object[cleanedElements.size()]);
      }
    };
    m_tableViewer.setFilters(new ViewerFilter[]{m_tableFilter});
    m_tableViewer.setSorter(new P_TableSorter());
    // layout
    filterControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_table.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
  }

  private Control createFilterControl(Composite parent) {
    Composite filterComposite = new Composite(parent, SWT.NONE);
    m_filterField = new Text(filterComposite, SWT.BORDER);
    m_filterField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_tableFilter.setFilterText(m_filterField.getText());
        refresh(true);
        GridData buttonData = (GridData) m_clearFilter.getLayoutData();
        if (StringUtility.isNullOrEmpty(m_filterField.getText())) {
          buttonData.exclude = true;
          m_clearFilter.setEnabled(false);
        }
        else {
          buttonData.exclude = false;
          m_clearFilter.setEnabled(true);
        }
        m_filterField.getParent().layout(true);
      }
    });
    m_filterField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.keyCode == SWT.ARROW_DOWN) {
          m_table.setFocus();
        }
      }
    });
    m_clearFilter = new Button(filterComposite, SWT.PUSH | SWT.NO_FOCUS);
    m_clearFilter.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolDelete));
    m_clearFilter.setEnabled(false);
    m_clearFilter.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_filterField.setText("");
        m_filterField.setFocus();
      }
    });
    filterComposite.setTabList(new Control[]{m_filterField});
    // layout
    GridLayout layout = new GridLayout(2, false);
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    filterComposite.setLayout(layout);
    m_filterField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    GridData clearFieldData = new GridData(20, 20);
    clearFieldData.horizontalIndent = 3;
    clearFieldData.exclude = true;
    m_clearFilter.setLayoutData(clearFieldData);
    return filterComposite;
  }

  public void refresh(boolean selectFirst) {
    m_tableViewer.refresh();
    if (selectFirst) {
      StructuredSelection selection = new StructuredSelection();
      if (m_tableViewer.getTable().getItemCount() > 0) {
        Object data = m_table.getItem(0).getData();
        selection = new StructuredSelection(data);
      }
      m_tableViewer.setSelection(selection);
    }
  }

  public Table getTable() {
    return m_table;
  }

  public TableViewer getViewer() {
    return m_tableViewer;
  }

  @Override
  public void setEnabled(boolean enabled) {
    m_table.setEnabled(enabled);
    m_filterField.setEnabled(enabled);
    super.setEnabled(enabled);
  }

  @Override
  public boolean isEnabled() {
    if (m_table.getEnabled() && m_filterField.getEnabled()) {
      return super.isEnabled();
    }
    return false;
  }

  @Override
  public boolean getEnabled() {
    return m_table.getEnabled() && m_filterField.getEnabled();
  }

  private class P_TableFilter extends ViewerFilter {
    private String m_filterString = "*";

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      TableViewer tableViewer = (TableViewer) viewer;
      if (element instanceof ISeparator) {
        return true;
      }
      ITableLabelProvider labelProvider = (ITableLabelProvider) tableViewer.getLabelProvider();
      String columnText = labelProvider.getColumnText(element, 0);
      return (!StringUtility.isNullOrEmpty(columnText)) && CharOperation.match(m_filterString.toCharArray(), columnText.toCharArray(), false);
    }

    public void setFilterText(String filterText) {
      if (filterText == null) {
        filterText = "";
      }
      filterText.replaceAll("^\\**", "*");
      filterText.replaceAll("\\**$", "*");
      m_filterString = "*" + filterText.toLowerCase() + "*";
    }

  } // end class P_TableFilter

  /**
   * <h3>{@link P_TableSorter}</h3> ...
   * The default table sorter. Can be replaced by setting any other sorter or null afterwards.
   * 
   * @author Andreas Hoegger
   * @since 3.8.0 27.01.2012
   */
  private class P_TableSorter extends ViewerSorter {
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
      IBaseLabelProvider provider = getViewer().getLabelProvider();
      if (provider instanceof ITableLabelProvider) {
        ITableLabelProvider labelProvider = (ITableLabelProvider) provider;
        return labelProvider.getColumnText(e1, 0).compareTo(labelProvider.getColumnText(e2, 0));
      }
      return -1;
    }
  }

}
