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
import org.eclipse.scout.sdk.operation.dnd.AbstractTypeDndOperation;
import org.eclipse.scout.sdk.ui.action.dnd.TableColumnRelocateAction;
import org.eclipse.scout.sdk.ui.extensions.IDropTargetDelegator;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table.ColumnNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table.ColumnTablePage;
import org.eclipse.scout.sdk.ui.view.outline.OutlineDropTargetEvent;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;
import org.eclipse.swt.dnd.DND;

public class TableColumnDropTargetDelegator implements IDropTargetDelegator {

  @Override
  public boolean validateDrop(OutlineDropTargetEvent event) {
    try {
      if (!LocalSelectionTransfer.getTransfer().isSupportedType(event.getTransferData())) {
        return false;
      }
      Object currentTargetPage = event.getCurrentTarget();
      IType targetType = null;
//      IType declaringTable = null;
      if (currentTargetPage instanceof ColumnTablePage) {
        if (event.getCurrentLocation() != ViewerDropAdapter.LOCATION_ON) {
          return false;
        }
        targetType = ((ColumnTablePage) currentTargetPage).getColumnDeclaringType();
//        declaringTable = ((ColumnTablePage) currentTargetPage).getColumnDeclaringType();
      }
      else if (currentTargetPage instanceof ColumnNodePage) {
        if (event.getCurrentLocation() == ViewerDropAdapter.LOCATION_ON) {
          return false;
        }
        targetType = ((ColumnNodePage) currentTargetPage).getType();
//        declaringTable = ((ColumnNodePage) currentTargetPage).getType().getDeclaringType();
      }
      if (targetType == null) {
        return false;
      }
      IType selectedType = null;
      if (event.getSelectedObject() instanceof ColumnNodePage) {
        selectedType = ((ColumnNodePage) event.getSelectedObject()).getType();
      }
      if (selectedType == null) {
        return false;
      }

      if (selectedType.equals(targetType)) {
        return false;
      }
      return true;
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("could not validate drop location.", e);
      return false;
    }
  }

  @Override
  public boolean expand(OutlineDropTargetEvent event) {
    return !(event.getCurrentTarget() instanceof AbstractFormFieldNodePage);
  }

  @Override
  public boolean performDrop(OutlineDropTargetEvent event) {
    if (event.getOperation() == DND.DROP_COPY || event.getOperation() == DND.DROP_MOVE) {
      IType targetDeclaringType = null;
      IType targetNeighborType = null;
      if (event.getCurrentTarget() instanceof ColumnNodePage) {
        targetNeighborType = ((ColumnNodePage) event.getCurrentTarget()).getType();
        targetDeclaringType = targetNeighborType.getDeclaringType();
      }
      else if (event.getCurrentTarget() instanceof ColumnTablePage) {
        targetDeclaringType = ((ColumnTablePage) event.getCurrentTarget()).getColumnDeclaringType();
      }
      AbstractScoutTypePage sourcePage = (AbstractScoutTypePage) event.getSelectedObject();

      TableColumnRelocateAction action = new TableColumnRelocateAction(sourcePage.getType(), sourcePage.getOutlineView().getSite().getShell());
      action.setCreateCopy(event.getOperation() == DND.DROP_COPY);
      action.setLocation(dndToMoveOperationLocation(event.getCurrentLocation()));
      action.setTargetDeclaringType(targetDeclaringType);
      action.setNeighborField(targetNeighborType);
      action.run();

    }
    return true;
  }

  private int dndToMoveOperationLocation(int location) {
    switch (location) {
      case ViewerDropAdapter.LOCATION_AFTER:
        return AbstractTypeDndOperation.AFTER;
      case ViewerDropAdapter.LOCATION_BEFORE:
        return AbstractTypeDndOperation.BEFORE;
      default:
        return AbstractTypeDndOperation.LAST;
    }
  }
}
