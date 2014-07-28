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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.ProductLaunchPresenter;
import org.eclipse.scout.sdk.util.resources.ResourceFilters;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>ServicePropertyPart</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 22.07.2010
 */
public class ProductLaunchPropertyPart extends AbstractSinglePageSectionBasedViewPart {
  private static final String SECTION_ID_LINKS = "section.links";

  public ProductLaunchPropertyPart() {
  }

  @Override
  protected void createSections() {
    // link area
    ISection linkSection = createSection(SECTION_ID_LINKS, Texts.get("Links"));
    fillLinkSection(linkSection.getSectionClient());
    super.createSections();
  }

  protected void fillLinkSection(Composite parent) {
    IScoutBundle bundle = getPage().getScoutBundle();
    if (bundle != null && !bundle.isBinary()) {
      try {
        List<IResource> productFiles = ResourceUtility.getAllResources(ResourceFilters.getProductFileByContentFilter(false, bundle.getSymbolicName()));
        for (IResource productFile : productFiles) {
          ProductLaunchPresenter p = new ProductLaunchPresenter(getFormToolkit(), parent, (IFile) productFile);
          GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
          layoutData.widthHint = 200;
          p.getContainer().setLayoutData(layoutData);
        }
      }
      catch (CoreException e) {
        ScoutSdkUi.logError("Unable to find product files that contain the bundle '" + bundle.getSymbolicName() + "'.", e);
      }
    }
  }
}
