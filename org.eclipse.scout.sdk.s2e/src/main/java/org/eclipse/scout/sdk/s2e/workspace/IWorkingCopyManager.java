/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.workspace;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * <h3>{@link IWorkingCopyManager}</h3>
 *
 * @author Matthias Villiger
 * @since 3.6.0 16.05.2014
 */
public interface IWorkingCopyManager {

  /**
   * Register compilation unit BEFORE doing changes on it. Creates a working copy on the first registration. Compilation
   * unit may be registered multiple times
   *
   * @param icu
   *          The compilation unit to register
   * @param monitor
   *          a progress monitor used to report progress while opening this compilation unit or null if no progress
   *          should be reported
   * @return true if the given compilation unit was newly registered with this call, false if it was already registered
   * @throws JavaModelException
   *           if the compilation unit could not become a working copy
   * @throws if
   *           the given compilation unit is read only.
   */
  boolean register(ICompilationUnit icu, IProgressMonitor monitor) throws JavaModelException;

  /**
   * unregisters all working copies managed by this instance.
   *
   * @param monitor
   *          a progress monitor used to report progress while opening this compilation unit or null if no progress
   *          should be reported
   * @param save
   *          Specifies if the {@link ICompilationUnit}s managed by this {@link IWorkingCopyManager} should be commited
   *          or just discarded.
   * @throws JavaModelException
   */
  void unregisterAll(IProgressMonitor monitor, boolean save);

  /**
   * When doing direct source changes on the compilation unit, a reconcile is required to fire element change deltas.
   * This is not required if changes are done using for example
   * {@link IType#createMethod(String, IJavaElement, boolean, IProgressMonitor)} or
   * {@link IType#createField(String, IJavaElement, boolean, IProgressMonitor)}
   *
   * @param icu
   *          The compilation unit to reconcile.
   * @param monitor
   *          a progress monitor used to report progress while opening this compilation unit or null if no progress
   *          should be reported
   * @throws CoreException
   *           if the contents of the original element cannot be accessed. Reasons include: The original Java element
   *           does not exist or the given compilation unit is not registered in this manager.
   */
  void reconcile(ICompilationUnit icu, IProgressMonitor monitor) throws CoreException;

}
