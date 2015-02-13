/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.marker;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants.MarkerType;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.IMarkerCommand;

public final class MarkerUtility {

  private MarkerUtility() {
  }

  public static int getQuality(IPage page, IScoutBundle bundle, String groupUUID) {
    int quality = IMarker.SEVERITY_INFO;

    for (IMarker marker : getMarkers(bundle, MarkerType.JaxWs, groupUUID)) {
      quality = Math.max(quality, marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO));
      if (quality == IMarker.SEVERITY_ERROR) {
        return quality;
      }
    }

    if (page.hasChildren()) {
      IPage[] a = page.getChildArray();
      for (int i = 0; i < a.length; i++) {
        quality = Math.max(quality, a[i].getQuality());
        if (quality == IMarker.SEVERITY_ERROR) {
          break;
        }
      }
      return quality;
    }
    else {
      return quality;
    }
  }

  public static void clearMarkers(IScoutBundle bundle, String groupUUID) {
    JaxWsSdk.getDefault().removeMarkerCommands(groupUUID);
    try {
      for (IMarker marker : getMarkers(bundle, MarkerType.JaxWs, groupUUID)) {
        marker.delete();
      }
    }
    catch (CoreException e) {
      JaxWsSdk.logError("Could not delete markers", e);
    }
  }

  public static void deleteMarkers(IScoutBundle bundle, MarkerType markerType, String groupUUID) {
    try {
      for (IMarker marker : getMarkers(bundle, markerType, groupUUID)) {
        marker.delete();
        String markerSourceId = marker.getAttribute(IMarker.SOURCE_ID, null);
        if (markerSourceId != null) {
          JaxWsSdk.getDefault().removeMarkerCommand(markerSourceId);
        }
      }
    }
    catch (CoreException e) {
      JaxWsSdk.logError("Could not delete markers", e);
    }
  }

  public static String createMarker(IResource resource, String groupUUID, String message) {
    return createMarker(resource, MarkerType.JaxWs, groupUUID, message);
  }

  public static String createMarker(IResource resource, MarkerType markerType, String groupUUID, String message) {
    return createMarker(resource, markerType, groupUUID, IMarker.SEVERITY_ERROR, message);
  }

  public static String createMarker(IResource resource, MarkerType markerType, String groupUUID, int severity, String message) {
    if (resource != null && resource.exists()) {
      try {
        IMarker marker = resource.createMarker(markerType.getId());
        marker.setAttribute(IMarker.MESSAGE, message);
        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
        marker.setAttribute(IMarker.SEVERITY, severity);
        marker.setAttribute(IMarker.TRANSIENT, true);
        String sourceId = StringUtility.join(" ", groupUUID, UUID.randomUUID().toString());
        marker.setAttribute(IMarker.SOURCE_ID, sourceId);
        return sourceId;
      }
      catch (Exception e) {
        JaxWsSdk.logError("could not create marker", e);
      }
    }
    return null;
  }

  public static boolean containsMarker(IScoutBundle bundle, MarkerType markerType, String groupUUID, int minimumSeverity) {
    IMarker[] markers = getMarkers(bundle, markerType, groupUUID);
    for (IMarker marker : markers) {
      if (marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO) >= minimumSeverity) {
        return true;
      }
    }
    return false;
  }

  public static IMarker[] getMarkers(IScoutBundle bundle, MarkerType markerType, String groupUUID) {
    Set<IMarker> markers = new HashSet<>();

    try {
      IMarker[] candidates = bundle.getJavaProject().getResource().findMarkers(markerType.getId(), true, IResource.DEPTH_INFINITE);
      for (IMarker candidate : candidates) {
        if (groupUUID == null) {
          markers.add(candidate);
        }
        else if (candidate.getAttribute(IMarker.SOURCE_ID, "").startsWith(groupUUID)) {
          markers.add(candidate);
        }
      }
    }
    catch (Exception e) {
      JaxWsSdk.logError("Could not retrieve markers", e);
    }
    return markers.toArray(new IMarker[markers.size()]);
  }

  public static IMarkerCommand[] getMarkerCommands(String markerGroupUUID, IScoutBundle bundle) {
    List<IMarkerCommand> commands = new LinkedList<>();
    IMarker[] markers = MarkerUtility.getMarkers(bundle, MarkerType.JaxWs, markerGroupUUID);
    for (IMarker marker : markers) {
      String sourceId = marker.getAttribute(IMarker.SOURCE_ID, null);
      if (sourceId == null) {
        continue;
      }
      IMarkerCommand markerCommand = JaxWsSdk.getDefault().getMarkerCommand(sourceId);
      if (markerCommand != null) {
        markerCommand.setMarker(marker);
        commands.add(markerCommand);
      }
    }
    return commands.toArray(new IMarkerCommand[commands.size()]);
  }
}
