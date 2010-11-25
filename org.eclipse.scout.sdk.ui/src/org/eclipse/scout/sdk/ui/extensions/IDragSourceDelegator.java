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
package org.eclipse.scout.sdk.ui.extensions;

import java.util.EventListener;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;

/**
 * <h3>IDragSourceDelegator</h3> This class is registered by the extension //TODO
 */
public interface IDragSourceDelegator extends EventListener {

  /**
   * will be called of the Outline. If returned true the {@link IDragSourceDelegator#dragSetData(DragSourceEvent)} and
   * {@link IDragSourceDelegator#dragFinished(DragSourceEvent)} method of this delegater will be called. If false
   * is returned this delegator will not be considered during the current drag and drop event.
   * 
   * @param event
   * @return
   */
  boolean acceptDrag(DragSourceEvent event, TreeViewer outline);

  /**
   * the method sets the data of the event. {@link DragSourceEvent#data}. Aware the data is an instance of a
   * {@link LocalSelectionTransfer}.
   * 
   * @param event
   */
  public void dragSetData(DragSourceEvent event, TreeViewer outline);

  /**
   * use this method to clean up some dragged items. The {@link DragSourceEvent#detail} gives information about the
   * drag type see {@link DND#DROP_COPY}...
   * 
   * @param event
   */
  public void dragFinished(DragSourceEvent event, TreeViewer outline);
}
