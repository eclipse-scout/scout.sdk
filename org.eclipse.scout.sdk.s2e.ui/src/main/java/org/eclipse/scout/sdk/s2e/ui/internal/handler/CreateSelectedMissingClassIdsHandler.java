/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.handler;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.classid.MissingClassIdsNewOperation;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <h3>{@link CreateSelectedMissingClassIdsHandler}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class CreateSelectedMissingClassIdsHandler extends AbstractHandler {
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    final Set<IResource> resourcesFromSelection = S2eUiUtils.getResourcesOfSelection(HandlerUtil.getCurrentSelection(event));
    if (resourcesFromSelection.isEmpty()) {
      SdkLog.warning("Cannot create missing @ClassIds in the selected scope because no resources are selected.");
      return null;
    }

    final IOperation operation = new MissingClassIdsNewOperation()
        .withSelection(resourcesFromSelection);

    new ResourceBlockingOperationJob(operation, resourcesFromSelection.toArray(new IResource[resourcesFromSelection.size()])).schedule();
    return null;
  }
}
