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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinkGroup;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.TypeOpenLink;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.LinksPresenter;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
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

  final IType basicPermission = TypeUtility.getType(RuntimeClasses.BasicPermission);
  final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);

  @Override
  protected void createSections() {
    // link area
    ISection linkSection = createSection(SECTION_ID_LINKS, Texts.get("Links"));
    fillLinkSection(linkSection.getSectionClient());
    linkSection.setExpanded(wasSectionExpanded(SECTION_ID_LINKS, true));
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
        String formRegex = entityName + SdkProperties.SUFFIX_FORM;
        ITypeFilter formFilter = TypeFilters.getMultiTypeFilter(
            TypeFilters.getRegexSimpleNameFilter(formRegex),
            TypeFilters.getClassFilter(),
            ScoutTypeFilters.getInScoutProject(getPage().getScoutResource().getScoutProject())
            );
        LinkGroup formGroup = model.getOrCreateGroup(Texts.get("Form"), 10);
        for (IType candidate : TypeUtility.getPrimaryTypeHierarchy(iForm).getAllSubtypes(iForm, formFilter, TypeComparators.getTypeNameComparator())) {
          formGroup.addLink(new TypeOpenLink(candidate));
        }
      }
      // permissions
      String permissionRegex = "(Create|Read|Update)" + entityName + SdkProperties.SUFFIX_PERMISSION;
      ITypeFilter filter = TypeFilters.getMultiTypeFilter(
          TypeFilters.getRegexSimpleNameFilter(permissionRegex),
          TypeFilters.getClassFilter(),
          ScoutTypeFilters.getInScoutProject(getPage().getScoutResource().getScoutProject())
          );
      LinkGroup permissionGroup = model.getOrCreateGroup(Texts.get("PermissionTablePage"), 20);
      for (IType candidate : TypeUtility.getPrimaryTypeHierarchy(basicPermission).getAllSubtypes(basicPermission, filter, TypeComparators.getTypeNameComparator())) {
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
    if (serviceName.endsWith(SdkProperties.SUFFIX_PROCESS_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + SdkProperties.SUFFIX_PROCESS_SERVICE + "$", "$1");
    }
    if (serviceName.endsWith(SdkProperties.SUFFIX_OUTLINE_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + SdkProperties.SUFFIX_OUTLINE_SERVICE + "$", "$1");
    }
    if (serviceName.endsWith(SdkProperties.SUFFIX_CUSTOM_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + SdkProperties.SUFFIX_CUSTOM_SERVICE + "$", "$1");
    }
    if (serviceName.endsWith(SdkProperties.SUFFIX_CALENDAR_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + SdkProperties.SUFFIX_CALENDAR_SERVICE + "$", "$1");
    }
    if (serviceName.endsWith(SdkProperties.SUFFIX_SMTP_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + SdkProperties.SUFFIX_SMTP_SERVICE + "$", "$1");
    }
    if (serviceName.endsWith(SdkProperties.SUFFIX_SQL_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + SdkProperties.SUFFIX_SQL_SERVICE + "$", "$1");
    }
    if (serviceName.endsWith(SdkProperties.SUFFIX_SERVICE)) {
      return serviceName.replaceAll("^(.*)" + SdkProperties.SUFFIX_SERVICE + "$", "$1");
    }
    return serviceName;
  }
}
