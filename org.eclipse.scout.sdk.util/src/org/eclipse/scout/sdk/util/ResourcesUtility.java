package org.eclipse.scout.sdk.util;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

public final class ResourcesUtility {

  private ResourcesUtility() {
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
