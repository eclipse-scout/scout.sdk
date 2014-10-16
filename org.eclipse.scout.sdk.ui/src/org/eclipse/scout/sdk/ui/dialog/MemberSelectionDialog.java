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
package org.eclipse.scout.sdk.ui.dialog;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.sdk.ui.fields.table.AutoResizeColumnTable;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

public class MemberSelectionDialog extends TitleAreaDialog {

  private Set<? extends IMember> m_members;
  private Set<? extends IMember> m_selectedMembers;
  private CheckboxTableViewer m_viewer;
  private EventListenerList m_listeners;
  private final String m_title;
  private final String m_message;

  public MemberSelectionDialog(Shell parentShell, String title) {
    this(parentShell, title, null);
  }

  public MemberSelectionDialog(Shell parentShell, String title, String message) {
    super(parentShell);
    m_title = title;
    m_message = message;
    m_listeners = new EventListenerList();
    setShellStyle(getShellStyle() | SWT.RESIZE);
    setHelpAvailable(false);
  }

  @Override
  protected Control createContents(Composite parent) {
    Control ret = super.createContents(parent);
    if (m_message != null) {
      setMessage(m_message);
    }
    if (m_title != null) {
      setTitle(m_title);
    }
    return ret;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    if (m_title != null) {
      newShell.setText(m_title);
    }
  }

  public void setMembers(Set<? extends IMember> members) {
    m_members = new LinkedHashSet<IMember>(members);
  }

  public Set<? extends IMember> getMembers() {
    return new LinkedHashSet<IMember>(m_members);
  }

  public void setSelectedMembers(Set<? extends IMember> selectedMembers) {
    if (selectedMembers == null) {
      m_selectedMembers = new LinkedHashSet<IMember>(0);
    }
    else {
      m_selectedMembers = new LinkedHashSet<IMember>(selectedMembers);
    }

    if (m_viewer != null && !m_viewer.getTable().isDisposed()) {
      m_viewer.setCheckedElements(m_selectedMembers.toArray(new IMember[m_selectedMembers.size()]));
      fireSelectionChanged(m_selectedMembers);
    }
  }

  public Set<? extends IMember> getSelectedMembers() {
    return new LinkedHashSet<IMember>(m_selectedMembers);
  }

  public Button getOkButton() {
    return getButton(OK);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite rootPane = new Composite(parent, SWT.NONE);
    AutoResizeColumnTable table = new AutoResizeColumnTable(rootPane, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
    TableColumn simpleNameCol = new TableColumn(table, SWT.LEFT);
    simpleNameCol.setData(AutoResizeColumnTable.COLUMN_WEIGHT, Integer.valueOf(3));
    simpleNameCol.setWidth(170);
    simpleNameCol.setText("Member");
    TableColumn packageCol = new TableColumn(table, SWT.LEFT);
    packageCol.setData(AutoResizeColumnTable.COLUMN_WEIGHT, Integer.valueOf(5));
    packageCol.setText("Package");
    packageCol.setWidth(270);
    m_viewer = new CheckboxTableViewer(table);
    m_viewer.addCheckStateListener(new ICheckStateListener() {
      @Override
      public void checkStateChanged(CheckStateChangedEvent event) {
        Object[] checkedElements = m_viewer.getCheckedElements();
        Set<IMember> newChecked = new LinkedHashSet<IMember>(checkedElements.length);
        for (Object m : checkedElements) {
          if (m instanceof IMember) {
            newChecked.add((IMember) m);
          }
        }
        m_selectedMembers = newChecked;
        fireSelectionChanged(m_selectedMembers);
      }
    });
    P_TableContentProvider provider = new P_TableContentProvider();
    m_viewer.setContentProvider(provider);
    m_viewer.setLabelProvider(provider);
    m_viewer.setInput(provider);
    m_viewer.setCheckedElements(getSelectedMembers().toArray());

    Control buttonArea = createButtons(rootPane);
    // layout
    if (parent.getLayout() instanceof GridLayout) {
      rootPane.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
    }
    rootPane.setLayout(new GridLayout(1, true));
    m_viewer.getTable().setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL));
    buttonArea.setLayoutData(new GridData());
    return rootPane;
  }

  protected Control createButtons(Composite parent) {
    Composite buttonArea = new Composite(parent, SWT.NONE);
    Button selectAll = new Button(buttonArea, SWT.PUSH | SWT.FLAT);
    selectAll.setText("select All");
    selectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setSelectedMembers(getMembers());
      }
    });
    Button deselectAll = new Button(buttonArea, SWT.PUSH | SWT.FLAT);
    deselectAll.setText("deselect All");
    deselectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setSelectedMembers(null);
      }
    });
    // layout
    GridLayout layout = new GridLayout(2, true);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.marginBottom = 5;
    buttonArea.setLayout(layout);
    selectAll.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    deselectAll.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    return buttonArea;
  }

  public void addMemberSelectionListener(IMemberSelectionChangedListener listener) {
    m_listeners.add(IMemberSelectionChangedListener.class, listener);
  }

  public void removeMemberSelectionListener(IMemberSelectionChangedListener listener) {
    m_listeners.remove(IMemberSelectionChangedListener.class, listener);
  }

  protected void fireSelectionChanged(final Set<? extends IMember> selectedMembers) {
    for (final IMemberSelectionChangedListener listener : m_listeners.getListeners(IMemberSelectionChangedListener.class)) {
      SafeRunner.run(new SafeRunnable() {
        @Override
        public void run() throws Exception {
          listener.handleSelectionChanged(selectedMembers);
        }
      });
    }
  }

  private class P_TableContentProvider implements IStructuredContentProvider, ITableLabelProvider {

    @Override
    public Object[] getElements(Object inputElement) {
      return getMembers().toArray();
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      Image img = null;
      if (columnIndex == 0) {
        try {
          switch (((IMember) element).getElementType()) {
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
      IMember member = (IMember) element;
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
}
