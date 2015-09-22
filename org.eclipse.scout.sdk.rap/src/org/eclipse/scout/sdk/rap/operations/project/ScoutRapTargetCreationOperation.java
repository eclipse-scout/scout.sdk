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
package org.eclipse.scout.sdk.rap.operations.project;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.rap.IScoutSdkRapConstants;
import org.eclipse.scout.sdk.rap.ScoutSdkRap;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.osgi.framework.Bundle;

/**
 * <h3>{@link ScoutRapTargetCreationOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 3.8.0 20.03.2012
 */
public class ScoutRapTargetCreationOperation implements IOperation {

  private static final String SCOUT_RAP_TARGET_PLUGIN_SUB_DIR = "resources/org.eclipse.scout.rt.rap.target.repo";
  private final Bundle m_sourcePlugin;
  private File m_destinationDir;

  public ScoutRapTargetCreationOperation() {
    m_sourcePlugin = Platform.getBundle(IScoutSdkRapConstants.ScoutRapTargetPlugin);
  }

  @Override
  public String getOperationName() {
    return "Creating new Scout RAP target in directory '" + getDestinationDirectory() + "'.";
  }

  @Override
  public void validate() {
    if (getDestinationDirectory() == null) {
      throw new IllegalArgumentException("the destination directory can not be null.");
    }
    if (getSourcePlugin() == null) {
      throw new IllegalArgumentException("the plugin '" + IScoutSdkRapConstants.ScoutRapTargetPlugin + "' can not be found.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      final String suffix = ".jar";
      IOUtility.deleteDirectory(getDestinationDirectory());
      Enumeration<URL> urls = getSourcePlugin().findEntries(SCOUT_RAP_TARGET_PLUGIN_SUB_DIR, "*", true);
      if (urls != null) {
        while (urls.hasMoreElements()) {
          URL url = urls.nextElement();
          extract(url);
        }
      }

      File featuresFolder = new File(getDestinationDirectory(), "features");
      if (featuresFolder.exists()) {
        File[] featureJars = featuresFolder.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(suffix);
          }
        });

        for (File jar : featureJars) {
          File dest = new File(featuresFolder, jar.getName().substring(0, jar.getName().length() - suffix.length()));
          ResourceUtility.extractZip(jar, dest);
          IOUtility.deleteFile(jar.getAbsolutePath());
        }
      }

      IOUtility.deleteFile(new File(getDestinationDirectory().getAbsolutePath(), "artifacts.jar").getAbsolutePath());
      IOUtility.deleteFile(new File(getDestinationDirectory().getAbsolutePath(), "content.jar").getAbsolutePath());
    }
    catch (IOException e) {
      ScoutSdkRap.logError("Unable to create new Scout RAP target.", e);
    }
  }

  private void extract(URL source) throws IOException {
    InputStream in = null;
    OutputStream out = null;
    try {
      String relPath = source.getFile();
      if (!relPath.endsWith("/")) { // skip directories
        String destRelPath = relPath;
        String prefix = "/" + SCOUT_RAP_TARGET_PLUGIN_SUB_DIR + "/";
        if (destRelPath.startsWith(prefix)) {
          destRelPath = destRelPath.substring(prefix.length());
        }
        File dest = new File(getDestinationDirectory(), destRelPath);
        File destFolder = dest.getParentFile();
        if (!destFolder.exists() && !destFolder.mkdirs()) {
          throw new IOException("Unable to create file directory '" + dest.getParentFile().getAbsolutePath() + "'.");
        }
        out = new BufferedOutputStream(new FileOutputStream(dest), ResourceUtility.BUF_SIZE);
        in = source.openStream();
        ResourceUtility.copy(in, out);
      }
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch (Exception e) {
        }
      }
      if (out != null) {
        try {
          out.close();
        }
        catch (Exception e) {
        }
      }
    }
  }

  public void setDestinationDirectory(File dir) {
    m_destinationDir = dir;
  }

  public File getDestinationDirectory() {
    return m_destinationDir;
  }

  private Bundle getSourcePlugin() {
    return m_sourcePlugin;
  }
}