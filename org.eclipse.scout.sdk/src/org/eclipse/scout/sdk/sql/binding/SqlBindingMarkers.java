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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.sql.binding.MethodSqlBindingModel.Marker;
import org.eclipse.scout.sdk.sql.binding.MethodSqlBindingModel.SQLStatement;

/**
 * <h3>{@link SqlBindingMarkers}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 28.02.2011
 */
public class SqlBindingMarkers {
  public static String MARKER_ID = "org.eclipse.scout.sdk.formdata.sql.binding";
  public static String MAKRER_ID_SINGLE = "org.eclipse.scout.sdk.formdata.sql.binding.single";
  public static String MARKER_ID_MULTI = "org.eclipse.scout.sdk.formdata.sql.binding.multi";
  public static String BIND_VARIABLE = "bindVariable";

  public static void removeMarkers(IResource resource) {
    try {
      if (resource != null) {
        resource.deleteMarkers(MARKER_ID, true, IResource.DEPTH_ZERO);
      }
    }
    catch (CoreException e) {
      ScoutSdk.logError("could not remove sql bind markers of '" + resource.getName() + "'.", e);
    }
  }

  public static void setMarkers(MethodSqlBindingModel model) {
    if (model.hasMarkers()) {
      for (SQLStatement statement : model.getStatements()) {
        setMarkers(statement);
      }
    }
  }

  private static void setMarkers(SQLStatement statement) {
    if (statement.hasMarkers()) {
      IMethod method = statement.getDeclaringMethod();
      try {
        IResource resource = method.getResource();

        Marker[] markers = statement.getMarkers();
        for (Marker markerModel : markers) {
          IMarker marker = resource.createMarker(MAKRER_ID_SINGLE);
          StringBuilder text = new StringBuilder("Binding '");
          text.append(markerModel.getBindVariable());
          text.append("' could not be resolved");
          if (markerModel.getSeverity() == IMarker.SEVERITY_WARNING) {
            text.append(" (has unresolved bindings)");
          }
          text.append(".");
          marker.setAttribute(IMarker.MESSAGE, text.toString());
          marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
          marker.setAttribute(IMarker.CHAR_START, statement.getOffset());
          marker.setAttribute(IMarker.CHAR_END, statement.getOffset() + statement.getLength());
          marker.setAttribute(IMarker.SEVERITY, markerModel.getSeverity());
          marker.setAttribute(BIND_VARIABLE, markerModel.getBindVariable());
        }
        if (markers.length > 1) {
          IMarker marker = resource.createMarker(MARKER_ID_MULTI);
          StringBuilder bindVarBuilder = new StringBuilder();
          for (int i = 0; i < markers.length; i++) {
            bindVarBuilder.append(markers[i].getBindVariable());
            if (i < markers.length - 1) {
              bindVarBuilder.append(",");
            }
          }
          marker.setAttribute(BIND_VARIABLE, bindVarBuilder.toString());
//          StringBuilder text = new StringBuilder("Binding '");
//          text.append(markerModel.getBindVariable());
//          text.append("' could not be resolved");
//          if (markerModel.getSeverity() == IMarker.SEVERITY_WARNING) {
//            text.append(" (has unresolved bindings)");
//          }
//          text.append(".");
//          marker.setAttribute(IMarker.MESSAGE, text.toString());
          marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
          marker.setAttribute(IMarker.CHAR_START, statement.getOffset());
          marker.setAttribute(IMarker.CHAR_END, statement.getOffset() + statement.getLength());
//          marker.setAttribute(IMarker.SEVERITY, markerModel.getSeverity());
//          marker.setAttribute(BIND_VARIABLE, markerModel.getBindVariable());
        }
      }

      catch (CoreException e) {
        ScoutSdk.logError("could not create sql bind markers of '" + method.getElementName() + "' on '" + method.getDeclaringType().getFullyQualifiedName() + "'.", e);
      }
    }
  }
}
