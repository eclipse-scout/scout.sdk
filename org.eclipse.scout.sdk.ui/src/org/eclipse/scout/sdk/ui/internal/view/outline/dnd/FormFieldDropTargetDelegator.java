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

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.scout.sdk.operation.dnd.FormFieldDndOperation;
import org.eclipse.scout.sdk.ui.action.dnd.FormFieldRelocateAction;
import org.eclipse.scout.sdk.ui.extensions.IDropTargetDelegator;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.AbstractBoxNodePage;
import org.eclipse.scout.sdk.ui.view.outline.OutlineDropTargetEvent;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.dnd.DND;

public class FormFieldDropTargetDelegator implements IDropTargetDelegator {

  @Override
  public boolean validateDrop(OutlineDropTargetEvent event) {
    try {
      if (!LocalSelectionTransfer.getTransfer().isSupportedType(event.getTransferData())) {
        return false;
      }
      AbstractScoutTypePage targetPage = getGroupBoxTargetType(event);
      if (targetPage == null) {
        targetPage = getFormFieldTargetType(event);
      }
      if (targetPage == null) {
        return false;
      }
      AbstractScoutTypePage selectedPage = getSelectedPage(event);
      if (selectedPage == null) {
        return false;
      }
      if (!isValidTargetLocation(event, targetPage, selectedPage)) {
        return false;
      }
      if (isCopyAndSourceComplexAndTargetSameIcu(event, targetPage.getType(), selectedPage.getType())) {
        return false;
      }

      return true;
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("could not validate drop location.", e);
    }
    return false;
  }

  /**
   * @return the target group box node page if valid target
   */
  private AbstractBoxNodePage getGroupBoxTargetType(OutlineDropTargetEvent event) {
    if (event.getCurrentTarget() instanceof AbstractBoxNodePage) {
      AbstractBoxNodePage targetPage = (AbstractBoxNodePage) event.getCurrentTarget();
      if (event.getCurrentLocation() != ViewerDropAdapter.LOCATION_ON && targetPage.getType().getElementName().equals(SdkProperties.TYPE_NAME_MAIN_BOX)) {
        return null;
      }
      else {
        return targetPage;
      }
    }
    return null;
  }

  /**
   * do not allow to move a field on an other field.
   */
  private AbstractFormFieldNodePage getFormFieldTargetType(OutlineDropTargetEvent event) {
    if (event.getCurrentTarget() instanceof AbstractFormFieldNodePage) {
      if (event.getCurrentLocation() == ViewerDropAdapter.LOCATION_ON) {
        return null;
      }
      return (AbstractFormFieldNodePage) event.getCurrentTarget();
    }
    return null;
  }

  private AbstractScoutTypePage getSelectedPage(OutlineDropTargetEvent event) {
    if (event.getSelectedObject() instanceof AbstractBoxNodePage) {
      return (AbstractBoxNodePage) event.getSelectedObject();
    }
    else if (event.getSelectedObject() instanceof AbstractFormFieldNodePage) {
      return (AbstractFormFieldNodePage) event.getSelectedObject();
    }
    return null;
  }

  private boolean isValidTargetLocation(OutlineDropTargetEvent event, AbstractScoutTypePage targetPage, AbstractScoutTypePage selectedPage) {
    if (event.getOperation() == DND.DROP_MOVE) {
      if (targetPage.equals(selectedPage)) {
        return false;
      }
      if (targetPage.getParent().equals(selectedPage.getParent())) {
        List<IPage> children = targetPage.getParent().getChildren();
        int selectedIndex = children.indexOf(selectedPage);
        if (selectedIndex < 0) {
          ScoutSdkUi.logError("could not find child index of selected node.");
          return false;
        }
        if (event.getCurrentLocation() == ViewerDropAdapter.LOCATION_AFTER) {
          if (selectedIndex > 0 && children.get(selectedIndex - 1).equals(targetPage)) {
            return false;
          }
        }
        else if (event.getCurrentLocation() == ViewerDropAdapter.LOCATION_BEFORE) {
          if (selectedIndex < children.size() && children.get(selectedIndex + 1).equals(targetPage)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * do not allow copy boxes with inner types within the same compilation unit -> import problems of inner fields
   */
  private boolean isCopyAndSourceComplexAndTargetSameIcu(OutlineDropTargetEvent event, IType targetType, IType selectedType) {
    if (event.getOperation() == DND.DROP_COPY) {
      if (selectedType.getCompilationUnit().equals(targetType.getCompilationUnit())) {
        if (ScoutTypeUtility.getFormFields(selectedType).length > 0) {
          return true;
        }
      }
    }
    return false;
  }

  private int getPositionInDeclaringType(IType element) throws JavaModelException {
    int i = -1;
    if (element != null) {
      for (IType candidate : TypeUtility.getInnerTypes(element, null, ScoutTypeComparators.getOrderAnnotationComparator())) {
        i++;
        if (element.equals(candidate)) return i;
      }
    }
    return i;
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
