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

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.PageFilterPresenter;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>UnknownPropertyViewPart</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 21.07.2010
 */
public class UnknownSinglePagePropertyViewPart extends AbstractSinglePageSectionBasedViewPart {
  private static final String SECTION_ID_FILTER = "section.filter";

  @Override
  protected Control createHead(Composite parent) {
    Composite headArea = getFormToolkit().createComposite(parent);
    String titleText = Texts.get("NoPropertiesAvailable");
    if (getPage() != null) {
      titleText = Texts.get("NoPropertiesAvailableForX", getPage().getName());
    }
    Label title = getFormToolkit().createLabel(headArea, titleText, SWT.WRAP | SWT.READ_ONLY);
    // layout
    headArea.setLayout(new GridLayout(1, true));
    GridData titleData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
    titleData.widthHint = 100;
    title.setLayoutData(titleData);
    return headArea;
  }

  @Override
  protected void createSections() {
    if (getPage() != null && getPage().isFolder()) {
      ISection filterSection = createSection(SECTION_ID_FILTER, Texts.get("Filter"));
      PageFilterPresenter filterPresenter = new PageFilterPresenter(getFormToolkit(), filterSection.getSectionClient(), getPage());
      GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      layoutData.widthHint = 200;
      filterPresenter.getContainer().setLayoutData(layoutData);
    }
    super.createSections();
  }
}
