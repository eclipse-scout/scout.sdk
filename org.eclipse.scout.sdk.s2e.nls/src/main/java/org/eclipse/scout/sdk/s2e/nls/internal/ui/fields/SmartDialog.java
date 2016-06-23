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
package org.eclipse.scout.sdk.s2e.nls.internal.ui.fields;

import java.text.Collator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class SmartDialog {

  private Shell m_shell;
  private ISmartFieldModel m_smartModel;
  private Table m_table;
  private TableViewer m_viewer;
  private Label m_infoLabel;
  private Point m_defaultSize;

  private final P_SmartFieldTableModel m_smartTableModel;
  private final List<ISmartDialogListener> m_smartDialogListeners;
  private final Shell m_parentShell;

  private static final Collator COLLATOR = Collator.getInstance(Locale.getDefault());

  public SmartDialog(Shell parentShell) {
    m_defaultSize = new Point(200, 250);
    m_smartDialogListeners = new LinkedList<>();
    m_smartTableModel = new P_SmartFieldTableModel();
    m_parentShell = parentShell;
    createComponent(parentShell);
  }

  protected void createComponent(Shell parentShell) {
    m_shell = new Shell(parentShell, SWT.ON_TOP);

    m_shell.addShellListener(new ShellAdapter() {
      @Override
      public void shellClosed(ShellEvent e) {
        e.doit = false;
        m_shell.setVisible(false);
      }

      @Override
      public void shellDeactivated(ShellEvent e) {
        m_shell.setVisible(false);
      }

      @Override
      public void shellActivated(ShellEvent e) {
        updateTableWith();
      }

    });
    m_shell.addControlListener(new ControlAdapter() {
      @Override
      public void controlResized(ControlEvent e) {
        updateTableWith();
      }
    });

    CLabel borderComp = new CLabel(m_shell, SWT.INHERIT_DEFAULT);
    borderComp.setBackground(borderComp.getDisplay().getSystemColor(SWT.COLOR_GRAY));
    Composite rootArea = new Composite(borderComp, SWT.INHERIT_DEFAULT);

    m_table = new Table(rootArea, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
    m_table.setHeaderVisible(false);
    m_table.setLinesVisible(false);
    m_table.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.keyCode == SWT.ARROW_UP && m_table.getSelectionIndex() == 0) {
          m_table.traverse(SWT.TRAVERSE_TAB_PREVIOUS);
        }
      }
    });

    TableColumn col = new TableColumn(m_table, SWT.TRAIL);
    col.setWidth(100);
    m_viewer = new TableViewer(m_table);
    m_viewer.setLabelProvider(m_smartTableModel);
    m_viewer.setContentProvider(m_smartTableModel);
    m_viewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        handleItemSelection(((StructuredSelection) event.getSelection()).getFirstElement());
      }
    });

    m_infoLabel = new Label(rootArea, SWT.INHERIT_DEFAULT);

    // layout
    m_shell.setLayout(new FillLayout());
    FillLayout borderLayout = new FillLayout();
    borderLayout.marginHeight = 2;
    borderLayout.marginWidth = 2;
    borderComp.setLayout(borderLayout);

    rootArea.setLayout(new FormLayout());

    FormData data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    data.bottom = new FormAttachment(m_infoLabel, 0);
    m_table.setLayoutData(data);

    data = new FormData();
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    data.bottom = new FormAttachment(100, 0);
    m_infoLabel.setLayoutData(data);
  }

  private void updateTableWith() {
    m_table.getColumn(0).setWidth(m_table.getClientArea().width);
  }

  protected void lazyCreateComponent() {
    if (m_shell == null || m_shell.isDisposed()) {
      createComponent(m_parentShell);
    }
  }

  public void setSmartFieldModel(ISmartFieldModel model) {
    m_smartModel = model;
  }

  public void setFont(Font font) {
    m_table.setFont(font);
  }

  public Font getFont() {
    return m_table.getFont();
  }

  public void setInitialShellSize(Point initialSize) {
    m_defaultSize = initialSize;
  }

  public void addSmartDialogListener(ISmartDialogListener listener) {
    m_smartDialogListeners.add(listener);
  }

  public void removeSmartDialogListener(ISmartDialogListener listener) {
    m_smartDialogListeners.remove(listener);
  }

  public void lazyOpen(String text) {
    Control c = Display.getDefault().getCursorControl();
    if (c != null) {
      open(c.toDisplay(new Point(0, c.getBounds().height)), text);
    }
    else {
      Rectangle bounds = Display.getDefault().getBounds();
      open(new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2), text);
    }
  }

  public void open(Point location, String text) {
    List<Object> props = m_smartModel.getProposals(text);
    openInternal(location, props);
  }

  public void open(Rectangle bounds, String text) {
    m_defaultSize = new Point(bounds.width, bounds.height);
    open(new Point(bounds.x, bounds.y), text);
  }

  /**
   * opens the dialog if more than one proposal
   *
   * @param location
   * @param text
   *          the filter to get some proposals
   */
  public void lazyOpen(Point location, String text) {
    List<Object> props = m_smartModel.getProposals(text);
    if (props.size() == 1) {
      notifyItemSelection(props.get(0));
    }
    else {
      openInternal(location, props);
    }
  }

  public void lazyOpen(Rectangle bounds, String text) {
    m_defaultSize = new Point(bounds.width, bounds.height);
    lazyOpen(new Point(bounds.x, bounds.y), text);
  }

  private void openInternal(Point location, List<Object> proposals) {
    lazyCreateComponent();
    if (m_defaultSize.x < 0) {
      m_defaultSize.x = 250;
    }
    if (m_defaultSize.y < 0) {
      m_defaultSize.y = 300;
    }
    if (proposals.size() == 0) {
      m_infoLabel.setText("no items could be found");
    }
    else {
      m_infoLabel.setText(proposals.size() + " items found");
    }
    m_smartTableModel.setItems(proposals);
    m_viewer.setInput(m_smartTableModel);
    m_viewer.refresh(true);
    m_shell.layout(true);
    m_shell.setLocation(location);
    m_shell.setSize(m_defaultSize);
    m_shell.setVisible(true);
  }

  protected void handleItemSelection(Object item) {
    notifyItemSelection(((P_CompareableSmartItem) item).getItem());
  }

  public boolean isVisible() {
    return m_shell.isVisible();
  }

  protected void notifyItemSelection(Object item) {
    List<ISmartDialogListener> listeners = new ArrayList<>(m_smartDialogListeners);
    for (ISmartDialogListener listener : listeners) {
      listener.itemSelected(item);
    }
    m_shell.setVisible(false);
  }

  private final class P_SmartFieldTableModel implements IStructuredContentProvider, ITableLabelProvider {
    private final Set<P_CompareableSmartItem> m_items = new TreeSet<>();

    public void setItems(List<Object> items) {
      m_items.clear();
      for (Object object : items) {
        m_items.add(new P_CompareableSmartItem(object));
      }
    }

    @Override
    public Object[] getElements(Object inputElement) {
      return m_items.toArray();
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      if (columnIndex == 0) {
        return m_smartModel.getImage(((P_CompareableSmartItem) element).getItem());
      }
      return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      if (columnIndex == 0) {
        return m_smartModel.getText(((P_CompareableSmartItem) element).getItem());
      }
      return "";
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }
  } // end class P_SmartFieldTableModel

  private final class P_CompareableSmartItem implements Comparable<P_CompareableSmartItem> {
    private final Object m_item;

    private P_CompareableSmartItem(Object item) {
      m_item = item;
    }

    @Override
    public int hashCode() {
      return m_smartModel.getText(m_item).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof P_CompareableSmartItem)) {
        return false;
      }
      String me = m_smartModel.getText(m_item);
      String other = m_smartModel.getText(((P_CompareableSmartItem) obj).getItem());
      return Objects.equals(me, other);
    }

    @Override
    public int compareTo(P_CompareableSmartItem o) {
      return COLLATOR.compare(m_smartModel.getText(m_item), m_smartModel.getText(o.getItem()));
    }

    public Object getItem() {
      return m_item;
    }
  }
}
