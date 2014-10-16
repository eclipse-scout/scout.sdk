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
package org.eclipse.scout.sdk.ui.wizard.form.fields;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.ui.extensions.AbstractInnerTypeWizard;
import org.eclipse.ui.IWorkbench;

public class FormFieldNewWizard extends AbstractInnerTypeWizard {

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);
    FormFieldSelectionWizardPage page1 = new FormFieldSelectionWizardPage(getDeclaringType());
    addPage(page1);
  }

  @Override
  public boolean needsPreviousAndNextButtons() {
    return true;
  }

  @Override
  public boolean canFinish() {
    return false;
  }
}
