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
package org.eclipse.scout.sdk.ui.internal.view.outline.clipboard;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.scout.sdk.ui.extensions.ICopySourceDelegator;
import org.eclipse.scout.sdk.ui.extensions.IPasteTargetDelegator;
import org.eclipse.scout.sdk.ui.internal.extensions.CopyAndPasteExtensionPoint;
import org.eclipse.scout.sdk.ui.view.outline.OutlineCopySourceEvent;
import org.eclipse.scout.sdk.ui.view.outline.OutlinePasteTargetEvent;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;

public class ExplorerCopyAndPasteSupport {

  public static boolean performPaste(TreeViewer viewer, AbstractPage page) {

    OutlinePasteTargetEvent event = new OutlinePasteTargetEvent(viewer);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    event.setTransferData(clipboard.getContents(null));
    event.setPage(page);

    boolean res = false;
    for (IPasteTargetDelegator del : CopyAndPasteExtensionPoint.getPasteTargetDelegators()) {
      res |= del.performPaste(event);
    }
    return res;
  }

  public static boolean performCopy(TreeViewer viewer, AbstractPage page) {

    OutlineCopySourceEvent event = new OutlineCopySourceEvent(viewer);
    event.setPage(page);

    boolean res = false;
    for (ICopySourceDelegator del : CopyAndPasteExtensionPoint.getCopySourceDelegators()) {
      res |= del.performCopy(event);
    }
    return res;
  }
}
