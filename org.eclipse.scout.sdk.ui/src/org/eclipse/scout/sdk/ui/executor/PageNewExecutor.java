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
package org.eclipse.scout.sdk.ui.executor;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageWithTableNodePage;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.wizard.page.PageNewWizard;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.ui.INewWizard;

/**
 * <h3>{@link PageNewExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 08.10.2014
 */
public class PageNewExecutor extends AbstractWizardExecutor {

  @Override
  public INewWizard getNewWizardInstance() {
    return new PageNewWizard();
  }

  @Override
  public boolean canRun(IStructuredSelection selection) {
    IScoutBundle scoutBundle = UiUtility.getScoutBundleFromSelection(selection);
    IType type = UiUtility.getTypeFromSelection(selection);
    boolean isEditable = isEditable(scoutBundle);
    if (type == null) {
      return isEditable;
    }
    return isEditable && !TypeUtility.exists(TypeUtility.getMethod(type, PageWithTableNodePage.METHOD_EXEC_CREATE_CHILD_PAGE)); // for page with table: if there is already a sub-page: don't show the menu
  }
}
