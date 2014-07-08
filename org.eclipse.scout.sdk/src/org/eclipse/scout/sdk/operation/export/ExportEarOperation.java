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
package org.eclipse.scout.sdk.operation.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class ExportEarOperation implements IOperation {

  public static final String META_INF = "META-INF";

  private final ArrayList<File> m_modules;
  private String m_earFileName;
  private File m_tempBuildDir;
  private File m_createdEarFile;

  public ExportEarOperation() {
    m_modules = new ArrayList<File>();
  }

  @Override
  public String getOperationName() {
    return "Create EAR";
  }

  @Override
  public void validate() {
    if (m_modules.size() == 0) {
      throw new IllegalArgumentException("At least one EAR module must be specified.");
    }
    if (StringUtility.isNullOrEmpty(getEarFileName())) {
      throw new IllegalArgumentException("EAR file name cannot be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      m_tempBuildDir = IOUtility.createTempDirectory("earPackagerBuildDir");

      // modules
      StringBuilder moduleXml = new StringBuilder();
      for (File module : m_modules) {
        String fileName = module.getName();
        if (fileName.toLowerCase().endsWith(".war")) {
          String context = cutExtension(fileName);

          moduleXml.append("  <module><web><web-uri>");
          moduleXml.append(fileName);
          moduleXml.append("</web-uri><context-root>/");
          moduleXml.append(context);
          moduleXml.append("</context-root></web></module>\n");
        }
        installFile(module.toURI().toURL(), fileName);
      }

      // meta data
      HashMap<String, String> props = new HashMap<String, String>();
      props.put("DISPLAY_NAME", cutExtension(new File(getEarFileName()).getName()));
      props.put("MODULE_LIST", moduleXml.toString());
      installTextFile("platform:/plugin/" + ScoutSdk.PLUGIN_ID + "/templates/ear/application.xml", META_INF + "/application.xml", props);
      installTextFile("platform:/plugin/" + ScoutSdk.PLUGIN_ID + "/templates/ear/MANIFEST.MF", META_INF + "/MANIFEST.MF", props);

      // pack ear
      m_createdEarFile = packEar();
    }
    catch (CoreException e) {
      throw e;
    }
    catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not ear file", e));
    }
    finally {
      cleanUp();
    }
  }

  private File packEar() throws IOException {
    File destinationFile = new File(getEarFileName());
    if (destinationFile.exists()) {
      destinationFile.delete();
    }
    else {
      File folder = destinationFile.getParentFile();
      if (!folder.exists() && !folder.mkdirs()) {
        throw new IOException("Unable to create file directory '" + destinationFile.getParentFile().getAbsolutePath() + "'.");
      }
    }

    ZipOutputStream zipOut = null;
    try {
      zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destinationFile)));
      ResourceUtility.addFolderToZip(m_tempBuildDir, zipOut);
      zipOut.flush();
    }
    finally {
      if (zipOut != null) {
        zipOut.close();
      }
    }
    return destinationFile;
  }

  private void cleanUp() {
    for (File module : m_modules) {
      if (module.exists()) {
        module.delete();
      }
    }
    IOUtility.deleteDirectory(m_tempBuildDir);
  }

  private String cutExtension(String fileName) {
    if (fileName == null || fileName.length() < 1) return fileName;
    int pos = fileName.lastIndexOf('.');
    if (pos > 0) {
      fileName = fileName.substring(0, pos);
    }
    return fileName;
  }

  @SuppressWarnings("resource")
  private File installTextFile(String platformUrl, String filePath, Map<String, String> replacements) throws IOException, ProcessingException, CoreException, URISyntaxException {
    URL absSourceUrl = FileLocator.resolve(URIUtil.toURL(URIUtil.fromString(platformUrl)));
    String s = new String(IOUtility.getContent(absSourceUrl.openStream()), "UTF-8");

    if (replacements != null) {
      for (Map.Entry<String, String> e : replacements.entrySet()) {
        s = s.replace("@@" + e.getKey() + "@@", e.getValue());
      }
    }

    // check that all variables have been substituted
    Matcher m = Pattern.compile("@@([^@]+)@@").matcher(s);
    if (m.find()) {
      throw new CoreException(new ScoutStatus("Missing tag replacement for tag " + m.group(1) + " in template " + platformUrl));
    }

    File destFile = getDestinationFile(filePath);
    if (destFile.exists()) {
      destFile.delete();
    }
    else {
      File folder = destFile.getParentFile();
      if (!folder.exists() && !folder.mkdirs()) {
        throw new IOException("Unable to create file directory '" + destFile.getParentFile().getAbsolutePath() + "'.");
      }
    }

    IOUtility.writeContent(new FileWriter(destFile), s, true);
    return destFile;
  }

  private File installFile(URL platformUrl, String filePath) throws IOException {
    URL absSourceUrl = FileLocator.resolve(platformUrl);
    InputStream in = null;
    OutputStream out = null;
    try {
      in = absSourceUrl.openStream();
      File destFile = getDestinationFile(filePath);
      if (destFile.exists()) {
        destFile.delete();
      }
      else {
        File folder = destFile.getParentFile();
        if (!folder.exists() && !folder.mkdirs()) {
          throw new IOException("Unable to create file directory '" + destFile.getParentFile().getAbsolutePath() + "'.");
        }
      }
      out = new FileOutputStream(destFile);
      ResourceUtility.copy(in, out);
      return destFile;
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

  private File getDestinationFile(String relPath) {
    return new File(m_tempBuildDir + File.separator + relPath.replaceAll("\\\\\\/$", ""));
  }

  public void addModule(File... modules) {
    for (File module : modules) {
      m_modules.add(module);
    }
  }

  public void setEarFileName(String earFileName) {
    m_earFileName = earFileName;
  }

  public String getEarFileName() {
    return m_earFileName;
  }

  public File getCreatedEarFile() {
    return m_createdEarFile;
  }
}
