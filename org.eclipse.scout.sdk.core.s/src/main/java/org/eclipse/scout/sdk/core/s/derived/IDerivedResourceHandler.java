/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.derived;

import java.util.Collection;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;

public interface IDerivedResourceHandler {

  /**
   * Creates all derived resources of this handler.
   * 
   * @param env
   *          The {@link IEnvironment} on which the handler should be executed. Must not be {@code null}.
   * @param progress
   *          The {@link IProgress indicator} to report progress and handle cancellation. Must not be {@code null}.
   * @return A {@link Collection} of all asynchronous write operations that have been scheduled by the handler. The
   *         implementation must ensure that ALL async tasks scheduled by this handler will be returned by this method
   *         call even though there might be an exception inside. Otherwise the caller cannot ensure to wait for the
   *         already scheduled tasks which might the async task to fail because the underlying transaction and
   *         {@link IEnvironment} have been closed.
   */
  Collection<? extends IFuture<?>> apply(IEnvironment env, IProgress progress);

}
