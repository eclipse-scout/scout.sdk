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
package org.eclipse.scout.sdk.ui.internal.marker.resolution;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.sql.binding.SqlBindingMarkers;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class SqlBindMarkerResolutionGenerator implements IMarkerResolutionGenerator {

  @Override
  public IMarkerResolution[] getResolutions(IMarker marker) {
    try {
      String bindVarAttribute = (String) marker.getAttribute(SqlBindingMarkers.BIND_VARIABLE);
      if (!StringUtility.isNullOrEmpty(bindVarAttribute)) {
        String[] binds = bindVarAttribute.split(",");
        return new IMarkerResolution[]{new SqlBindMarkerResolution(binds)};
      }
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("could not get resolutions", e);
    }
    return new IMarkerResolution[0];
  }

}
