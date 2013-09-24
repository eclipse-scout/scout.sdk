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
package org.eclipse.scout.sdk.util.resources;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.Document;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.log.ScoutStatus;

/**
 * <h3>{@link ResourceUtility}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2012
 */
@SuppressWarnings("restriction")
public final class ResourceUtility {

  public final static int BUF_SIZE = 8192;

  private ResourceUtility() {
  }

  public static IResource[] getAllResources(IResourceFilter filter) throws CoreException {
    return getAllResources(ResourcesPlugin.getWorkspace().getRoot(), filter);
  }

  public static IResource[] getAllResources(IResource startResource, final IResourceFilter filter) throws CoreException {
    final List<IResource> collector = new LinkedList<IResource>();
    startResource.accept(new IResourceProxyVisitor() {
      @Override
      public boolean visit(IResourceProxy proxy) throws CoreException {
        if (proxy.isAccessible()) {
          if (filter.accept(proxy)) {
            collector.add(proxy.requestResource());
          }
          return true;
        }
        return false;
      }
    }, IResource.NONE);

    return collector.toArray(new IResource[collector.size()]);
  }

  /**
   * Tries to open the given url in the system default browser.
   * 
   * @param url
   *          the url to show
   */
  public static void showUrlInBrowser(String url) {
    try {
      if (Desktop.isDesktopSupported()) {
        Desktop d = Desktop.getDesktop();
        if (d.isSupported(Desktop.Action.BROWSE)) {
          d.browse(new URI(url));
        }
      }
    }
    catch (Throwable e) {
      SdkUtilActivator.logWarning("Could not open web browser. ", e);
    }
  }

  public static boolean exists(IResource resource) {
    return resource != null && resource.exists();
  }

  /**
   * Returns this document's default line delimiter.
   * <p>
   * This default line delimiter should be used by clients who want unique delimiters (e.g. 'CR's) in the document.
   * </p>
   * 
   * @return the default line delimiter or <code>null</code> if none.
   */
  public static String getLineSeparator(Document doc) {
    if (doc != null) {
      String delim = doc.getDefaultLineDelimiter();
      if (delim != null) {
        return delim;
      }
    }
    return getLineSeparator();
  }

