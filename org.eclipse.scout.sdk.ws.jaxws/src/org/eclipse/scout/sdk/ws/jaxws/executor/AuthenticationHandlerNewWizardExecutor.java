/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.executor;

import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.ui.executor.AbstractExecutor;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.AuthenticationHandlerTablePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.TypeNewWizard;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.TypeNewWizardPage.JavaSearchScopeFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link AuthenticationHandlerNewWizardExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
public class AuthenticationHandlerNewWizardExecutor extends AbstractExecutor {

  private IScoutBundle m_bundle;
  private WebserviceEnum m_webserviceEnum;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    Object selectedElement = selection.getFirstElement();
    if (selectedElement instanceof AuthenticationHandlerTablePage) {
      AuthenticationHandlerTablePage ahtp = (AuthenticationHandlerTablePage) selectedElement;
      m_bundle = ahtp.getScoutBundle();
      m_webserviceEnum = ahtp.getWebserviceEnum();
    }
    return isEditable(m_bundle);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    TypeNewWizard wizard = new TypeNewWizard(m_bundle, Texts.get("AuthenticationHandler"));
    wizard.setAllowModifyInterfaceType(false);
    wizard.setAllowModifySuperType(true);
    wizard.setAllowModifyPackage(true);
    wizard.setTypeSuffix("AuthenticationHandler");
    wizard.setInterfaceTypeSearchScopeFactory(new P_JavaSearchScopeFactory(m_webserviceEnum, m_bundle, true));
    wizard.setSuperTypeSearchScopeFactory(new P_JavaSearchScopeFactory(m_webserviceEnum, m_bundle, false));

    if (m_webserviceEnum == WebserviceEnum.PROVIDER) {
      wizard.setInterfaceType(TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerProvider));
      wizard.setRecommendedPackageFragment(JaxWsSdkUtility.getRecommendedProviderSecurityPackageName(m_bundle));
    }
    else {
      wizard.setInterfaceType(TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerConsumer));
      wizard.setRecommendedPackageFragment(JaxWsSdkUtility.getRecommendedConsumerSecurityPackageName(m_bundle));
    }

    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setPageSize(680, 350);
    wizardDialog.setHelpAvailable(false);
    wizardDialog.open();
    return null;
  }

  private class P_JavaSearchScopeFactory extends JavaSearchScopeFactory {

    private WebserviceEnum m_webserviceEnum;
    private IScoutBundle m_bundle;
    private boolean m_onlyInterfaceTypes;

    public P_JavaSearchScopeFactory(WebserviceEnum webserviceEnum, IScoutBundle bundle, boolean onlyInterfaceTypes) {
      m_webserviceEnum = webserviceEnum;
      m_bundle = bundle;
      m_onlyInterfaceTypes = onlyInterfaceTypes;
    }

    @Override
    public IJavaSearchScope create() {
      IType type;
      if (m_webserviceEnum == WebserviceEnum.PROVIDER) {
        type = TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerProvider);
      }
      else {
        type = TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerConsumer);
      }
      Set<IType> subTypes;
      if (m_onlyInterfaceTypes) {
        subTypes = TypeUtility.getPrimaryTypeHierarchy(type).getAllSubtypes(type, new ITypeFilter() {
          @Override
          public boolean accept(IType candidate) {
            try {
              if (!TypeUtility.exists(candidate) || !candidate.isInterface()) {
                return false;
              }
              return TypeUtility.isOnClasspath(candidate, m_bundle.getJavaProject());
            }
            catch (JavaModelException e) {
              JaxWsSdk.logError(e);
              return false;
            }
          }
        });
        // add interface type itself
        subTypes.add(type);
      }
      else {
        subTypes = TypeUtility.getPrimaryTypeHierarchy(type).getAllSubtypes(type, TypeFilters.getTypesOnClasspath(type.getJavaProject()));
      }
      return SearchEngine.createJavaSearchScope(subTypes.toArray(new IType[subTypes.size()]));
    }
  }
}
