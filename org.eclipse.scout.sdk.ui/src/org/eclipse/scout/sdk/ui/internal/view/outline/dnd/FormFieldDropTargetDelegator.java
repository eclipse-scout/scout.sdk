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
package org.eclipse.scout.sdk.ui.internal.view.outline.dnd;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.dnd.FormFieldDndOperation;
import org.eclipse.scout.sdk.ui.action.dnd.FormFieldRelocateAction;
import org.eclipse.scout.sdk.ui.extensions.IDropTargetDelegator;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.AbstractBoxNodePage;
import org.eclipse.scout.sdk.ui.view.outline.OutlineDropTargetEvent;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.swt.dnd.DND;

public class FormFieldDropTargetDelegator implements IDropTargetDelegator {

  @Override
  public boolean validateDrop(OutlineDropTargetEvent event) {
    try {
      if (!LocalSelectionTransfer.getTransfer().isSupportedType(event.getTransferData())) {
        return false;
      }
      Object currentTargetPage = event.getCurrentTarget();
      IType targetType = null;
      if (currentTargetPage instanceof AbstractBoxNodePage) {
        targetType = ((AbstractBoxNodePage) currentTargetPage).getType();
      }
      else if (currentTargetPage instanceof AbstractFormFieldNodePage) {
        if (event.getCurrentLocation() == ViewerDropAdapter.LOCATION_ON) {
          return false;
        }
        targetType = ((AbstractFormFieldNodePage) currentTargetPage).getType();
      }
      if (targetType == null) {
        return false;
      }
      IType selectedType = null;
      if (event.getSelectedObject() instanceof AbstractBoxNodePage) {
        selectedType = ((AbstractBoxNodePage) event.getSelectedObject()).getType();
      }
      else if (event.getSelectedObject() instanceof AbstractFormFieldNodePage) {
        selectedType = ((AbstractFormFieldNodePage) event.getSelectedObject()).getType();
      }
      if (selectedType == null || targetType.equals(selectedType)) {
        return false;
      }
      // do not allow copy boxes with inner types within the same compilation unit -> import problems of inner fields
      if (event.getOperation() == DND.DROP_COPY) {
        if (selectedType.getCompilationUnit().equals(targetType.getCompilationUnit())) {
          if (SdkTypeUtility.getFormFields(selectedType).length > 0) {
            return false;
          }
        }
      }
      if (selectedType.equals(targetType)) {
        return false;
      }
      return true;
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not validate drop location.", e);
    }
    return false;
  }

  @Override
  public boolean expand(OutlineDropTargetEvent event) {
    return !(event.getCurrentTarget() instanceof AbstractFormFieldNodePage);
  }

  @Override
  public boolean performDrop(OutlineDropTargetEvent event) {
    if (event.getOperation() == DND.DROP_COPY || event.getOperation() == DND.DROP_MOVE) {
      AbstractScoutTypePage sourcePage = (AbstractScoutTypePage) event.getSelectedObject();
      AbstractScoutTypePage targetPage = (AbstractScoutTypePage) event.getCurrentTarget();

      FormFieldRelocateAction action = new FormFieldRelocateAction(sourcePage.getType(), sourcePage.getOutlineView().getSite().getShell());
      action.setCreateCopy(event.getOperation() == DND.DROP_COPY);
      action.setLocation(dndToMoveOperationLocation(event.getCurrentLocation()));
      if (event.getCurrentLocation() == ViewerDropAdapter.LOCATION_ON) {
        action.setTargetDeclaringType(targetPage.getType());
      }
      else {
        action.setTargetDeclaringType(targetPage.getType().getDeclaringType());
        action.setNeighborField(targetPage.getType());
      }
      action.run();
    }
    return true;
  }

  private int dndToMoveOperationLocation(int location) {
    switch (location) {
      case ViewerDropAdapter.LOCATION_AFTER:
        return FormFieldDndOperation.AFTER;
      case ViewerDropAdapter.LOCATION_BEFORE:
        return FormFieldDndOperation.BEFORE;
      default:
        return FormFieldDndOperation.LAST;
    }
  }

}
