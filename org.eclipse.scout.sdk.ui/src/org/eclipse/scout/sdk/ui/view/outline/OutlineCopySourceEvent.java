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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.swt.events.TypedEvent;

public class OutlineCopySourceEvent extends TypedEvent {

  private static final long serialVersionUID = 1L;

  private AbstractPage m_page;

  public OutlineCopySourceEvent(TreeViewer viewer) {
    super(viewer);
    widget = viewer.getControl();
    display = viewer.getControl().getDisplay();
  }

  public AbstractPage getPage() {
    return m_page;
  }

  public void setPage(AbstractPage page) {
    m_page = page;
  }
}
