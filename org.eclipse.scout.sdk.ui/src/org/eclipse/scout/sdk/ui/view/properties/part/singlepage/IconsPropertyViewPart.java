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

import org.eclipse.core.resources.IFile;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.view.property.ISinglePropertyViewPart;
import org.eclipse.scout.sdk.ui.internal.view.icons.IconsEditor;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.IconNodePage;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.FileOpenLink;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.LinksPresenter;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.properties.part.AbstractSectionBasedPart;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>UnknownPropertyViewPart</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 21.07.2010
 */
public class IconsPropertyViewPart extends AbstractSectionBasedPart implements ISinglePropertyViewPart {
  private static final String SECTION_ID_LINKS = "section.links";
  private IconNodePage m_page;

  @Override
  public void setPage(IPage page) {
    if (!(page instanceof IconNodePage)) {
      throw new IllegalArgumentException("page must extend '" + IconNodePage.class.getName() + "'");
    }
    m_page = (IconNodePage) page;
  }

  @Override
  public IconNodePage getPage() {
    return m_page;
  }

  @Override
  protected void createSections() {
    // link area
    ISection linkSection = createSection(SECTION_ID_LINKS, Texts.get("Links"));
    fillLinkSection(linkSection.getSectionClient());
    super.createSections();
  }

  protected void fillLinkSection(Composite parent) {
    // model
    LinksPresenterModel model = new LinksPresenterModel();
    IFile nlsFile = (IFile) getPage().getIconsType().getResource();
    if (nlsFile != null && nlsFile.exists()) {
      FileOpenLink link = new FileOpenLink(nlsFile, 10, IconsEditor.ID);
      link.setName(Texts.get("OpenIconsEditor"));
      model.addGlobalLink(link);
    }
    // ui
    LinksPresenter presenter = new LinksPresenter(getFormToolkit(), parent, model);
    GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    layoutData.widthHint = 200;
    presenter.getContainer().setLayoutData(layoutData);
  }
}
