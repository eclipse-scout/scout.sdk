/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.worker;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.ws.jaxws.marker.IMarkerRebuildListener;

public class MarkerQueueManager extends AbstractQueueManager<IMarkerRebuildListener> {

  @Override
  protected void handleRequest(IMarkerRebuildListener request, IProgressMonitor monitor) {
    request.rebuildMarkers();
  }
}
