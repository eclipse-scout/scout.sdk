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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.FormNodePage;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinkGroup;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.TypeOpenLink;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.LinksPresenter;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>ServicePropertyPart</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 22.07.2010
 */
public class FormPropertyPart extends JdtTypePropertyPart {
  private static final String SECTION_ID_LINKS = "section.links";

  final IType abstractFormData = ScoutSdk.getType(RuntimeClasses.AbstractFormData);
  final IType iService = ScoutSdk.getType(RuntimeClasses.IService);
  final IType basicPermission = ScoutSdk.getType(RuntimeClasses.BasicPermission);

  @Override
  protected void createSections() {
    // link area
    ISection linkSection = createSection(SECTION_ID_LINKS, "Links");
    fillLinkSection(linkSection.getSectionClient());
    super.createSections();
  }

  @Override
  public FormNodePage getPage() {
    return (FormNodePage) super.getPage();
  }

  protected void fillLinkSection(Composite parent) {
    // ui

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
        String entityName = null;
        LinksPresenterModel model = new LinksPresenterModel();
        if (getPage().getType() != null) {
          model.addGlobalLink(new TypeOpenLink(getPage().getType()));
          entityName = findEntityName(getPage().getType().getElementName());
        }
        if (!StringUtility.isNullOrEmpty(entityName)) {
          // form data
          IScoutBundle clientBundle = getPage().getScoutResource();
          IScoutProject scoutProject = clientBundle.getScoutProject();
          IScoutBundle sharedBundle = scoutProject.getSharedBundle();
          while (sharedBundle == null) {
            scoutProject = scoutProject.getParentProject();
            if (scoutProject == null) {
              break;
            }
            sharedBundle = scoutProject.getSharedBundle();
          }
          if (sharedBundle != null) {
            String formDataName = sharedBundle.getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_PROCESS) + "." + entityName + ScoutIdeProperties.SUFFIX_FORM_DATA;
            if (ScoutSdk.existsType(formDataName)) {
              model.addGlobalLink(new TypeOpenLink(ScoutSdk.getType(formDataName)));
            }
          }
          // service
          String formRegex = "(I)?" + entityName + ScoutIdeProperties.SUFFIX_PROCESS_SERVICE;
          ITypeFilter formFilter = TypeFilters.getMultiTypeFilter(
              TypeFilters.getRegexSimpleNameFilter(formRegex),
              TypeFilters.getInScoutProject(clientBundle.getScoutProject())
              );
          LinkGroup serviceGroup = model.getOrCreateGroup("Service", 10);
          for (IType candidate : ScoutSdk.getPrimaryTypeHierarchy(iService).getAllSubtypes(iService, formFilter, TypeComparators.getTypeNameComparator())) {
            serviceGroup.addLink(new TypeOpenLink(candidate));
          }
          // permissions
          String permissionRegex = "(Create|Read|Update)" + entityName + ScoutIdeProperties.SUFFIX_PERMISSION;
          ITypeFilter filter = TypeFilters.getMultiTypeFilter(
              TypeFilters.getRegexSimpleNameFilter(permissionRegex),
              TypeFilters.getClassFilter(),
              TypeFilters.getInScoutProject(clientBundle.getScoutProject())
              );
          LinkGroup permissionGroup = model.getOrCreateGroup("Permissions", 20);
          for (IType candidate : ScoutSdk.getPrimaryTypeHierarchy(basicPermission).getAllSubtypes(basicPermission, filter, TypeComparators.getTypeNameComparator())) {
            permissionGroup.addLink(new TypeOpenLink(candidate));
          }
        }
        if (presenter != null && !presenter.isDisposed()) {
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

  private String findEntityName(String serviceName) {
    if (StringUtility.isNullOrEmpty(serviceName)) {
      return serviceName;
    }
    if (serviceName.endsWith(ScoutIdeProperties.SUFFIX_FORM)) {
      return serviceName.replaceAll("^(.*)" + ScoutIdeProperties.SUFFIX_FORM + "$", "$1");
    }
    return serviceName;
  }
}
