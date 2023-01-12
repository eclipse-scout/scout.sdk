/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.classid;

import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;

import java.util.function.BiConsumer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.s2e.classid.MissingClassIdsNewOperation;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <h3>{@link CreateSelectedMissingClassIdsHandler}</h3>
 *
 * @since 5.2.0
 */
public class CreateSelectedMissingClassIdsHandler extends AbstractHandler {
  @Override
  public Object execute(ExecutionEvent event) {
    var resourcesFromSelection = S2eUiUtils.getResourcesOfSelection(HandlerUtil.getCurrentSelection(event));
    if (resourcesFromSelection.isEmpty()) {
      SdkLog.warning("Cannot create missing @ClassIds in the selected scope because no resources are selected.");
      return null;
    }

    BiConsumer<EclipseEnvironment, EclipseProgress> operation = new MissingClassIdsNewOperation()
        .withSelection(resourcesFromSelection);
    ISchedulingRule rule = new MultiRule(resourcesFromSelection.toArray(new IResource[0]));
    runInEclipseEnvironment(operation, rule);
    return null;
  }
}
