/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.operation;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;

/**
 * <h3>{@link IFileWriteOperation}</h3> Represents an {@link IOperation} that writes {@link IFile}s.
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public interface IFileWriteOperation extends IOperation {

  /**
   * @return The innermost existing {@link IResource} that will be changed when executing this
   *         {@link IFileWriteOperation}. If the result is an {@link IContainer} then a new {@link IFile} or
   *         {@link IFolder} will be created in it. If the result is an {@link IFile} this file will be updated with the
   *         new content.
   */
  IResource getAffectedResource();

  /**
   * @return The {@link IFile} that has been modified or created.
   */
  IFile getFile();
}