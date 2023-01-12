/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
