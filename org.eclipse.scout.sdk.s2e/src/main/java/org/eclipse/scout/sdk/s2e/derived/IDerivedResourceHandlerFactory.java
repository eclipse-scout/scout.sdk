/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.derived;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.scout.sdk.core.s.derived.IDerivedResourceHandler;

/**
 * <h3>{@link IDerivedResourceHandlerFactory}</h3>
 *
 * @since 5.2
 */
@FunctionalInterface
public interface IDerivedResourceHandlerFactory {

  /**
   * Creates all {@link IDerivedResourceHandler}s to update the resources derived from the given resource {@link Set}.
   *
   * @param resources
   *          The {@link Set} of {@link IResource}s for which the derived resources should be updated. Such an
   *          {@link IResource} can be of any type ({@link IResource#PROJECT}, {@link IResource#FOLDER},
   *          {@link IResource#FILE}, {@link IResource#ROOT}). If a resource is {@code instanceof} {@link IContainer},
   *          all resources inside this container and all of its sub-containers should update its derived resources.
   * @param searchScope
   *          The {@link IJavaSearchScope} covering the given resources or {@code null} if no {@link IJavaElement}s are
   *          in the given resources. Note: The searchScope may not contain all resources given. Only the ones that
   *          belong to an {@link IJavaElement} are part of the search scope.
   * @return All {@link IDerivedResourceHandler}s that are based on the given resources.
   * @throws CoreException
   *           if there is an error
   */
  List<IDerivedResourceHandler> createHandlersFor(Set<IResource> resources, IJavaSearchScope searchScope) throws CoreException;
}
