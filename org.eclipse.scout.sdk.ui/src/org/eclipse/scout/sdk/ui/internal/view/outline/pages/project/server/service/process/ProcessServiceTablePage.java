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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.process;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.ProcessServiceNewAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.type.PackageContentChangedListener;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class ProcessServiceTablePage extends AbstractPage {
  final IType iService = TypeUtility.getType(RuntimeClasses.IService);

  private ICachedTypeHierarchy m_serviceHierarchy;
  private PackageContentChangedListener m_packageContentListener;
  private IPackageFragment m_processServicePackage;

  public ProcessServiceTablePage(AbstractPage parent) {
    setParent(parent);
    // package
    m_processServicePackage = getScoutResource().getPackageFragment(getScoutResource().getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_PROCESS));
    m_packageContentListener = new PackageContentChangedListener(this, m_processServicePackage);
    JavaCore.addElementChangedListener(m_packageContentListener);
    setName(Texts.get("ProcessServiceTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Services));
  }

  @Override
  public void unloadPage() {
    if (m_packageContentListener != null) {
      JavaCore.removeElementChangedListener(m_packageContentListener);
      m_packageContentListener = null;
    }
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
    return IScoutPageConstants.PROCESS_SERVICE_TABLE_PAGE;
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
    for (IType service : resolveAllProcessServices()) {
      IType serviceInterface = null;
      IType[] interfaces = m_serviceHierarchy.getSuperInterfaces(service, TypeFilters.getElementNameFilter("I" + service.getElementName()));
      if (interfaces.length > 0) {
        serviceInterface = interfaces[0];
      }
      new ProcessServiceNodePage(this, service, serviceInterface);
    }
  }

  protected IType[] resolveAllProcessServices() {
    if (m_serviceHierarchy == null || !m_serviceHierarchy.isCreated()) {
      m_serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iService);
    }
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(
        TypeFilters.getClassesInProject(getScoutResource().getJavaProject()),
        TypeFilters.getPackageFilter(m_processServicePackage)
        );
    IType[] processServices = m_serviceHierarchy.getAllSubtypes(iService, filter, TypeComparators.getTypeNameComparator());
    return processServices;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{FormDataSqlBindingValidateAction.class, ProcessServiceNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof FormDataSqlBindingValidateAction) {
      ((FormDataSqlBindingValidateAction) menu).setTyperesolver(new ITypeResolver() {
        @Override
        public IType[] getTypes() {
          return resolveAllProcessServices();
        }
      });
    }
    else if (menu instanceof ProcessServiceNewAction) {
      ((ProcessServiceNewAction) menu).setScoutBundle(getScoutResource());
    }
  }
}
