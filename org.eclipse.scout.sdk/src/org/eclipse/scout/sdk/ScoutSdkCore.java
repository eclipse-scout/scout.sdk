package org.eclipse.scout.sdk;

import org.eclipse.scout.sdk.internal.workspace.ScoutWorkspace;
import org.eclipse.scout.sdk.workspace.IScoutWorkspace;

public class ScoutSdkCore {
  public static IScoutWorkspace getScoutWorkspace() {
    return ScoutWorkspace.getInstance();
  }
}
