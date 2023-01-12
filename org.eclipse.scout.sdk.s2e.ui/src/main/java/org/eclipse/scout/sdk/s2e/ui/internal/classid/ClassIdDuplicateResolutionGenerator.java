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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.s2e.classid.ClassIdValidationJob;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

/**
 * <h3>{@link ClassIdDuplicateResolutionGenerator}</h3>
 *
 * @since 4.0.0 2014-05-21
 */
public class ClassIdDuplicateResolutionGenerator implements IMarkerResolutionGenerator {

  private static final IMarkerResolution[] NO_RESOLUTION = new IMarkerResolution[0];

  @Override
  public IMarkerResolution[] getResolutions(IMarker marker) {
    try {
      if (marker != null && marker.exists() && ClassIdValidationJob.CLASS_ID_DUPLICATE_MARKER_ID.equals(marker.getType())) {
        var annot = marker.getAttribute(ClassIdValidationJob.CLASS_ID_ATTR_ANNOTATION);
        if (annot instanceof IAnnotation annotation) {
          if (annotation.exists()) {
            return new IMarkerResolution[]{new ClassIdDuplicateResolution(annotation)};
          }
        }
      }
    }
    catch (CoreException e) {
      SdkLog.error("Unable to calculate possible marker resolutions.", e);
    }
    return NO_RESOLUTION;
  }
}
