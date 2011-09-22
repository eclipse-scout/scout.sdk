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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>UnknownPropertyViewPart</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 21.07.2010
 */
public class TestFolderViewPart extends AbstractSinglePageSectionBasedViewPart {

  @Override
  protected Control createHead(Composite parent) {
    Composite headArea = getFormToolkit().createComposite(parent);
    String titleText = Texts.get("NoPropertiesAvailable");
    if (getPage() != null) {
      titleText = Texts.get("NoPropertiesAvailableForX ", getPage().getName());
    }
    Label title = getFormToolkit().createLabel(headArea, titleText, SWT.WRAP | SWT.READ_ONLY);
    // layout
    headArea.setLayout(new GridLayout(1, true));
    GridData titleData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
    titleData.widthHint = 100;
    title.setLayoutData(titleData);
    return headArea;
  }

  public static final String SECTION_ID_TEST = "test";
  public static final String SECTION_ID_ACTION = "action";
  public static final String SECTION_ID_FILTER = "filter";

  @Override
  protected void createSections() {
    ISection actionSection = createSection(SECTION_ID_ACTION, Texts.get("SomeActions"));
    getSection(SECTION_ID_ACTION).setExpanded(false);
    Button testSectionButton = getFormToolkit().createButton(actionSection.getSectionClient(), Texts.get("ShowTestSection"), SWT.CHECK);
    testSectionButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {

        boolean selected = ((Button) e.widget).getSelection();
        getSection(SECTION_ID_TEST).setVisible(selected);

      }
    });
    ISection testSection = createSection(SECTION_ID_TEST, Texts.get("ATestSection"), "", false, SECTION_ID_ACTION);
    getFormToolkit().createLabel(testSection.getSectionClient(), "blubber");
    if (getPage().isFolder()) {
      ISection filterSection = createSection(SECTION_ID_FILTER, Texts.get("Filter"));
      PageFilterPresenter filterPresenter = new PageFilterPresenter(getFormToolkit(), filterSection.getSectionClient(), getPage());
      GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      layoutData.widthHint = 200;
      filterPresenter.getContainer().setLayoutData(layoutData);
    }
  }
}
