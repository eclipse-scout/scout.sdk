package org.eclipse.scout.sdk.util;

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
