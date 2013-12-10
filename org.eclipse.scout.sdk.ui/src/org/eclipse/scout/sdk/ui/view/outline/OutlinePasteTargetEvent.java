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
package org.eclipse.scout.sdk.ui.view.outline;

import java.awt.datatransfer.Transferable;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.events.TypedEvent;

public class OutlinePasteTargetEvent extends TypedEvent {

  private static final long serialVersionUID = 1L;

  private Transferable m_transferData;
  private IPage m_page;

  public OutlinePasteTargetEvent(TreeViewer viewer) {
    super(viewer);
    widget = viewer.getControl();
    display = viewer.getControl().getDisplay();
  }

  public Transferable getTransferData() {
    return m_transferData;
  }

  public void setTransferData(Transferable transferData) {
    m_transferData = transferData;
  }

  public IPage getPage() {
    return m_page;
  }

  public void setPage(IPage page) {
    m_page = page;
  }
}
