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
package org.eclipse.scout.sdk.ui.extensions.technology;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public interface IScoutTechnologyHandler {

  /**
   * called when the technology selection checkbox changes its values. this method is called for all handlers before the
   * first selectionChanged() is invoked.
   *
   * @param selected
   *          the new selection value
   * @return true if the processing should continue, false if the processing should be aborted and the checkbox should
   *         not change its value.
   * @throws CoreException
   */
  boolean preSelectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor) throws CoreException;

  /**
   * called when the technology selection checkbox changes its value
   *
   * @param resources
   *          all resources that should be modified. only "own" (returned by getModifactionResourceCandidates) resources
   *          are passed.
   * @param selected
   *          the new selection value
   */
  void selectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException;

  /**
   * called when the technology selection checkbox changes its values. this method is called for all handlers after the
   * last selectionChanged() is invoked.
   *
   * @param selected
   *          the new selection value
   * @throws CoreException
   */
  void postSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException;

  /**
   * gets the current selection the checkbox should have.
   *
   * @param project
   *          the corresponding scout project
   * @return true if this handler is the opinion the checkbox should be selected, false otherwise.
   */
  TriState getSelection(IScoutBundle project) throws CoreException;

  /**
   * gets all resources the user can choose from when modifying a technology.
   *
   * @param project
   *          the corresponding scout project
   * @return The resources that can be modified
   */
  List<IScoutTechnologyResource> getModifactionResourceCandidates(IScoutBundle project) throws CoreException;

  /**
   * specifies if the handler is active. an inactive handler is never used. it cannot contribute resources and will not
   * be executed.
   *
   * @param project
   *          the corresponding scout project
   * @return true if the handler should be used, false otherwise
   */
  boolean isActive(IScoutBundle project);
}
