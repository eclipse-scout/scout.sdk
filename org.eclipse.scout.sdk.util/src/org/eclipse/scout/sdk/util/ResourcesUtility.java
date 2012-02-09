package org.eclipse.scout.sdk.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.Document;

@SuppressWarnings("restriction")
public final class ResourcesUtility {

  private final static int BUF_SIZE = 8192;

  private ResourcesUtility() {
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

  /**
   * creates a folder recursively
   * 
   * @param folder
   * @throws CoreException
   */
  public static void createFolder(IContainer folder) throws CoreException {
    if (!folder.exists()) {
      IContainer parent = folder.getParent();
      if (parent instanceof IFolder) {
        createFolder(parent);
      }
      if (folder instanceof IFolder) {
        ((IFolder) folder).create(true, true, null);
      }
    }
  }
}
