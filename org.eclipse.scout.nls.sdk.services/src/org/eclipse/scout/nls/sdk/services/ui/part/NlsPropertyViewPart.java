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
package org.eclipse.scout.nls.sdk.services.ui.part;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.internal.ui.editor.NlsEditor;
import org.eclipse.scout.nls.sdk.internal.ui.editor.NlsTypeEditorInput;
import org.eclipse.scout.nls.sdk.services.ui.page.TextServiceNodePage;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.TypeOpenLink;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.LinksPresenter;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.JdtTypePropertyPart;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>UnknownPropertyViewPart</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 21.07.2010
 */
public class NlsPropertyViewPart extends JdtTypePropertyPart {
  private static final String SECTION_ID_LINKS = "section.links";

  @Override
  public TextServiceNodePage getPage() {
    return (TextServiceNodePage) super.getPage();
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
    IScoutBundle bundle = getPage().getScoutBundle();
    if (bundle != null) {
      final IType serviceType = getPage().getType();
      if (TypeUtility.exists(serviceType)) {
        TypeOpenLink link = new TypeOpenLink(serviceType) {
          @Override
          public void execute() {
            IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            try {
              activePage.openEditor(new NlsTypeEditorInput(serviceType), NlsEditor.EDITOR_ID, true, IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
            }
            catch (PartInitException e) {
              ScoutSdkUi.logError("Unable to open NLS editor for type '" + serviceType.getFullyQualifiedName() + "'.", e);
            }
          }
        };
        link.setName(Texts.get("OpenNlsEditor"));
        model.addGlobalLink(link);
      }
    }
    // ui
    LinksPresenter presenter = new LinksPresenter(getFormToolkit(), parent, model);
    GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    layoutData.widthHint = 200;
    presenter.getContainer().setLayoutData(layoutData);
  }
}
