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
package org.eclipse.scout.sdk.ui.wizard.form.fields.buttonfield;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.ui.IWorkbench;

public class OkButtonNewWizard extends ButtonFieldNewWizard {

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    setWindowTitle(Texts.get("NewOkButton"));
    IJavaProject jp = getDeclaringType().getJavaProject();

    getButtonFieldNewWizardPage().setSuperType(RuntimeClasses.getSuperType(IRuntimeClasses.AbstractOkButton, jp));

    INlsProject nlsProject = ScoutTypeUtility.getScoutBundle(jp).getNlsProject();
    INlsEntry entry = null;
    if (nlsProject != null) {
      entry = nlsProject.getEntry("Ok");
    }
    getButtonFieldNewWizardPage().setTypeName("OkButton");
    getButtonFieldNewWizardPage().setNlsName(entry);
  }

}
