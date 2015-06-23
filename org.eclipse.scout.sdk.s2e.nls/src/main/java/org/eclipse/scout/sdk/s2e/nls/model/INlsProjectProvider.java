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
package org.eclipse.scout.sdk.s2e.nls.model;

import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;

/**
 * Defines a provider that is capable to create <code>INlsProject</code> hierarchies for implementation specific input
 * parameters.
 */
public interface INlsProjectProvider {
  /**
   * Return the <code>INlsProject</code> hierarchy for the given parameters or null if the implementation does not
   * understand the given parameters.
   *
   * @param args
   * @return The <code>INlsProject</code> hierarchy.
   */
  INlsProject getProject(Object[] args);
}
