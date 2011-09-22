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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.outline;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.OutlineServiceNewAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.type.PackageContentChangedListener;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

public class OutlineServiceTablePage extends AbstractPage {

  final IType iService = ScoutSdk.getType(RuntimeClasses.IService);

  private ICachedTypeHierarchy m_serviceHierarchy;
  private PackageContentChangedListener m_packageContentListener;
  private IPackageFragment m_servicePackage;

  public OutlineServiceTablePage(AbstractPage parent) {
    setParent(parent);
    // package
    m_servicePackage = getScoutResource().getPackageFragment(getScoutResource().getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_OUTLINE));
//    if (TypeUtility.exists(m_servicePackage)) {
    m_packageContentListener = new PackageContentChangedListener(this, m_servicePackage);
//    new PackageElementChangedListener(m_servicePackage) {
//      @Override
//      public void packageContentChanged(int flags, IJavaElement e) {
//        markStructureDirty();
//      }
//    };
    JavaCore.addElementChangedListener(m_packageContentListener);
//    }
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Services));
    setName(Texts.get("OutlineServiceTablePage"));
  }

  @Override
  public void unloadPage() {
    if (m_packageContentListener != null) {
      JavaCore.removeElementChangedListener(m_packageContentListener);
    }
    super.unloadPage();
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_serviceHierarchy != null) {
      m_serviceHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.OUTLINE_SERVICE_TABLE_PAGE;
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
  public void loadChildrenImpl() {
    for (IType service : resolveAllOutlineServices()) {
      IType serviceInterface = null;
      IType[] interfaces = m_serviceHierarchy.getSuperInterfaces(service, TypeFilters.getElementNameFilter("I" + service.getElementName()));
      if (interfaces.length > 0) {
        serviceInterface = interfaces[0];
      }
      new OutlineServiceNodePage(this, service, serviceInterface);
    }
  }

  protected IType[] resolveAllOutlineServices() {
    if (m_serviceHierarchy == null) {
      m_serviceHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iService);
    }
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(
        TypeFilters.getClassesInProject(getScoutResource().getJavaProject()),
        TypeFilters.getPackageFilter(m_servicePackage)
        );
    IType[] services = m_serviceHierarchy.getAllSubtypes(iService, filter, TypeComparators.getTypeNameComparator());
    return services;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{FormDataSqlBindingValidateAction.class, OutlineServiceNewAction.class};
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    if (menu instanceof FormDataSqlBindingValidateAction) {
      ((FormDataSqlBindingValidateAction) menu).setTyperesolver(new ITypeResolver() {
        @Override
        public IType[] getTypes() {
          return resolveAllOutlineServices();
        }
      });
    }
    else if (menu instanceof OutlineServiceNewAction) {
      ((OutlineServiceNewAction) menu).setScoutBundle(getScoutResource());
    }
  }
}
