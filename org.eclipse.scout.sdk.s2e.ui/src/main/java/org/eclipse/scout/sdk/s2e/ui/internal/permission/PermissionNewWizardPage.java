/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.permission;

import java.security.Permission;
import java.util.Optional;

import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
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
  protected Optional<ITypeNameSupplier> calcSuperTypeDefaultFqn() {
    return scoutApi().map(IScoutApi::AbstractPermission);
  }

  @Override
  protected Optional<ITypeNameSupplier> calcSuperTypeDefaultBaseFqn() {
    return Optional.of(Permission.class.getName()).map(ITypeNameSupplier::of);
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
