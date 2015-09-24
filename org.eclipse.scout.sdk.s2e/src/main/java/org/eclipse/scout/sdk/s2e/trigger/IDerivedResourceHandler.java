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
package org.eclipse.scout.sdk.s2e.trigger;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;

/**
 * <h3>{@link IDerivedResourceHandler}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 16.08.2013
 */
public interface IDerivedResourceHandler {

  List<IDerivedResourceOperation> createOperations(IType jdtType, IJavaEnvironmentProvider envProvider) throws CoreException;

  /**
   * @param collector
   * @throws CoreException
   */
  List<IDerivedResourceOperation> createAllOperations(IJavaSearchScope scope, IJavaEnvironmentProvider envProvider) throws CoreException;
}
