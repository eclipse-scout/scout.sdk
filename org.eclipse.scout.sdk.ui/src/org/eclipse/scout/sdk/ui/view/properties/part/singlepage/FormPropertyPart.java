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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.FormNodePage;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinkGroup;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.TypeOpenLink;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.LinksPresenter;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>ServicePropertyPart</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 22.07.2010
 */
public class FormPropertyPart extends JdtTypePropertyPart {
  private static final String SECTION_ID_LINKS = "section.links";

  private final IType iService = TypeUtility.getType(IRuntimeClasses.IService);
  private final IType permission = TypeUtility.getType(IRuntimeClasses.Permission);

  @Override
  protected void createSections() {
    // link area
    ISection linkSection = createSection(SECTION_ID_LINKS, Texts.get("Links"));
    fillLinkSection(linkSection.getSectionClient());
    linkSection.setExpanded(wasSectionExpanded(SECTION_ID_LINKS, true));
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
        IType form = getPage().getType();

        if (TypeUtility.exists(form)) {
          model.addGlobalLink(new TypeOpenLink(form));
          entityName = findEntityName(form.getElementName());
        }

        if (!StringUtility.isNullOrEmpty(entityName)) {
          // super form class
          try {
            ITypeHierarchy formSuperTypeHierarchy = ScoutTypeUtility.getSupertypeHierarchy(form);
            IType formSuperClass = formSuperTypeHierarchy.getSuperclass(form);

            if (TypeUtility.exists(formSuperClass)) {
              int flags = formSuperClass.getFlags();
              if (!Flags.isAbstract(flags) && !Flags.isInterface(flags)) {
                TypeOpenLink lnk = new TypeOpenLink(formSuperClass);
                lnk.setName(formSuperClass.getElementName());
                model.addGlobalLink(lnk);
              }
            }
          }
          catch (JavaModelException e) {
            ScoutSdkUi.logError(e);
          }

          // form data
          IType formDataType = null;
          try {
            formDataType = ScoutTypeUtility.findDtoForForm(form);
          }
          catch (JavaModelException e) {
            ScoutSdkUi.logError(e);
          }
          if (TypeUtility.exists(formDataType)) {
            model.addGlobalLink(new TypeOpenLink(formDataType));
          }

          IScoutBundle client = getPage().getScoutBundle();
          Set<? extends IScoutBundle> shareds = client.getParentBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), false);
          HashSet<IScoutBundle> serversAndSharedsSet = new HashSet<IScoutBundle>();
          for (IScoutBundle shared : shareds) {
            serversAndSharedsSet.add(shared);
            for (IScoutBundle server : shared.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER), false)) {
              serversAndSharedsSet.add(server);
            }
          }

          // service
          String formRegex = "(I)?" + entityName + "(Process)?" + SdkProperties.SUFFIX_SERVICE;
          ITypeFilter formFilter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getRegexSimpleNameFilter(formRegex), ScoutTypeFilters.getInScoutBundles(serversAndSharedsSet));
          LinkGroup serviceGroup = model.getOrCreateGroup(Texts.get("Service"), 10);
          for (IType candidate : TypeUtility.getPrimaryTypeHierarchy(iService).getAllSubtypes(iService, formFilter, TypeComparators.getTypeNameComparator())) {
            serviceGroup.addLink(new TypeOpenLink(candidate));
          }

          // permissions
          String permissionRegex = "(Create|Read|Update)" + entityName + SdkProperties.SUFFIX_PERMISSION;
          ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(
              TypeFilters.getRegexSimpleNameFilter(permissionRegex),
              TypeFilters.getClassFilter(),
              ScoutTypeFilters.getInScoutBundles(shareds)
              );
          LinkGroup permissionGroup = model.getOrCreateGroup(Texts.get("PermissionTablePage"), 20);
          for (IType candidate : TypeUtility.getPrimaryTypeHierarchy(permission).getAllSubtypes(permission, filter, TypeComparators.getTypeNameComparator())) {
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
    if (serviceName.endsWith(SdkProperties.SUFFIX_FORM)) {
      return serviceName.replaceAll("^(.*)" + SdkProperties.SUFFIX_FORM + "$", "$1");
    }
    return serviceName;
  }
}
