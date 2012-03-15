/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.BuildPropertyWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

public class BuildPropertiesWizardPage extends AbstractWorkspaceWizardPage {

  private List<BuildProperty> m_properties;

  private TableViewer m_tableViewer;
  private Button m_addButton;
  private Button m_editButton;
  private Button m_removeButton;

  public BuildPropertiesWizardPage() {
    super(BuildPropertiesWizardPage.class.getName());
    setTitle(Texts.get("WsBuildDirectives"));
    setDescription(Texts.get("DescriptionBuildDirectives"));
  }

  @Override
  protected void createContent(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);

    Composite tableComposite = new Composite(container, SWT.NONE);
    m_tableViewer = new TableViewer(new Table(tableComposite, SWT.BORDER | SWT.FULL_SELECTION));
    m_tableViewer.setUseHashlookup(true);
    m_tableViewer.getTable().setHeaderVisible(true);
    m_tableViewer.getTable().setLinesVisible(false);
    m_tableViewer.addDoubleClickListener(new IDoubleClickListener() {

      @Override
      public void doubleClick(DoubleClickEvent event) {
        BuildProperty property = (BuildProperty) ((IStructuredSelection) m_tableViewer.getSelection()).getFirstElement();
        if (openEditPropertyWizard(property) != null) {
          m_tableViewer.refresh(property);
        }
      }
    });

    m_tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        m_editButton.setEnabled(!event.getSelection().isEmpty());
        m_removeButton.setEnabled(!event.getSelection().isEmpty());
      }
    });

    m_tableViewer.getTable().addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        if (e.keyCode == SWT.DEL) {
          if (m_removeButton.isEnabled()) {
            IStructuredSelection selection = (IStructuredSelection) m_tableViewer.getSelection();
            Iterator iterator = selection.iterator();

            while (iterator.hasNext()) {
              BuildProperty property = (BuildProperty) iterator.next();
              m_properties.remove(property);
            }
            m_tableViewer.refresh();
          }
        }
      }
    });

    TableViewerColumn nameColumn = new TableViewerColumn(m_tableViewer, SWT.LEFT, 0);
    nameColumn.setLabelProvider(new P_LabelProvider());
    nameColumn.getColumn().setResizable(false);
    nameColumn.getColumn().setText(Texts.get("Directive"));

    TableViewerColumn valueColumn = new TableViewerColumn(m_tableViewer, SWT.LEFT, 1);
    valueColumn.setLabelProvider(new P_LabelProvider());
    valueColumn.getColumn().setResizable(true);
    valueColumn.getColumn().setText(Texts.get("Value"));

    m_tableViewer.setContentProvider(new P_ContentProvider());
    m_tableViewer.setInput(m_properties);

    Composite buttonComposite = new Composite(container, SWT.NONE);

    m_addButton = new Button(buttonComposite, SWT.PUSH | SWT.FLAT);
    m_addButton.setText(Texts.get("Add"));
    m_addButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        BuildProperty property = new BuildProperty();
        property = openEditPropertyWizard(property);
        if (property != null) {
          m_properties.add(property);
          m_tableViewer.refresh();
        }
      }
    });

    m_editButton = new Button(buttonComposite, SWT.PUSH | SWT.FLAT);
    m_editButton.setText(Texts.get("Edit"));
    m_editButton.setEnabled(false);
    m_editButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        BuildProperty property = (BuildProperty) ((IStructuredSelection) m_tableViewer.getSelection()).getFirstElement();
        if (openEditPropertyWizard(property) != null) {
          m_tableViewer.refresh(property);
        }
      }
    });

    m_removeButton = new Button(buttonComposite, SWT.PUSH | SWT.FLAT);
    m_removeButton.setText(Texts.get("Remove"));
    m_removeButton.setEnabled(false);
    m_removeButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = (IStructuredSelection) m_tableViewer.getSelection();
        Iterator iterator = selection.iterator();

        while (iterator.hasNext()) {
          BuildProperty property = (BuildProperty) iterator.next();
          m_properties.remove(property);
        }
        m_tableViewer.refresh();
      }
    });

    // layout
    container.setLayout(new GridLayout(2, false));
    // table composite
    GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
    tableComposite.setLayoutData(gd);

    // button composite
    gd = new GridData(GridData.FILL_VERTICAL);
    gd.horizontalAlignment = SWT.TOP;
    buttonComposite.setLayoutData(gd);

    // layout within button composite
    GridLayout layout = new GridLayout(1, true);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    buttonComposite.setLayout(layout);
    m_addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    m_editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    m_removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    // layout within table composite (autoresize of columns)
    TableColumnLayout tableLayout = new TableColumnLayout();
    tableComposite.setLayout(tableLayout);
    tableLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(30));
    tableLayout.setColumnData(valueColumn.getColumn(), new ColumnWeightData(70));
  }

  private BuildProperty openEditPropertyWizard(BuildProperty property) {
    if (property == null) {
      return null;
    }
    // prepare illegal names
    Set<String> illegalNames = new HashSet<String>();
    for (BuildProperty prop : m_properties) {
      if (!CompareUtility.equals(property.getName(), prop.getName())) {
        illegalNames.add(prop.getName());
      }
    }

    BuildPropertyWizard wizard = new BuildPropertyWizard();
    wizard.setDirective(property.getName());
    wizard.setValue(property.getValue());
    wizard.setIllegalNames(illegalNames);

    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setHelpAvailable(false);
    if (wizardDialog.open() == Window.OK) {
      property.setName(wizard.getDirective());
      property.setValue(wizard.getValue());
      return property;
    }
    return null;
  }

  public List<BuildProperty> getProperties() {
    return m_properties;
  }

  public void setProperties(List<BuildProperty> properties) {
    m_properties = properties;
  }

  private class P_LabelProvider extends CellLabelProvider {

    @Override
    public void update(ViewerCell cell) {
      BuildProperty property = (BuildProperty) cell.getElement();
      if (cell.getColumnIndex() == 0) {
        cell.setText(property.getName());
        cell.setImage(JaxWsSdk.getImage(JaxWsIcons.BuildDirective));
      }
      else {
        cell.setText(property.getValue());
      }
    }
  }

  public class P_ContentProvider implements IStructuredContentProvider {

    @Override
    @SuppressWarnings("unchecked")
    public Object[] getElements(Object inputElement) {
      List<BuildProperty> properties = (List<BuildProperty>) inputElement;
      return properties.toArray(new BuildProperty[properties.size()]);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }
}
