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
package org.eclipse.scout.sdk.ws.jaxws.util;

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

/**
 * Abstraction for different files, e.g. Eclipse-{@link IFile} or Java-{@link File}.
 * 
 * @param <T>
 */
public interface IFileHandle<T> {

  /**
   * @return a handle that represents the containing folder.
   */
  IFileHandle<T> getParent();

  /**
   * @return <code>true</code> if the resource exists, <code>false</code> otherwise.
   */
  boolean exists();

  /**
   * @return {@link InputStream} of the file resource.
   */
  InputStream getInputStream();

  /**
   * @return the name of the file resource.
   */
  String getName();

  /**
   * @return the absolute path in the local file system to this resource, or <code>null</code> if no path can be
   *         determined.
   */
  IPath getFullPath();

  /**
   * @param path
   *          the relative path to the resource.
   * @return a handle to the resource or <code>null</code> if not found.
   */
  IFileHandle<T> getChild(IPath path);

  /**
   * @return the resource wrapped by this handle.
   */
  T getFile();
}
