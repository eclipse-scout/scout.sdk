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
package org.eclipse.scout.sdk.s2e.environment;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * <h3>{@link IWorkingCopyManager}</h3>
 * <p>
 * Holds working copies and releases or discards them based on error- and cancellation status of the task running.
 * <p>
 * A {@link IWorkingCopyManager} must only be used once. As soon as {@link #unregisterAll(boolean, IProgressMonitor)
 * unregisterAll} as been called, no more calls to {@link #register(ICompilationUnit, IProgressMonitor) register} or
 * {@link #reconcile(ICompilationUnit, IProgressMonitor) reconcile} are allowed (transaction finished).
 *
 * @since 3.6.0 2014-05-16
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
   *           if the compilation unit could not become a working copy (e.g. if the given compilation unit is read
   *           only).
   * @throws IllegalArgumentException
   *           if {@link #unregisterAll(boolean, IProgressMonitor) unregisterAll} has already been called on this
   *           instance (transaction finished).
   */
  boolean register(ICompilationUnit icu, IProgressMonitor monitor) throws JavaModelException;

  /**
   * unregisters all working copies managed by this instance.
   *
   * @param save
   *          Specifies if the {@link ICompilationUnit}s managed by this {@link IWorkingCopyManager} should be commited
   *          or just discarded.
   * @param monitor
   *          a progress monitor used to report progress while opening this compilation unit or null if no progress
   *          should be reported
   * @throws IllegalArgumentException
   *           if {@link #unregisterAll(boolean, IProgressMonitor) unregisterAll} has already been called on this
   *           instance (transaction finished).
   */
  void unregisterAll(boolean save, IProgressMonitor monitor);

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
   * @throws JavaModelException
   *           if the contents of the original element cannot be accessed. Reasons include: The original Java element
   *           does not exist or the given compilation unit is not registered in this manager.
   * @throws IllegalArgumentException
   *           if {@link #unregisterAll(boolean, IProgressMonitor) unregisterAll} has already been called on this
   *           instance (transaction finished).
   */
  void reconcile(ICompilationUnit icu, IProgressMonitor monitor) throws JavaModelException;

}
