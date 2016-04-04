/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.permission;

import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.wizard.CompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link PermissionNewWizardPage}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PermissionNewWizardPage extends CompilationUnitNewWizardPage {
  public PermissionNewWizardPage(PackageContainer packageContainer) {
    super(PermissionNewWizardPage.class.getName(), packageContainer, ISdkProperties.SUFFIX_PERMISSION,
        IJavaRuntimeTypes.Permission, IJavaRuntimeTypes.BasicPermission, ScoutTier.Shared);
    setTitle("Create a new Permission");
    setDescription(getTitle());
    setIcuGroupName("New Permission Details");
  }

  @Override
  public PermissionNewWizard getWizard() {
    return (PermissionNewWizard) super.getWizard();
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_PERMISSION_NEW_WIZARD_PAGE);
  }
}
