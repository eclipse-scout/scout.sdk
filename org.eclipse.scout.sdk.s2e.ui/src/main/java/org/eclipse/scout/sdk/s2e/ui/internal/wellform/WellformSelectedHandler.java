/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.wellform;

import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.s2e.operation.wellform.WellformScoutTypeOperation;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;

/**
 * <h3>{@link WellformSelectedHandler}</h3>
 *
 * @since 5.1.0
 */
public class WellformSelectedHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) {
    var types = S2eUiUtils.getTypesInEventSelection(event);
    if (types.isEmpty()) {
      logNoSelection();
      return null;
    }

    var rules = types.stream()
        .map(IJavaElement::getResource)
        .distinct()
        .toArray(ISchedulingRule[]::new);
    ISchedulingRule rule = new MultiRule(rules);
    runInEclipseEnvironment(new WellformScoutTypeOperation(types, true), rule);
    return null;
  }

  private static void logNoSelection() {
    SdkLog.warning("Cannot wellform classes in the selected scope because no classes are selected.");
  }
}
