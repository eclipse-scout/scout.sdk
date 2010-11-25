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
package org.eclipse.scout.sdk.ui.view.properties.part.singlepage;

import org.eclipse.scout.sdk.ui.extensions.view.property.ISinglePropertyViewPart;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.properties.part.AbstractSectionBasedPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>AbstractSinglePageSectionBasedViewPart</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 23.07.2010
 */
public abstract class AbstractSinglePageSectionBasedViewPart extends AbstractSectionBasedPart implements ISinglePropertyViewPart {

  private IPage m_page;

  @Override
  public IPage getPage() {
    return m_page;
  }

  @Override
  public void setPage(IPage page) {
    m_page = page;
  }

  @Override
  protected Control createHead(Composite parent) {
    Composite headArea = getFormToolkit().createComposite(parent);
    Label title = getFormToolkit().createLabel(headArea, getPage().getName(), SWT.WRAP | SWT.READ_ONLY);
    // layout
    headArea.setLayout(new GridLayout(1, true));
    GridData titleData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
    titleData.widthHint = 100;
    title.setLayoutData(titleData);
    return headArea;
  }

}
