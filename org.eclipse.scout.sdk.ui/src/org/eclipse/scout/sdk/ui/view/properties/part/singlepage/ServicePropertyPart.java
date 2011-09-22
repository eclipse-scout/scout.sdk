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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinkGroup;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.TypeOpenLink;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.LinksPresenter;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>ServicePropertyPart</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 22.07.2010
 */
public class ServicePropertyPart extends JdtTypePropertyPart {
  private static final String SECTION_ID_LINKS = "section.links";

  final IType basicPermission = ScoutSdk.getType(RuntimeClasses.BasicPermission);
  final IType iForm = ScoutSdk.getType(RuntimeClasses.IForm);

  @Override
  protected void createSections() {
    // link area
    ISection linkSection = createSection(SECTION_ID_LINKS, Texts.get("Links"));
    fillLinkSection(linkSection.getSectionClient());
    super.createSections();
  }

  @Override
  public AbstractServiceNodePage getPage() {
    return (AbstractServiceNodePage) super.getPage();
  }

  protected void fillLinkSection(Composite parent) {
    String entityName = null;
    // model
    LinksPresenterModel model = new LinksPresenterModel();
    if (getPage().getInterfaceType() != null) {
      model.addGlobalLink(new TypeOpenLink(getPage().getInterfaceType()));
    }
    if (getPage().getType() != null) {
      model.addGlobalLink(new TypeOpenLink(getPage().getType()));
      entityName = findEntityName(getPage().getType().getElementName());
    }
    if (!StringUtility.isNullOrEmpty(entityName)) {
      // form
      if (TypeUtility.exists(iForm)) /* can be null on a server-only-project (bugzilla ticket 325428) */{
        String formRegex = entityName + ScoutIdeProperties.SUFFIX_FORM;
        ITypeFilter formFilter = TypeFilters.getMultiTypeFilter(
            TypeFilters.getRegexSimpleNameFilter(formRegex),
            TypeFilters.getClassFilter(),
            TypeFilters.getInScoutProject(getPage().getScoutResource().getScoutProject())
            );
        LinkGroup formGroup = model.getOrCreateGroup(Texts.get("Form"), 10);
        for (IType candidate : ScoutSdk.getPrimaryTypeHierarchy(iForm).getAllSubtypes(iForm, formFilter, TypeComparators.getTypeNameComparator())) {
          formGroup.addLink(new TypeOpenLink(candidate));
        }
      }
      // permissions
      String permissionRegex = "(Create|Read|Update)" + entityName + ScoutIdeProperties.SUFFIX_PERMISSION;
      ITypeFilter filter = TypeFilters.getMultiTypeFilter(
          TypeFilters.getRegexSimpleNameFilter(permissionRegex),
          TypeFilters.getClassFilter(),
          TypeFilters.getInScoutProject(getPage().getScoutResource().getScoutProject())
          );
      LinkGroup permissionGroup = model.getOrCreateGroup(Texts.get("PermissionTablePage"), 20);
      for (IType candidate : ScoutSdk.getPrimaryTypeHierarchy(basicPermission).getAllSubtypes(basicPermission, filter, TypeComparators.getTypeNameComparator())) {
        permissionGroup.addLink(new TypeOpenLink(candidate));
      }

    }

    // ui
    LinksPresenter presenter = new LinksPresenter(getFormToolkit(), parent, model);
    GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    layoutData.widthHint = 200;
    presenter.getContainer().setLayoutData(layoutData);
  }

  private String findEntityName(String serviceName) {
    if (StringUtility.isNullOrEmpty(serviceName)) {
      return serviceName;
    }
    if (serviceName.endsWith(ScoutIdeProperties.SUFFIX_PROCESS_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + ScoutIdeProperties.SUFFIX_PROCESS_SERVICE + "$", "$1");
    }
    if (serviceName.endsWith(ScoutIdeProperties.SUFFIX_OUTLINE_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + ScoutIdeProperties.SUFFIX_OUTLINE_SERVICE + "$", "$1");
    }
    if (serviceName.endsWith(ScoutIdeProperties.SUFFIX_CUSTOM_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + ScoutIdeProperties.SUFFIX_CUSTOM_SERVICE + "$", "$1");
    }
    if (serviceName.endsWith(ScoutIdeProperties.SUFFIX_CALENDAR_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + ScoutIdeProperties.SUFFIX_CALENDAR_SERVICE + "$", "$1");
    }
    if (serviceName.endsWith(ScoutIdeProperties.SUFFIX_SMTP_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + ScoutIdeProperties.SUFFIX_SMTP_SERVICE + "$", "$1");
    }
    if (serviceName.endsWith(ScoutIdeProperties.SUFFIX_SQL_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + ScoutIdeProperties.SUFFIX_SQL_SERVICE + "$", "$1");
    }
    if (serviceName.endsWith(ScoutIdeProperties.SUFFIX_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + ScoutIdeProperties.SUFFIX_SERVICE + "$", "$1");
    }
    return serviceName;
  }
}
