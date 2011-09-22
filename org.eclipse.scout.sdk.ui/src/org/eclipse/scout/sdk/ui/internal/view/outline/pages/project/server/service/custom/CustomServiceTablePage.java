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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.custom;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.CustomServiceNewPackageAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.type.PackageContentChangedListener;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.IPrimaryTypeTypeHierarchy;

public class CustomServiceTablePage extends AbstractPage {
  final IType iService = ScoutSdk.getType(RuntimeClasses.IService);
  private PackageContentChangedListener m_changedListener;
  private IPackageFragment m_servicePackage;

  public CustomServiceTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("CustomServices"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Services));

  }

  @Override
  public void unloadPage() {
    if (m_changedListener != null) {
      JavaCore.removeElementChangedListener(m_changedListener);
      m_changedListener = null;
    }
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CUSTOM_SERVICE_TABLE_PAGE;
  }

  /**
   * server bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  protected void loadChildrenImpl() {
    m_servicePackage = getScoutResource().getPackageFragment(getScoutResource().getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_CUSTOM));
    if (m_changedListener == null) {
      m_changedListener = new PackageContentChangedListener(this, m_servicePackage);
      JavaCore.addElementChangedListener(m_changedListener);
    }
    for (IPackageFragment pFrag : TypeUtility.getSubPackages(m_servicePackage)) {
      String appendix = pFrag.getElementName().replaceFirst(m_servicePackage.getElementName() + ".", "");
      CustomServicePackageNodePage node = new CustomServicePackageNodePage(this, pFrag);
      node.setName(appendix);

    }

  }

  protected IType[] resolveServices() {
    IPrimaryTypeTypeHierarchy serviceHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iService);
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(
        TypeFilters.getClassFilter(),
        TypeFilters.getSubtypeFilter(iService, serviceHierarchy)
        );
    IType[] services = TypeUtility.getTypesInPackage(m_servicePackage, filter, null, true);
    return services;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{FormDataSqlBindingValidateAction.class, CustomServiceNewPackageAction.class};
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    if (menu instanceof FormDataSqlBindingValidateAction) {
      ((FormDataSqlBindingValidateAction) menu).setTyperesolver(new ITypeResolver() {
        @Override
        public IType[] getTypes() {
          return resolveServices();
        }
      });
    }
    else if (menu instanceof CustomServiceNewPackageAction) {
      ((CustomServiceNewPackageAction) menu).setScoutBundle(getScoutResource());
    }
  }
}
