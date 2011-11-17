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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractTypeProposalPresenter;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>CodeTypePresenter</h3> ...
 */
public class LookupCallProposalPresenter extends AbstractTypeProposalPresenter {
  final IType lookupCall = TypeUtility.getType(RuntimeClasses.LookupCall);

  public LookupCallProposalPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent, null, true);
  }

  @Override
  protected IType[] provideScoutTypes(IJavaProject project, IType ownerType) {
    return TypeUtility.getPrimaryTypeHierarchy(lookupCall).getAllSubtypes(lookupCall, TypeFilters.getTypesOnClasspath(project), TypeComparators.getTypeNameComparator());
  }

  @Override
  protected void createContextMenu(MenuManager manager) {
    super.createContextMenu(manager);
    if (getCurrentSourceValue() != null) {
      final IType lc = getCurrentSourceValue().getJavaClass();
      if (lc != null) {
        String entityName = NamingUtility.removeSuffixes(lc.getElementName(), "Call");
        String lookupServiceFqn = lc.getPackageFragment().getElementName() + ".I" + entityName + "Service";
        if (TypeUtility.existsType(lookupServiceFqn)) {
          final IType lookupServiceInterface = TypeUtility.getType(lookupServiceFqn);
          if (lookupServiceInterface != null) {
            manager.add(new Action("Go to " + lookupServiceInterface.getElementName(), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.StatusInfo)) {
              @Override
              public void run() {
                showJavaElementInEditor(lookupServiceInterface);
              }
            });
          }
        }
        String serviceFqn = lc.getPackageFragment().getElementName().replace(".shared.", ".server.") + "." + entityName + "Service";
        if (TypeUtility.existsType(serviceFqn)) {
          final IType lookupServiceImplementation = TypeUtility.getType(serviceFqn);
          if (lookupServiceImplementation != null) {
            manager.add(new Action("Go to " + lookupServiceImplementation.getElementName(), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.StatusInfo)) {
              @Override
              public void run() {
                showJavaElementInEditor(lookupServiceImplementation);
              }
            });
          }
        }
      }
    }
  }
}
