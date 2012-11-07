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

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.scout.sdk.ui.extensions.IDragSourceDelegator;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.AbstractBoxNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;
import org.eclipse.swt.dnd.DragSourceEvent;

public class FormFieldDragSourceDelegator implements IDragSourceDelegator {

  @Override
  public boolean acceptDrag(DragSourceEvent event, TreeViewer outlineViewer) {
    StructuredSelection selection = (StructuredSelection) outlineViewer.getSelection();
    if (selection.size() == 1) {
      Object firstElement = selection.getFirstElement();
      return (firstElement instanceof AbstractFormFieldNodePage || firstElement instanceof AbstractBoxNodePage);
    }
    return false;
  }

  @Override
  public void dragSetData(DragSourceEvent event, TreeViewer outlineViewer) {
    LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
    if (transfer.isSupportedType(event.dataType)) {
      transfer.setSelection(outlineViewer.getSelection());
      event.data = transfer;
    }
  }

  @Override
  public void dragFinished(DragSourceEvent event, TreeViewer outlineViewer) {
    // void delete of the moved field is handled in the move operation.
  }

}
