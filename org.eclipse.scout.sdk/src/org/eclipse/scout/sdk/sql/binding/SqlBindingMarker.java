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
package org.eclipse.scout.sdk.sql.binding;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.sql.binding.MethodSqlBindingModel.Marker;

/**
 * <h3>{@link SqlBindingMarker}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 28.02.2011
 */
public class SqlBindingMarker {
  public static String ID = "org.eclipse.scout.sdk.formdata.sql.binding";
  public static String BIND_VARIABLE = "bindVariable";

  public static void removeMarkers(ICompilationUnit icu) {
    IResource resource = icu.getResource();
    try {
      IMarker[] existingMarkers = resource.findMarkers(ID, true, IResource.DEPTH_ONE);
      for (IMarker m : existingMarkers) {
        m.delete();
      }
    }
    catch (CoreException e) {
      ScoutSdk.logError("could not remove sql bind markers of '" + icu.getElementName() + "'.", e);
    }
  }

  public static void setMarkers(MethodSqlBindingModel model) {
    IMethod method = model.getMethod();
    try {
      IResource resource = method.getResource();
      for (Marker markerModel : model.getMissingMarkers()) {
        IMarker marker = resource.createMarker(ID);
        StringBuilder text = new StringBuilder("Binding '");
        text.append(markerModel.getBindVariable());
        text.append("' could not be resolved");
        if (markerModel.getSeverity() == IMarker.SEVERITY_WARNING) {
          text.append(" (has unresolved bindings)");
        }
        text.append(".");
        marker.setAttribute(IMarker.MESSAGE, text.toString());
        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
        marker.setAttribute(IMarker.CHAR_START, markerModel.getOffset());
        marker.setAttribute(IMarker.CHAR_END, markerModel.getOffset() + markerModel.getLength());
        marker.setAttribute(IMarker.SEVERITY, markerModel.getSeverity());
        marker.setAttribute(BIND_VARIABLE, markerModel.getBindVariable());

      }
    }
    catch (CoreException e) {
      ScoutSdk.logError("could not create sql bind markers of '" + method.getElementName() + "' on '" + method.getDeclaringType().getFullyQualifiedName() + "'.", e);
    }
  }
}
