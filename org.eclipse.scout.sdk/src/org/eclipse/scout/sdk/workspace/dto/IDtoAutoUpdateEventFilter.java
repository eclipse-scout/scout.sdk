/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.workspace.dto;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * <h3>{@link IDtoAutoUpdateEventFilter}</h3>
 * 
 * @author mvi
 * @since 3.10.0 07.10.2013
 */
public interface IDtoAutoUpdateEventFilter {
  /**
   * defines if the given compilation unit should be considered to update a DTO.
   * 
   * @param cu
   *          The compilation unit candidate for update.
   * @return true if the event should continue processing.
   * @throws CoreException
   */
  boolean accept(ICompilationUnit cu) throws CoreException;
}
