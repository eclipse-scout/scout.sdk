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
package org.eclipse.scout.sdk.ui.classid;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.scout.sdk.classid.ClassIdValidationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

/**
 * <h3>{@link ClassIdDuplicateResolutionGenerator}</h3>
 *
 * @author Matthias Villiger
 * @since 4.0.0 21.05.2014
 */
public class ClassIdDuplicateResolutionGenerator implements IMarkerResolutionGenerator {

  @Override
  public IMarkerResolution[] getResolutions(IMarker marker) {
    try {
      if (marker != null && marker.exists() && ClassIdValidationJob.CLASS_ID_DUPLICATE_MARKER_ID.equals(marker.getType())) {
        Object annot = marker.getAttribute(ClassIdValidationJob.CLASS_ID_ATTR_ANNOTATION);
        if (annot instanceof IAnnotation) {
          IAnnotation annotation = (IAnnotation) annot;
          if (annotation.exists()) {
            return new IMarkerResolution[]{new ClassIdDuplicateResolution(annotation)};
          }
        }
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logError("Unable to calculate possible marker resolutions.", e);
    }
    return new IMarkerResolution[]{};
  }
}
