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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.scout.sdk.ui.extensions.IDragSourceDelegator;
import org.eclipse.scout.sdk.ui.extensions.IDropTargetDelegator;
import org.eclipse.scout.sdk.ui.internal.extensions.DndExtensionPoint;
import org.eclipse.scout.sdk.ui.view.outline.OutlineDropTargetEvent;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

public class ExplorerDndSupport {

  private final TreeViewer m_outlineTree;

  public ExplorerDndSupport(TreeViewer outlineTree) {
    m_outlineTree = outlineTree;
    int ops = DND.DROP_COPY | DND.DROP_MOVE;
    Transfer[] transfers = new Transfer[]{LocalSelectionTransfer.getTransfer(), FileTransfer.getInstance()};
    outlineTree.addDragSupport(ops, transfers, new P_DragSourceAdapter());
    // drop
    P_DropTargetAdapter adapter = new P_DropTargetAdapter(outlineTree);
    adapter.setExpandEnabled(false);
    outlineTree.addDropSupport(ops, transfers, adapter);
  }

  public TreeViewer getOutlineViewer() {
    return m_outlineTree;
  }

  private class P_DragSourceAdapter extends DragSourceAdapter {
    private IDragSourceDelegator m_currentDragDelegator;

    @Override
    public void dragStart(DragSourceEvent event) {
      for (IDragSourceDelegator del : DndExtensionPoint.getDragSourceDelegators()) {
        if (del.acceptDrag(event, getOutlineViewer())) {
          m_currentDragDelegator = del;
          return;
        }
      }
      event.doit = false;
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
      if (m_currentDragDelegator != null) {
        m_currentDragDelegator.dragSetData(event, getOutlineViewer());
      }
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
      if (m_currentDragDelegator != null) {
        m_currentDragDelegator.dragFinished(event, getOutlineViewer());
        m_currentDragDelegator = null;
      }
    }
  } // end class P_DragSourceAdapter

  private class P_DropTargetAdapter extends ViewerDropAdapter {
    private IDropTargetDelegator m_currentDropDelegator;

    protected P_DropTargetAdapter(Viewer viewer) {
      super(viewer);
    }

    @Override
    public void dragOver(DropTargetEvent event) {
      super.dragOver(event);
      if (m_currentDropDelegator != null) {
        if (m_currentDropDelegator.expand(createDropTargetEvent())) {
          event.feedback |= DND.FEEDBACK_EXPAND;
        }
      }
    }

    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferData) {
      OutlineDropTargetEvent event = createDropTargetEvent();
      event.setTransferData(transferData);
      for (IDropTargetDelegator del : DndExtensionPoint.getDropTargetDelegators()) {
        if (del.validateDrop(event)) {
          m_currentDropDelegator = del;
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean performDrop(Object data) {
      boolean retVal = false;
      if (m_currentDropDelegator != null) {
        OutlineDropTargetEvent event = createDropTargetEvent();
        event.setTransferObject(data);

        retVal = m_currentDropDelegator.performDrop(event);
        m_currentDropDelegator = null;
      }
      return retVal;
    }

    private OutlineDropTargetEvent createDropTargetEvent() {
      OutlineDropTargetEvent event = new OutlineDropTargetEvent();
      event.widget = getViewer().getControl();
      event.display = getViewer().getControl().getDisplay();
      event.setCurrentLocation(getCurrentLocation());
      event.setCurrentTarget(getCurrentTarget());
      event.setOperation(getCurrentOperation());
      event.setSelectedObject(getSelectedObject());
      return event;
    }
  } // end class P_DropTargetAdapter
}
