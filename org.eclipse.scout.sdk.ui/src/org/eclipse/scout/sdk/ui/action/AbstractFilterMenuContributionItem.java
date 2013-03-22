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
package org.eclipse.scout.sdk.ui.action;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * <h3>{@link AbstractFilterMenuContributionItem}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 20.03.2013
 */
public abstract class AbstractFilterMenuContributionItem extends ContributionItem {

  private String m_label;
  private MenuItem m_item;
  private boolean m_selected;

  public AbstractFilterMenuContributionItem(String label, boolean selected) {
    m_label = label;
    m_selected = selected;
  }

  @Override
  public void fill(Menu menu, int index) {
    m_item = new MenuItem(menu, SWT.CHECK, index);
    m_item.setText(m_label);
    m_item.setSelection(m_selected);
    m_item.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        selectionChangedInternal();
      }
    });
  }

  private void selectionChangedInternal() {
    m_selected = !m_selected;
    m_item.setSelection(m_selected);
    run(m_selected);
  }

  protected abstract void run(boolean selected);

  public boolean getSelection() {
    return m_selected;
  }

  @Override
  public boolean isDynamic() {
    return true;
  }
}
