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

import org.eclipse.scout.sdk.ui.view.outline.OutlineDropTargetEvent;

public interface IDropTargetDelegator extends EventListener {

  boolean validateDrop(OutlineDropTargetEvent event);

  boolean performDrop(OutlineDropTargetEvent event);

  boolean expand(OutlineDropTargetEvent event);

}
