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
package org.eclipse.scout.sdk.s2e.ui.internal.dataobject;

import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.dataobject.DoConvenienceMethodsUpdateOperation;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;

/**
 * @since 11.0.0
 */
public class UpdateDoConvenienceMethodsInSelectionHandler extends AbstractHandler {
  @Override
  public Object execute(ExecutionEvent event) {
    var types = S2eUiUtils.getTypesInEventSelection(event);
    if (types.isEmpty()) {
      logNoSelection();
      return null;
    }
    runInEclipseEnvironment((e, p) -> updateDoConvenienceMethods(types, e, p));
    return null;
  }

  private static void updateDoConvenienceMethods(Collection<IType> types, EclipseEnvironment env, IProgress progress) {
    var scoutTypes = types.stream()
        .map(env::toScoutType)
        .collect(toList());
    new DoConvenienceMethodsUpdateOperation().withDataObjects(scoutTypes).accept(env, progress);
  }

  private static void logNoSelection() {
    SdkLog.warning("Cannot update DO convenience methods in the selected scope because no classes are selected.");
  }
}
