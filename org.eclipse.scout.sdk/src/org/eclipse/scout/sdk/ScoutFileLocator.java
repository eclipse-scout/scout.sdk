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
package org.eclipse.scout.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

/**
 * <h3>{@link ScoutFileLocator}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 21.12.2010
 */
public class ScoutFileLocator {

  public static InputStream resolve(String bundleID, String path) {
    InputStream stream = null;
    try {
      stream = resolveWs(bundleID, path);
    }
    catch (Exception e) {
      ScoutSdk.logWarning(e);
    }
    if (stream == null) {
      try {
        stream = resolvePlatform(bundleID, path);
      }
      catch (IOException e) {
      }
    }
    return stream;
  }

  private static InputStream resolveWs(String bundleID, String path) throws URISyntaxException, CoreException {
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(bundleID);
    IFile file = project.getFile(new Path(path));
    if (file != null && file.exists()) {
      return file.getContents();
    }
    return null;
  }

  private static InputStream resolvePlatform(String bundleId, String path) throws IOException {
    URL url = FileLocator.resolve(new URL("platform:/plugin/" + bundleId + "/" + path));
    if (url != null) {
      return url.openStream();
    }
    return null;
  }
}
