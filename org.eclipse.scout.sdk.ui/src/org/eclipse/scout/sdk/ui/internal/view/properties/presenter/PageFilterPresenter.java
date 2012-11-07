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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPageFilter;
import org.eclipse.scout.sdk.ui.view.outline.pages.PageFilter;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * <h3>PageFilterPresenter</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 23.07.2010
 */
public class PageFilterPresenter extends AbstractPresenter {

  private Text m_filterExpressionField;
  private Button m_regexButton;
  private ImageHyperlink m_resetFilter;
  private final IPage m_page;

  /**
   * @param toolkit
   * @param parent
   */
  public PageFilterPresenter(PropertyViewFormToolkit toolkit, Composite parent, IPage page) {
    super(toolkit, parent);
    m_page = page;

    createContent(getContainer());
  }

  private void createContent(Composite container) {
    m_filterExpressionField = getToolkit().createText(container, "", SWT.BORDER);
    m_resetFilter = getToolkit().createImageHyperlink(container, SWT.PUSH);
    m_resetFilter.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolDelete));
    m_regexButton = getToolkit().createButton(container, Texts.get("Regex"), SWT.CHECK);
    // listeners
    m_filterExpressionField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        GridData gd = (GridData) m_resetFilter.getLayoutData();
        boolean emptyText = StringUtility.isNullOrEmpty(m_filterExpressionField.getText());
        m_resetFilter.setVisible(!emptyText);
        gd.exclude = emptyText;
        updateFilter();
        getContainer().layout(true);
      }
    });
    m_resetFilter.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        m_filterExpressionField.setText("");
        updateFilter();
      }
    });
    m_regexButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updateFilter();
      }
    });

    // layout
    GridLayout layout = new GridLayout(3, false);
    layout.marginHeight = 0;
    layout.horizontalSpacing = 3;
    layout.verticalSpacing = 3;
    layout.marginWidth = 0;
    container.setLayout(layout);

    GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL);
    m_filterExpressionField.setLayoutData(data);
    data = new GridData(GridData.FILL_VERTICAL);
    data.exclude = true;
    m_resetFilter.setLayoutData(data);
    data = new GridData(GridData.FILL_VERTICAL);
    m_regexButton.setLayoutData(data);

    //init
    if (getPage() != null && getPage().getOutlineView() != null) {
      IPageFilter pageFilter = getPage().getOutlineView().getPageFilter(getPage());
      if (pageFilter != null && pageFilter instanceof PageFilter) {
        m_filterExpressionField.setText(pageFilter.getFilterExpression());
        m_regexButton.setSelection(((PageFilter) pageFilter).isRegExFilter());
      }
    }
  }

  private void updateFilter() {
    PageFilter filter = null;
    if (!StringUtility.isNullOrEmpty(m_filterExpressionField.getText())) {
      filter = new PageFilter();
      filter.setFilterExpression(m_filterExpressionField.getText());
      filter.setRegExFilter(m_regexButton.getSelection());
    }
    getPage().getOutlineView().addPageFilter(getPage(), filter);
  }

  /**
   * @return the page
   */
  public IPage getPage() {
    return m_page;
  }

}
