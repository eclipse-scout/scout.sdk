/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.apidef;

/**
 * By default, an {@link IApiSpecification} is computed chained on its first access, that is first the highest (the
 * current) {@link IApiSpecification} is checked if it complies with the requested {@link IApiSpecification}, then the
 * next lower version will be checked. This interface allows breaking this chain.
 */
public interface IChainedApiComputationAbortSupport {

  /**
   * @param apiDefinition
   *          the API to find.
   * @return whether to abort chain
   */
  boolean abortChainedApiComputation(Class<? extends IApiSpecification> apiDefinition);
}
