/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.swt.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.TypeNewWizard;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.TypeNewWizardPage.JavaSearchScopeFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.widgets.Shell;

public class AuthenticationHandlerNewWizardAction extends AbstractLinkAction {
  private int m_result;
  private IScoutBundle m_bundle;
  private WebserviceEnum m_webserviceEnum;

  public AuthenticationHandlerNewWizardAction() {
    super(Texts.get("Action_newTypeX", Texts.get("AuthenticationHandler")), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolAdd));
    setLeadingText(Texts.get("CreateNewX", Texts.get("AuthenticationHandler")));
    setLinkText(Texts.get("here"));
  }

  public void init(IScoutBundle bundle, WebserviceEnum webserviceEnum) {
    m_bundle = bundle;
    m_webserviceEnum = webserviceEnum;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    TypeNewWizard wizard = new TypeNewWizard(m_bundle, Texts.get("AuthenticationHandler"));
    wizard.setAllowModifyInterfaceType(false);
    wizard.setAllowModifySuperType(true);
    wizard.setAllowModifyPackage(true);
    wizard.setTypeSuffix("AuthenticationHandler");
    wizard.setInterfaceTypeSearchScopeFactory(new P_JavaSearchScopeFactory(m_webserviceEnum, m_bundle, true));
    wizard.setSuperTypeSearchScopeFactory(new P_JavaSearchScopeFactory(m_webserviceEnum, m_bundle, false));

    if (m_webserviceEnum == WebserviceEnum.Provider) {
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
    m_result = wizardDialog.open();
    return null;
  }

  public int getResult() {
    return m_result;
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
      if (m_webserviceEnum == WebserviceEnum.Provider) {
        type = TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerProvider);
      }
      else {
        type = TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerConsumer);
      }
      IType[] subTypes;
      if (m_onlyInterfaceTypes) {
        IType[] candidates = TypeUtility.getPrimaryTypeHierarchy(type).getAllSubtypes(type, new ITypeFilter() {

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
        subTypes = new IType[candidates.length + 1];
        System.arraycopy(candidates, 0, subTypes, 1, candidates.length);
        subTypes[0] = type;
      }
      else {
        subTypes = TypeUtility.getPrimaryTypeHierarchy(type).getAllSubtypes(type, TypeFilters.getTypesOnClasspath(type.getJavaProject()));
      }
      return SearchEngine.createJavaSearchScope(subTypes);
    }
  }
}
