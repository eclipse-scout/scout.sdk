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

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Represents the result of a query search.
 */
public interface IFileQueryResult {

  /**
   * @param file
   *          The file for which the matches should be returned.
   * @return The {@link FileRange ranges} in the file specified that match the query search.
   */
  Set<FileQueryMatch> result(Path file);

  /**
   * @return All {@link FileRange matches} of the query. The result depends on the scope on which the query has been
   *         executed.
   */
  Stream<FileQueryMatch> result();

  /**
   * @return The name or description of the query.
   */
  String name();
}