  /**
   * Gets the content of the given file as string.
   * 
   * @param f
   *          the file to get the content from
   * @return a string containing the content of the given file
   */
  public static String getContent(IFile f) throws CoreException {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(f.getContents()));
      StringBuilder sb = new StringBuilder();
      String line = null;
      String nl = getLineSeparator(f);
      while ((line = reader.readLine()) != null) {
        sb.append(line);
        sb.append(nl);
      }
      return sb.toString();
    }
    catch (IOException e) {
      throw new CoreException(new ScoutStatus(e));
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException e) {
          //nop
        }
      }
    }
  }

  /**
   * gets the install location of the running eclipse.
   * 
   * @return the location or null if no location could be found.
   */
  public static File getEclipseInstallLocation() {
    Location l = Platform.getInstallLocation();
    File ret = null;
    if (l != null) {
      ret = new File(l.getURL().getPath());
    }
    return ret;
  }

  /**
   * Adds all files below the given baseDir into the zip stream.
   * 
   * @param baseDir
   *          The base dir. All files (recursively) in this folder will be added to the zip.
   * @param zOut
   *          The zip where the files will be added.
   * @throws IOException
   */
  public static void addFolderToZip(File baseDir, ZipOutputStream zOut) throws IOException {
    addFolderToZipRec(baseDir, baseDir, zOut);
  }

  private static void addFolderToZipRec(File baseDir, File file, ZipOutputStream zOut) throws IOException {
    if ((!file.exists()) || (!file.isDirectory())) {
      throw new IOException("source directory " + file + " does not exist or is not a folder");
    }
    for (File f : file.listFiles()) {
      if (f.exists() && (!f.isHidden())) {
        if (f.isDirectory()) {
          addFolderToZipRec(baseDir, f, zOut);
        }
        else {
          String name = f.getAbsolutePath();
          String prefix = baseDir.getAbsolutePath();
          if (prefix.endsWith("/") || prefix.endsWith("\\")) {
            prefix = prefix.substring(0, prefix.length() - 1);
          }
          name = name.substring(prefix.length() + 1);
          name = name.replace('\\', '/');

          zOut.putNextEntry(new ZipEntry(name));
          copy(f, zOut);
          zOut.closeEntry();
        }
      }
    }
  }

  /**
   * returns the line separator defined in preference {@link org.eclipse.core.runtime.Platform#PREF_LINE_SEPARATOR} on
   * the workspace.
   * If this is null, returns the system line separator.
   * 
   * @return The line separator to use.
   */
  public static String getLineSeparator() {
    return Util.getLineSeparator(null, null);
  }

  /**
   * returns the line separator defined in preference {@link org.eclipse.core.runtime.Platform#PREF_LINE_SEPARATOR} on
   * the project or workspace of the given resource.
   * If this is null, returns the platform separator.
   * 
   * @return The line separator to use.
   */
  public static String getLineSeparator(IResource r) {
    if (r != null) {
      IScopeContext[] scopeContext = new IScopeContext[]{new ProjectScope(r.getProject())};
      String lineSeparator = Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null, scopeContext);
      if (lineSeparator != null) return lineSeparator;
    }
    return getLineSeparator();
  }

  /**
   * Finds and returns the recommended line separator for this element.
   * The element's buffer is first searched and the first line separator in this buffer is returned if any.
   * Otherwise the preference {@link org.eclipse.core.runtime.Platform#PREF_LINE_SEPARATOR} on this element's project or
   * workspace is returned.
   * Finally if no such preference is set, the system line separator is returned.
   * 
   * @return the recommended line separator for this element
   */
  public static String getLineSeparator(IOpenable o) {
    try {
      return o.findRecommendedLineSeparator();
    }
    catch (JavaModelException e) {
      return getLineSeparator();
    }
  }

  /**
   * copies all data from the given file to the output stream
   * 
   * @param from
   *          source file
   * @param to
   *          data target
   * @throws IOException
   */
  public static void copy(File from, OutputStream to) throws IOException {
    InputStream input = null;
    try {
      input = new BufferedInputStream(new FileInputStream(from), BUF_SIZE);
      copy(input, to);
    }
    finally {
      if (input != null) {
        try {
          input.close();
        }
        catch (IOException e) {
        }
      }
    }
  }

  /**
   * recursively creates all parent directories of the given file
   * 
   * @param toCreate
   *          The file for which the parent directories should be created.
   * @param monitor
   * @throws CoreException
   */
  public static void mkdirs(IFile toCreate, IProgressMonitor monitor) throws CoreException {
    if (toCreate == null || toCreate.exists()) {
      return;
    }
    mkdirs(toCreate.getParent(), monitor);
  }

  /**
   * create the given directory and all of its parents.
   * 
   * @param toCreate
   *          the directory to create.
   * @param monitor
   * @throws CoreException
   */
  public static void mkdirs(IContainer toCreate, IProgressMonitor monitor) throws CoreException {
    if (toCreate == null || toCreate.exists()) {
      return;
    }
    else {
      IContainer parent = toCreate.getParent();
      if (parent instanceof IFolder) {
        mkdirs(parent, monitor);
      }
      if (toCreate instanceof IFolder) {
        ((IFolder) toCreate).create(true, true, monitor);
      }
    }
  }

  /**
   * move the given file into the given folder.
   * 
   * @param from
   *          the file to move
   * @param destFolder
   *          the destination folder.
   * @throws IOException
   */
  public static void moveFile(File from, File destFolder) throws IOException {
    if (from == null || !from.isFile()) {
      throw new IOException("source file is not valid");
    }
    if (destFolder == null) {
      throw new IOException("destination folder is not valid");
    }
    destFolder.mkdirs();
    boolean success = from.renameTo(destFolder);
    if (!success) {
      // fallback: copy file
      FileUtility.copyFile(from, new File(destFolder, from.getName()));
      IOUtility.deleteFile(from.getAbsolutePath());
    }
  }

  public static void extractZip(File zipFile, File destinationFolder) throws IOException {
    ZipInputStream zipInputStream = null;
    ZipEntry zipEntry;
    try {
      zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
      zipEntry = zipInputStream.getNextEntry();
      while (zipEntry != null) {
        if (!zipEntry.isDirectory()) {
          String entryName = zipEntry.getName();

          File destFile = new File(destinationFolder, entryName);
          destFile.getParentFile().mkdirs();
          OutputStream fos = null;
          try {
            fos = new BufferedOutputStream(new FileOutputStream(destFile));
            copy(zipInputStream, fos);
          }
          finally {
            if (fos != null) {
              try {
                fos.close();
              }
              catch (Exception e) {
              }
            }
          }
        }
        zipInputStream.closeEntry();
        zipEntry = zipInputStream.getNextEntry();
      }
    }
    finally {
      if (zipInputStream != null) {
        try {
          zipInputStream.close();
        }
        catch (Exception e) {
        }
      }
    }
  }

  /**
   * copies all data from the input stream to the output stream.
   * 
   * @param from
   *          data source
   * @param to
   *          data target
   * @throws IOException
   */
  public static void copy(InputStream from, OutputStream to) throws IOException {
    copy(from, to, BUF_SIZE);
  }

  /**
   * copies all data from the input stream to the output stream.
   * 
   * @param from
   *          data source
   * @param to
   *          data target
   * @param bufferSize
   *          buffer size to use
   * @throws IOException
   */
  public static void copy(InputStream from, OutputStream to, int bufferSize) throws IOException {
    byte[] b = new byte[bufferSize];
    int len;
    while ((len = from.read(b, 0, b.length)) != -1) {
      to.write(b, 0, len);
    }
    to.flush();
  }
}
