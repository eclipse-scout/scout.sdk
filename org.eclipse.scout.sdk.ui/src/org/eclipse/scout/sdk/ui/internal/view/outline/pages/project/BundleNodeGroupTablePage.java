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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.operation.util.wellform.WellformClientBundleOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.OrganizeAllImportsAction;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.ScoutBundleNewAction;
import org.eclipse.scout.sdk.ui.action.dto.MultipleUpdateFormDataAction;
import org.eclipse.scout.sdk.ui.action.dto.TypeResolverPageDataAction;
import org.eclipse.scout.sdk.ui.action.export.ExportScoutProjectAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.SdkIcons;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerSettingsSupport;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerSettingsSupport.BundlePresentation;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.dto.formdata.ScoutBundlesUpdateFormDataOperation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>{@link BundleNodeGroupTablePage}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.9.0 11.02.2013
 */
public class BundleNodeGroupTablePage extends AbstractPage {

  private ScoutBundleNodeGroup m_group;

  public BundleNodeGroupTablePage(AbstractPage parentPage, ScoutBundleNodeGroup group) {
    m_group = group;
    setParent(parentPage);
    setName(group.getGroupName());

    ImageDescriptor icon = ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ScoutProject);
    if (group.isBinary()) {
      icon = ScoutSdkUi.getImageDescriptor(icon, SdkIcons.BinaryDecorator, IDecoration.BOTTOM_LEFT);
    }
    setImageDescriptor(icon);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.BUNDLE_NODE_GROUP_TABLE_PAGE;
  }

  @Override
  public int getOrder() {
    return 10000;
  }

  @Override
  public IScoutBundle getScoutBundle() {
    return m_group.getDefiningBundle().getScoutBundle();
  }

  @Override
  public boolean isInitiallyLoaded() {
    return true;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{OrganizeAllImportsAction.class, MultipleUpdateFormDataAction.class, TypeResolverPageDataAction.class,
        FormDataSqlBindingValidateAction.class, ExportScoutProjectAction.class, ScoutBundleNewAction.class, WellformAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof OrganizeAllImportsAction) {
      ((OrganizeAllImportsAction) menu).setScoutProject(getScoutBundle());
    }
    else if (menu instanceof MultipleUpdateFormDataAction) {
      ((MultipleUpdateFormDataAction) menu).setOperation(new ScoutBundlesUpdateFormDataOperation(getScoutBundle()));
    }
    else if (menu instanceof FormDataSqlBindingValidateAction) {
      ((FormDataSqlBindingValidateAction) menu).setTyperesolver(new ITypeResolver() {
        @Override
        public IType[] getTypes() {
          return resolveServices();
        }
      });
    }
    else if (menu instanceof ExportScoutProjectAction) {
      ((ExportScoutProjectAction) menu).setScoutProject(getScoutBundle());
    }
    else if (menu instanceof ScoutBundleNewAction) {
      ((ScoutBundleNewAction) menu).setScoutProject(getScoutBundle());
    }
    else if (menu instanceof TypeResolverPageDataAction) {
      ((TypeResolverPageDataAction) menu).init(new ITypeResolver() {
        @Override
        public IType[] getTypes() {
          IType iPageWithTable = TypeUtility.getType(RuntimeClasses.IPageWithTable);
          IPrimaryTypeTypeHierarchy pageWithTableHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPageWithTable);
          ArrayList<IScoutBundle> bundles = new ArrayList<IScoutBundle>();
          collectBundlesRec(m_group, bundles);
          return pageWithTableHierarchy.getAllSubtypes(iPageWithTable, ScoutTypeFilters.getTypesInScoutBundles(bundles.toArray(new IScoutBundle[bundles.size()])));
        }
      }, getScoutBundle());
    }
    else if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      ArrayList<IScoutBundle> bundles = new ArrayList<IScoutBundle>();
      collectBundlesRec(m_group, bundles);
      action.setOperation(new WellformClientBundleOperation(bundles.toArray(new IScoutBundle[bundles.size()])));
      action.setScoutBundle(getScoutBundle());
    }
  }

  private void collectBundlesRec(ScoutBundleNodeGroup group, List<IScoutBundle> collector) {
    for (ScoutBundleNode b : m_group.getChildBundles()) {
      collector.add(b.getScoutBundle());
    }
    for (ScoutBundleNodeGroup childGroup : group.getChildGroups()) {
      collectBundlesRec(childGroup, collector);
    }
  }

  protected IType[] resolveServices() {
    IType iService = TypeUtility.getType(RuntimeClasses.IService);
    IPrimaryTypeTypeHierarchy serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iService);
    IScoutBundle[] serverBundles = getScoutBundle().getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER), true);

    IType[] services = serviceHierarchy.getAllSubtypes(iService, ScoutTypeFilters.getTypesInScoutBundles(serverBundles));
    return services;
  }

  @Override
  public void loadChildrenImpl() {
    for (ScoutBundleNode b : m_group.getChildBundles()) {
      b.createBundlePage(this);
    }

    if (BundlePresentation.Grouped.equals(ScoutExplorerSettingsSupport.get().getBundlePresentation())) {
      ScoutBundleNodeGroup[] childGroups = m_group.getChildGroups().toArray(new ScoutBundleNodeGroup[m_group.getChildGroups().size()]);
      Arrays.sort(childGroups);
      for (ScoutBundleNodeGroup childGroup : childGroups) {
        new BundleNodeGroupTablePage(this, childGroup);
      }
    }
  }
}
