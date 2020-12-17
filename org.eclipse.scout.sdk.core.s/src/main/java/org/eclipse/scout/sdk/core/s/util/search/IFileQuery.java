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
package org.eclipse.scout.sdk.core.s.util.search;

/**
 * Represents a query executed on a set of files.
 */
public interface IFileQuery extends IFileQueryResult {

  /**
   * Performs the query search on the input given.
   * 
   * @param input
   *          The {@link FileQueryInput} to search in. Must not be {@code null}.
   */
  void searchIn(FileQueryInput input);

}
