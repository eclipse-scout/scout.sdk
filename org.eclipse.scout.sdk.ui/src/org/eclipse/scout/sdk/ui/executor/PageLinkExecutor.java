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
import org.eclipse.scout.sdk.ui.wizard.page.PageLinkWizard;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.ui.INewWizard;

/**
 * <h3>{@link PageLinkExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 10.10.2014
 */
public class PageLinkExecutor extends AbstractWizardExecutor {

  @Override
  public INewWizard getNewWizardInstance() {
    return new PageLinkWizard();
  }

  @Override
  public boolean canRun(IStructuredSelection selection) {
    IType type = UiUtility.getTypeFromSelection(selection);
    return isEditable(type)
        && isEditable(UiUtility.getScoutBundleFromSelection(selection))
        && !TypeUtility.exists(TypeUtility.getMethod(type, PageWithTableNodePage.METHOD_EXEC_CREATE_CHILD_PAGE)); // for page with table: if there is already a sub-page: don't show the menu
  }
}
