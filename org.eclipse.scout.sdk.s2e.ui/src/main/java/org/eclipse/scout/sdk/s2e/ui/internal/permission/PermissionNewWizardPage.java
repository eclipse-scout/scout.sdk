/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.permission;

import java.security.Permission;
import java.util.Optional;

import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractCompilationUnitNewWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link PermissionNewWizardPage}</h3>
 *
 * @since 5.2.0
 */
public class PermissionNewWizardPage extends AbstractCompilationUnitNewWizardPage {
  public PermissionNewWizardPage(PackageContainer packageContainer) {
    super(PermissionNewWizardPage.class.getName(), packageContainer, ISdkConstants.SUFFIX_PERMISSION, ScoutTier.Shared);
    setTitle("Create a new Permission");
    setDescription(getTitle());
    setIcuGroupName("New Permission Details");
  }

  @Override
  protected Optional<IClassNameSupplier> calcSuperTypeDefaultFqn() {
    return scoutApi().map(IScoutApi::AbstractPermission);
  }

  @Override
  protected Optional<IClassNameSupplier> calcSuperTypeDefaultBaseFqn() {
    return Optional.of(Permission.class.getName()).map(IClassNameSupplier::raw);
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
