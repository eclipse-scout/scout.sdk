/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.view.properties.part.singlepage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageWithTableNodePage;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.TypeOpenLink;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.LinksPresenter;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>{@link PageWithTablePropertyPart}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 09.10.2013
 */
public class PageWithTablePropertyPart extends JdtTypePropertyPart {

  private static final String SECTION_ID_LINKS = "section.links";

  @Override
  protected void createSections() {
    // link area
    ISection linkSection = createSection(SECTION_ID_LINKS, Texts.get("Links"));
    fillLinkSection(linkSection.getSectionClient());
    linkSection.setExpanded(wasSectionExpanded(SECTION_ID_LINKS, true));
    super.createSections();
  }

  @Override
  public PageWithTableNodePage getPage() {
    return (PageWithTableNodePage) super.getPage();
  }

  protected void fillLinkSection(Composite parent) {
    final LinksPresenter presenter = new LinksPresenter(getFormToolkit(), parent);
    GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    layoutData.widthHint = 200;
    presenter.getContainer().setLayoutData(layoutData);

    // load lazy
    final ProgressIndicator indicator = new ProgressIndicator(presenter.getContainer(), SWT.INDETERMINATE | SWT.SMOOTH);
    indicator.beginAnimatedTask();
    GridData indicatorData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    indicatorData.horizontalSpan = 2;
    indicatorData.heightHint = 5;
    indicator.setLayoutData(indicatorData);

    Job j = new Job("load links...") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        if (presenter != null && !presenter.isDisposed()) {
          LinksPresenterModel model = new LinksPresenterModel();
          IType pageWithTable = getPage().getType();

          // page
          if (TypeUtility.exists(pageWithTable)) {
            model.addGlobalLink(new TypeOpenLink(pageWithTable));

            // super pages
            try {
              ITypeHierarchy pageSuperTypeHierarchy = ScoutTypeUtility.getSupertypeHierarchy(pageWithTable);
              IType pageSuperClass = pageSuperTypeHierarchy.getSuperclass(pageWithTable);

              if (TypeUtility.exists(pageSuperClass)) {
                int flags = pageSuperClass.getFlags();
                if (!Flags.isAbstract(flags) && !Flags.isInterface(flags)) {
                  TypeOpenLink lnk = new TypeOpenLink(pageSuperClass);
                  lnk.setName(pageSuperClass.getElementName());
                  model.addGlobalLink(lnk);
                }
              }
            }
            catch (JavaModelException e) {
              ScoutSdkUi.logError(e);
            }

            // page data
            IType pageDataType = null;
            try {
              pageDataType = ScoutTypeUtility.findDtoForPage(getPage().getType());
              if (TypeUtility.exists(pageDataType)) {
                model.addGlobalLink(new TypeOpenLink(pageDataType));
              }
            }
            catch (JavaModelException e) {
              ScoutSdkUi.logError(e);
            }
          }

          final LinksPresenterModel finalModel = model;
          presenter.getContainer().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
              if (presenter != null && !presenter.isDisposed()) {
                indicator.dispose();
                presenter.setLinksProperty(finalModel);
                getForm().layout(true, true);
                getForm().updateToolBar();
                getForm().reflow(true);
              }
            }
          });
        }
        return Status.OK_STATUS;
      }
    };
    j.setSystem(true);
    j.schedule();
  }
}
