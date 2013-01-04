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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class ResourceSelectionWizardPage extends AbstractWorkspaceWizardPage {

  private CheckboxTableViewer m_tableViewer;
  private List<ElementBean> m_elements;

  public ResourceSelectionWizardPage(String title, String description) {
    super(ResourceSelectionWizardPage.class.getName());
    setTitle(title);
    setDescription(description);
  }

  @Override
  protected void createContent(Composite parent) {
    m_tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
    m_tableViewer.setContentProvider(ArrayContentProvider.getInstance());
    m_tableViewer.setLabelProvider(new P_LabelProvider());

    m_tableViewer.addCheckStateListener(new ICheckStateListener() {

      @Override
      public void checkStateChanged(CheckStateChangedEvent event) {
        ElementBean element = (ElementBean) event.getElement();
        if (element.isMandatory()) {
          m_tableViewer.setChecked(element, true);
        }
        else {
          element.setChecked(event.getChecked());
        }
      }
    });
    m_tableViewer.setInput(m_elements);

    // preselect elements
    List<ElementBean> checkedElements = new LinkedList<ElementBean>();
    for (ElementBean element : m_elements) {
      if (element.isChecked()) {
        checkedElements.add(element);
      }
    }
    m_tableViewer.setCheckedElements(checkedElements.toArray(new ElementBean[checkedElements.size()]));
  }

  public List<ElementBean> getElements() {
    return m_elements;
  }

  public void setElements(List<ElementBean> elements) {
    m_elements = elements;
  }

  private class P_LabelProvider extends CellLabelProvider {

    @Override
    public void update(ViewerCell cell) {
      ElementBean element = (ElementBean) cell.getElement();
      cell.setText(element.getName());
      if (element.getImageDescriptor() != null) {
        cell.setImage(element.getImageDescriptor().createImage());
      }
      else {
        cell.setImage(null);
      }
      if (element.isMandatory()) {
        cell.setForeground(ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
      }
    }
  }
}
