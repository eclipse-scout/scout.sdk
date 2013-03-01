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
package org.eclipse.scout.sdk.ui.action.create;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.form.SearchFormNewWizard;
import org.eclipse.scout.sdk.util.ScoutMethodUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public class SearchFormNewAction extends AbstractWizardAction {

  private IType m_type;
  private IScoutBundle m_scoutRes;

  public SearchFormNewAction() {
    super(Texts.get("CreateSearchForm"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SearchFormAdd), null, false, Category.NEW);
  }

  @Override
  public boolean isVisible() {
    return !m_scoutRes.isBinary();
  }

  public void init(IScoutBundle scoutRes) {
    init(null, scoutRes);
  }

  public void init(IType type, IScoutBundle scoutRes) {
    m_type = type;
    m_scoutRes = scoutRes;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    SearchFormNewWizard wizard = new SearchFormNewWizard(m_scoutRes);
    if (m_type != null) {
      wizard.setTablePage(m_type);
      IMethod titleMethod = TypeUtility.getMethod(m_type, "getConfiguredTitle");
      if (TypeUtility.exists(titleMethod)) {
        try {
          wizard.setNlsEntry(ScoutMethodUtility.getReturnNlsEntry(titleMethod));
        }
        catch (CoreException e) {
          ScoutSdkUi.logWarning("could not parse nls entry for method '" + titleMethod.getElementName() + "'.", e);
        }
      }
    }
    return wizard;
  }
}
