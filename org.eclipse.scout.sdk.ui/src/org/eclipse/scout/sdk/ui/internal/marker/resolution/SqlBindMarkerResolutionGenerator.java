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
