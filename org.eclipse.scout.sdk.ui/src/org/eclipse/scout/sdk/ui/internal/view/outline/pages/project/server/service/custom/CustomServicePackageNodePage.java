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
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.CustomServiceNewAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.type.PackageContentChangedListener;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.IPrimaryTypeTypeHierarchy;

public class CustomServicePackageNodePage extends AbstractPage {

  final IType iService = ScoutSdk.getType(RuntimeClasses.IService);

  private PackageContentChangedListener m_packageContentListener;
  private IPackageFragment m_package;

  private IPrimaryTypeTypeHierarchy m_serviceHierarchy;

  public CustomServicePackageNodePage(AbstractPage parent, IPackageFragment packageFrament) {
    m_package = packageFrament;
    setParent(parent);
    m_packageContentListener = new PackageContentChangedListener(this, m_package);
    JavaCore.addElementChangedListener(m_packageContentListener);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Package));
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    JavaCore.removeElementChangedListener(m_packageContentListener);
    m_serviceHierarchy = null;
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CUSTOM_SERVICE_PACKAGE_NODE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  /**
   * server bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  protected void loadChildrenImpl() {
    for (IType service : resolveServices()) {
      IType serviceInterface = null;
      IType[] interfaces = m_serviceHierarchy.getSuperInterfaces(service, TypeFilters.getElementNameFilter("I" + service.getElementName()));
      if (interfaces.length > 0) {
        serviceInterface = interfaces[0];
      }
      new CustomServiceNodePage(this, service, serviceInterface);
    }
  }

  protected IType[] resolveServices() {
    if (m_serviceHierarchy == null) {
      m_serviceHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iService);
    }
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(
        TypeFilters.getClassFilter(),
        TypeFilters.getSubtypeFilter(iService, m_serviceHierarchy)
        );
    IType[] services = TypeUtility.getTypesInPackage(m_package, filter, TypeComparators.getTypeNameComparator());
    return services;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{FormDataSqlBindingValidateAction.class, CustomServiceNewAction.class};
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
    else if (menu instanceof CustomServiceNewAction) {
      ((CustomServiceNewAction) menu).init(getScoutResource(), m_package);
    }
  }
}
