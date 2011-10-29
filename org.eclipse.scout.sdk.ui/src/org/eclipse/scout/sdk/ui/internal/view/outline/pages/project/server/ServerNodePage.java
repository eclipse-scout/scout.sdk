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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server;

import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.scout.commons.CompositeLong;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.util.wellform.WellformServerBundleOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.ExportServerWarAction;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.validation.FormDataSqlBindingValidateAction;
import org.eclipse.scout.sdk.ui.action.validation.ITypeResolver;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.CommonServicesNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.custom.CustomServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.lookup.LookupServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.outline.OutlineServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.process.ProcessServiceTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.typecache.IPrimaryTypeTypeHierarchy;

/**
 * <h3>ServerNodePage</h3> ...
 */
public class ServerNodePage extends AbstractPage {
  final IType iService = ScoutSdk.getType(RuntimeClasses.IService);
  final IType iServerSession = ScoutSdk.getType(RuntimeClasses.IServerSession);

  private final IScoutBundle m_serverBundle;
  private ICachedTypeHierarchy m_serverSessionHierarchy;

  public ServerNodePage(IPage parent, IScoutBundle serverBundle) {
    setParent(parent);
    m_serverBundle = serverBundle;
    setName(getScoutResource().getSimpleName());
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServerBundle));
    m_serverSessionHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iServerSession);
    m_serverSessionHierarchy.addHierarchyListener(getPageDirtyListener());
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SERVER_NODE_PAGE;
  }

  @Override
  public int getOrder() {
    return 300;
  }

  @Override
  public void unloadPage() {
    m_serverSessionHierarchy.removeHierarchyListener(getPageDirtyListener());
  }

  @Override
  public boolean isInitiallyLoaded() {
    return true;
  }

  @Override
  public IScoutBundle getScoutResource() {
    return m_serverBundle;
  }

  @Override
  public void loadChildrenImpl() {

    try {
      ITypeFilter filter = TypeFilters.getClassesInProject(getScoutResource().getJavaProject());
      IType[] serverSessions = m_serverSessionHierarchy.getAllSubtypes(iServerSession, filter, TypeComparators.getTypeNameComparator());
      if (serverSessions.length > 1) {
        ScoutSdkUi.logError("The server bundle '" + getScoutResource().getBundleName() + "' can have in maximum 1 server session.");
      }
      else if (serverSessions.length == 1) {
        new ServerSessionNodePage(this, serverSessions[0]);
      }
      else {
        ScoutSdkUi.logInfo("No server session found in server bundle '" + getScoutResource().getBundleName() + "'.");
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error occured during loading '" + ServerSessionNodePage.class.getSimpleName() + "' node in bundle '" + getScoutResource().getBundleName() + "'.", e);
    }
    try {
      new LookupServiceTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error occured during loading '" + LookupServiceTablePage.class.getSimpleName() + "' node in bundle '" + getScoutResource().getBundleName() + "'.", e);
    }

    try {
      new OutlineServiceTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error occured during loading '" + OutlineServiceTablePage.class.getSimpleName() + "' node in bundle '" + getScoutResource().getBundleName() + "'.", e);
    }
    try {
      new ProcessServiceTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error occured during loading '" + ProcessServiceTablePage.class.getSimpleName() + "' node in bundle '" + getScoutResource().getBundleName() + "'.", e);
    }
    try {
      new CommonServicesNodePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error occured during loading '" + CommonServicesNodePage.class.getSimpleName() + "' node in bundle '" + getScoutResource().getBundleName() + "'.", e);
    }
    try {
      new CustomServiceTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error occured during loading '" + CustomServiceTablePage.class.getSimpleName() + "' node in bundle '" + getScoutResource().getBundleName() + "'.", e);
    }
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  protected IType[] resolveServices() {
    IPrimaryTypeTypeHierarchy serviceHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iService);
    IType[] services = serviceHierarchy.getAllSubtypes(iService, TypeFilters.getClassesInProject(getScoutResource().getJavaProject()));
    return services;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{WellformAction.class, FormDataSqlBindingValidateAction.class, ExportServerWarAction.class};
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    if (menu instanceof WellformAction) {
      ((WellformAction) menu).setOperation(new WellformServerBundleOperation(getScoutResource()));
    }
    else if (menu instanceof FormDataSqlBindingValidateAction) {
      ((FormDataSqlBindingValidateAction) menu).setTyperesolver(new ITypeResolver() {
        @Override
        public IType[] getTypes() {
          return resolveServices();
        }
      });
    }
    else if (menu instanceof ExportServerWarAction) {
      ((ExportServerWarAction) menu).setScoutBundle(getScoutResource());
    }
  }

  private IType resolveType(final String fqn) throws CoreException {
    final TreeMap<CompositeLong, IType> matchList = new TreeMap<CompositeLong, IType>();
    //speed tuning, only search for last component of pattern, remaining checks are done in accept
    String fastPat = fqn;
    int i = fastPat.lastIndexOf('.');
    if (i >= 0) {
      fastPat = fastPat.substring(i + 1);
    }
    new SearchEngine().search(
        SearchPattern.createPattern(fastPat, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH),
        new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
        SearchEngine.createJavaSearchScope(new IJavaElement[]{getScoutResource().getJavaProject()},
            IJavaSearchScope.REFERENCED_PROJECTS | IJavaSearchScope.SOURCES),

        new SearchRequestor() {
          @Override
          public final void acceptSearchMatch(SearchMatch match) throws CoreException {
            if (match instanceof TypeDeclarationMatch) {
              TypeDeclarationMatch typeMatch = (TypeDeclarationMatch) match;

              IType t = (IType) typeMatch.getElement();
//              matchList.put(new CompositeLong(t.isBinary() ? 1 : 0, matchList.size()), t);
              if (t.getFullyQualifiedName('.').indexOf(fqn) >= 0) {

                matchList.put(new CompositeLong(t.isBinary() ? 1 : 0, matchList.size()), t);
              }
            }
          }
        },
        null
        );
    if (matchList.size() > 1) {
      ScoutSdk.logWarning("found more than one type matches for '" + fqn + "' (matches: '" + matchList.size() + "').");
    }
    else if (matchList.size() < 1) {
      ScoutSdk.logWarning("found no type matches for '" + fqn + "'.");
      return null;
    }
    return matchList.firstEntry().getValue();
  }
}
