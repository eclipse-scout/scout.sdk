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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;

/**
 * <h3>{@link IDerivedResourceHandlerFactory}</h3>
 *
 * @author Ivan Motsch
 * @since 5.2
 */
public interface IDerivedResourceHandlerFactory {

  /**
   * Creates all {@link IDerivedResourceHandler}s to update the given {@link IType}.
   *
   * @param jdtType
   *          The type for which the derived resources should be updated.
   * @param envProvider
   *          The {@link IJavaEnvironmentProvider} to use.
   * @return All {@link IDerivedResourceHandler}s that are based on the given {@link IType}.
   * @throws CoreException
   */
  List<IDerivedResourceHandler> createHandlersFor(IType jdtType, IJavaEnvironmentProvider envProvider) throws CoreException;

  /**
   * Creates all {@link IDerivedResourceHandler}s to update all derived resources based on {@link IType}s in the given
   * {@link IJavaSearchScope}.
   *
   * @param scope
   *          The {@link IJavaSearchScope} defining all base {@link IType}s for which the derived resources should be
   *          updated.
   * @param envProvider
   *          The {@link IJavaEnvironmentProvider} to use.
   * @return All {@link IDerivedResourceHandler}s necessary to execute to update all derived resources based on a
   *         {@link IType} in the given {@link IJavaSearchScope}.
   * @throws CoreException
   */
  List<IDerivedResourceHandler> createAllHandlersIn(IJavaSearchScope scope, IJavaEnvironmentProvider envProvider) throws CoreException;

  /**
   * Creates all {@link IDerivedResourceHandler}s that cleanup the given scope.
   * 
   * @param scope
   *          The scope to clean
   * @param envProvider
   *          The {@link IJavaEnvironmentProvider} to use.
   * @return All {@link IDerivedResourceHandler}s necessary to execute to clean all derived resources based on a
   *         {@link IType} in the given {@link IJavaSearchScope}.
   * @throws CoreException
   */
  List<IDerivedResourceHandler> createCleanupHandlersIn(IJavaSearchScope scope, IJavaEnvironmentProvider envProvider) throws CoreException;
}
