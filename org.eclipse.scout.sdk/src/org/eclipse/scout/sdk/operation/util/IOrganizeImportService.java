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
package org.eclipse.scout.sdk.operation.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * <h3>{@link IOrganizeImportService}</h3>
 * 
 * @author mvi
 * @since 3.9.0 23.05.2013
 */
public interface IOrganizeImportService {
  void organize(ICompilationUnit cu, IProgressMonitor monitor) throws CoreException;
}
