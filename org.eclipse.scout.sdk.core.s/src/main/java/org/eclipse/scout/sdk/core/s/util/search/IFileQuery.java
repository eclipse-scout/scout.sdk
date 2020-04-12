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
package org.eclipse.scout.sdk.core.s.util.search;

import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;

/**
 * Represents a query executed on a set of files.
 */
public interface IFileQuery extends IFileQueryResult {

  /**
   * Performs the query search on the candidate given.
   * 
   * @param candidate
   *          The {@link FileQueryInput} to search in. Must not be {@code null}.
   * @param env
   *          The {@link IEnvironment} which may be used by the query. Must not be {@code null}.
   * @param progress
   *          To report progress of the query. Must not be {@code null}.
   */
  void searchIn(FileQueryInput candidate, IEnvironment env, IProgress progress);

}
