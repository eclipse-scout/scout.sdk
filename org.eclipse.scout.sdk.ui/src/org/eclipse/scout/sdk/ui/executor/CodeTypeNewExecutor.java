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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.wizard.code.type.CodeTypeNewWizard;
import org.eclipse.ui.INewWizard;

/**
 * <h3>{@link CodeTypeNewExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 08.10.2014
 */
public class CodeTypeNewExecutor extends AbstractWizardExecutor {
  @Override
  public INewWizard getNewWizardInstance() {
    return new CodeTypeNewWizard();
  }

  @Override
  public boolean canRun(IStructuredSelection selection) {
    return isEditable(UiUtility.getScoutBundleFromSelection(selection));
  }

}
