package org.eclipse.scout.sdk.util;

import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

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

  public static void createFile(IFile file, InputStream in, IProgressMonitor monitor) throws CoreException {
    if (file.exists()) {
      file.setContents(in, true, false, monitor);
    }
    else {
      createFolder(file.getParent());
      file.create(in, true, monitor);
    }
  }
}
