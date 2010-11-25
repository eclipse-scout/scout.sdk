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
import org.eclipse.jface.action.MenuManager;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractTypeProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>CodeTypePresenter</h3> ...
 */
public class LookupServiceProposalPresenter extends AbstractTypeProposalPresenter {
  final IType iLookupService = ScoutSdk.getType(RuntimeClasses.ILookupService);

  public LookupServiceProposalPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent, null, true);
  }

  @Override
  protected IType[] provideScoutTypes(IJavaProject project, IType ownerType) {
    return ScoutSdk.getPrimaryTypeHierarchy(iLookupService).getAllSubtypes(iLookupService, TypeFilters.getTypesOnClasspath(project), TypeComparators.getTypeNameComparator());
  }

  @Override
  protected void createContextMenu(MenuManager manager) {
    super.createContextMenu(manager);
//    if (getCurrentSourceValue() != null) {
//      final IType lookupCall = getCurrentSourceValue().getJavaClass();
//      if (lookupCall != null) {
//        String entityName = NamingUtility.removeSuffixes(lookupCall.getElementName(), "Call");
//        final IType lookupServiceInterface = ScoutSdk.getType(lookupCall.getPackageFragment().getElementName() + ".I" + entityName + "Service");
//        if (lookupServiceInterface != null) {
//          manager.add(new Action("Go to " + lookupServiceInterface.getElementName(), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_INFO)) {
//            @Override
//            public void run() {
//              showJavaElementInEditor(lookupServiceInterface);
//            }
//          });
//        }
//        final IType lookupServiceImplementation = ScoutSdk.getType(lookupCall.getPackageFragment().getElementName().replace(".shared.", ".server.") + "." + entityName + "Service");
//        if (lookupServiceImplementation != null) {
//          manager.add(new Action("Go to " + lookupServiceImplementation.getElementName(), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_INFO)) {
//            @Override
//            public void run() {
//              showJavaElementInEditor(lookupServiceImplementation);
//            }
//          });
//        }
//      }
//    }
  }
}
