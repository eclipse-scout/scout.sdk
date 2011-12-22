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
package org.eclipse.scout.sdk.util.typecache;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;

public interface IWorkingCopyManager {

  /**
   * Register compilation unit BEFORE doing changes on it.
   * Creates a working copy on the first registration.
   * Compilation unit may be registered multiple times
   * 
   * @param icu
   */
  void register(ICompilationUnit icu, IProgressMonitor monitor) throws JavaModelException;

  /**
   * Register compilation unit BEFORE doing changes on it.
   * Creates a working copy on the first registration.
   * Compilation unit may be registered multiple times
   * 
   * @param icu
   * @param wellform
   *          to ensure the icu will be wellformed after the process ended.
   */
  void register(ICompilationUnit icu, boolean wellform, IProgressMonitor monitor) throws JavaModelException;

  /**
   * Unregister compilation unit AFTER doing changes on it.
   * Commits and discards working copy at the first invokation.
   * Compilation unit may be unregistered multiple times
   * 
   * @param icu
   */
  void unregister(ICompilationUnit icu, IProgressMonitor monitor);

  void unregisterAll(IProgressMonitor monitor);

  /**
   * When doing direct source changes on the compilation unit, a reconcile is required to fire element change deltas.
   * This is not required if changes are done using for example
   * {@link IType#createMethod(String, IJavaElement, boolean, IProgressMonitor)} or
   * {@link IType#createField(String, IJavaElement, boolean, IProgressMonitor)}
   * 
   * @throws JavaModelException
   */
  CompilationUnit reconcile(ICompilationUnit icu, IProgressMonitor monitor) throws CoreException;

}
