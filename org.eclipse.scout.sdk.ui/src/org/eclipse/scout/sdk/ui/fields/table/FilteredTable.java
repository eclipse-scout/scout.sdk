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
import java.util.Iterator;

import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
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

    m_tableViewer = new P_TableViewer(m_table);
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
    m_clearFilter = new Button(filterComposite, SWT.PUSH | SWT.NO_FOCUS | SWT.FLAT);
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

  private final class P_TableFilter extends ViewerFilter {
    private static final char END_SYMBOL = '<';
    private static final char ANY_STRING = '*';
    private static final char BLANK = ' ';
    private static final String DATA_FILTER_RESULT_PREFIX = "filterResult";
    private int m_matchKind;
    private String m_pattern;

    private P_TableFilter() {
      setFilterText("*");
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      if (element instanceof ISeparator) {
        return true;
      }
      String text = ((P_StyledLabelProvider) getViewer().getLabelProvider()).getText(element, 0);
      String filterText = text;
      if (!StringUtility.isNullOrEmpty(filterText)) {
        int index = text.indexOf(JavaElementLabels.CONCAT_STRING);
        if (index > -1) {
          filterText = filterText.substring(0, index);
        }
      }
      int[] matchingRegions = SearchPattern.getMatchingRegions(m_pattern, filterText, m_matchKind);
      viewer.setData(DATA_FILTER_RESULT_PREFIX + Integer.toString(element.hashCode()), new P_FilterResult(m_pattern, text, matchingRegions));
      return matchingRegions != null;
    }

    public void setFilterText(String filterText) {
      if (StringUtility.isNullOrEmpty(filterText)) {
        filterText = "*";
      }
      initializePatternAndMatchKind(filterText);

    }

    private void initializePatternAndMatchKind(String pattern) {
      int length = pattern.length();
      if (length == 0) {
        m_matchKind = SearchPattern.R_EXACT_MATCH;
        m_pattern = pattern;
        return;
      }
      char last = pattern.charAt(length - 1);

      if (pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1) {
        m_matchKind = SearchPattern.R_PATTERN_MATCH;
        switch (last) {
          case END_SYMBOL:
          case BLANK:
            m_pattern = pattern.substring(0, length - 1);
            break;
          case ANY_STRING:
            m_pattern = pattern;
            break;
          default:
            m_pattern = pattern + ANY_STRING;
            break;
        }
        return;
      }

      if (last == END_SYMBOL || last == BLANK) {
        m_pattern = pattern.substring(0, length - 1);
        if (SearchPattern.validateMatchRule(m_pattern, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH) == SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH) {
          m_matchKind = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
        }
        else {
          m_matchKind = SearchPattern.R_EXACT_MATCH;
        }
        return;
      }

      if (SearchPattern.validateMatchRule(pattern, SearchPattern.R_CAMELCASE_MATCH) == SearchPattern.R_CAMELCASE_MATCH) {
        m_matchKind = SearchPattern.R_CAMELCASE_MATCH;
        m_pattern = pattern;
        return;
      }

      m_matchKind = SearchPattern.R_PREFIX_MATCH;
      m_pattern = pattern;
    }

    public P_FilterResult getFilterResult(Viewer viewer, Object element) {
      if (viewer == null) return null;
      String key = DATA_FILTER_RESULT_PREFIX + Integer.toString(element.hashCode());
      return (P_FilterResult) viewer.getData(key);
    }

  } // end class P_TableFilter

  private final class P_FilterResult {
    private final String m_text;
    private final int[] m_matchingRegions;
    private final String m_pattern;

    private P_FilterResult(String pattern, String text, int[] matchingRegions) {
      m_pattern = pattern;
      m_text = text;
      m_matchingRegions = matchingRegions;
    }

    public String getPattern() {
      return m_pattern;
    }

    public String getText() {
      return m_text;
    }

    public int[] getMatchingRegions() {
      return m_matchingRegions;
    }
  }

  /**
   * <h3>{@link P_TableSorter}</h3> ...
   * The default table sorter. Can be replaced by setting any other sorter or null afterwards.
   *
   * @author Andreas Hoegger
   * @since 3.8.0 27.01.2012
   */
  private final class P_TableSorter extends ViewerSorter {
    @Override
    public void sort(Viewer viewer, Object[] elements) {
      super.sort(viewer, elements);

    }

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

  /**
   * <h3>{@link P_TableViewer}</h3> ...
   *
   * @author Andreas Hoegger
   * @since 3.8.0 17.02.2012
   */
  private final class P_TableViewer extends TableViewer {
    /**
     * @param table
     */
    public P_TableViewer(Table table) {
      super(table);
    }

    @Override
    public void setLabelProvider(IBaseLabelProvider labelProvider) {
      if (labelProvider instanceof ITableLabelProvider || labelProvider instanceof ILabelProvider) {
        super.setLabelProvider(new P_StyledLabelProvider(labelProvider));
      }
      else {
        throw new IllegalArgumentException("only allows ITableLabelProvider and ILabelProvider subtypes.");
      }
    }

    @Override
    protected Object[] getSortedChildren(Object parentNode) {
      // ensure no separator at beginning/end or siblings
      ArrayList<Object> cleanedElements = CollectionUtility.arrayList(super.getSortedChildren(parentNode));
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
  } // end class P_TableViewer

  private final class P_StyledLabelProvider extends StyledCellLabelProvider {
    private final IBaseLabelProvider m_wrappedLabelProvider;
    private Font m_boldFont;
    private Styler m_boldStyler;

    private P_StyledLabelProvider(IBaseLabelProvider labelProvider) {
      m_wrappedLabelProvider = labelProvider;
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
      return m_wrappedLabelProvider.isLabelProperty(element, property);
    }

    @Override
    public void initialize(ColumnViewer viewer, ViewerColumn column) {
      super.initialize(viewer, column);
      Font defaultFont = viewer.getControl().getFont();
      FontData[] defaultFontData = defaultFont.getFontData();
      FontData[] boldFontData = new FontData[defaultFontData.length];
      for (int i = 0; i < defaultFontData.length; i++) {
        boldFontData[i] = new FontData(defaultFontData[i].getName(), defaultFontData[i].getHeight(), defaultFontData[i].getStyle() | SWT.BOLD);
      }
      m_boldFont = new Font(viewer.getControl().getDisplay(), boldFontData);
      m_boldStyler = new Styler() {
        @Override
        public void applyStyles(TextStyle textStyle) {
          textStyle.font = m_boldFont;
        }
      };
    }

    @Override
    public void dispose() {
      super.dispose();
      m_wrappedLabelProvider.dispose();
      m_boldFont.dispose();
      m_boldFont = null;
    }

    @Override
    public void update(ViewerCell cell) {
      Object element = cell.getElement();
      StyledString text = new StyledString(getText(element, cell.getColumnIndex()));
      if (cell.getColumnIndex() == 0) {
        P_FilterResult filterResult = m_tableFilter.getFilterResult(getViewer(), element);
        if (filterResult != null && !"*".equals(filterResult.getPattern())) {
          int[] matchingRegions = filterResult.getMatchingRegions();
          if (matchingRegions != null && matchingRegions.length > 0) {

            for (int i = 0; i < matchingRegions.length - 1; i += 2) {
              text.setStyle(matchingRegions[i], matchingRegions[i + 1], m_boldStyler);
            }
          }
          // package information
          if (!StringUtility.isNullOrEmpty(filterResult.getText())) {
            int index = filterResult.getText().indexOf(JavaElementLabels.CONCAT_STRING);
            if (index > 0) {
              text.setStyle(index, text.length() - index, StyledString.QUALIFIER_STYLER);
            }
          }
        }
      }
      cell.setText(text.getString());
      cell.setStyleRanges(text.getStyleRanges());
      cell.setImage(getImage(element, cell.getColumnIndex()));
      super.update(cell);
    }

    private String getText(Object element, int columnIndex) {
      if (m_wrappedLabelProvider instanceof ITableLabelProvider) {
        return ((ITableLabelProvider) m_wrappedLabelProvider).getColumnText(element, columnIndex);
      }
      else if (m_wrappedLabelProvider instanceof ILabelProvider) {
        return ((ILabelProvider) m_wrappedLabelProvider).getText(element);
      }
      return null;
    }

    private Image getImage(Object element, int columnIndex) {
      if (m_wrappedLabelProvider instanceof ITableLabelProvider) {
        return ((ITableLabelProvider) m_wrappedLabelProvider).getColumnImage(element, columnIndex);
      }
      else if (m_wrappedLabelProvider instanceof ILabelProvider) {
        return ((ILabelProvider) m_wrappedLabelProvider).getImage(element);
      }
      return null;
    }

  } // end class P_StyledLabelProvider

}
