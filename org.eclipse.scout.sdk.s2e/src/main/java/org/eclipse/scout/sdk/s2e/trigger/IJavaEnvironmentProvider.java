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
package org.eclipse.scout.sdk.s2e.trigger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;

/**
 * <h3>{@link IJavaEnvironmentProvider}</h3>
 * <p>
 * Provides {@link IJavaEnvironment} instances.
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public interface IJavaEnvironmentProvider {

  /**
   * @param jdtType
   * @return the scout sdk model type
   */
  IType jdtTypeToScoutType(org.eclipse.jdt.core.IType jdtType) throws CoreException;

  IJavaEnvironment get(org.eclipse.jdt.core.IJavaProject jdtProject) throws CoreException;
}
