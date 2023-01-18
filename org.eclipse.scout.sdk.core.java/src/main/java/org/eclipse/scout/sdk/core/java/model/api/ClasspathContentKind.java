/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

/**
 * <h3>{@link ClasspathContentKind}</h3>
 * <p>
 * Specifies the types of classpath content.
 *
 * @since 7.0.0
 */
public enum ClasspathContentKind {
  /**
   * A classpath entry that contains *.java files in a folder structure.
   */
  SOURCE,

  /**
   * A classpath entry that contains *.class files in a folder structure or an archive file (jar).
   */
  BINARY,

  /**
   * A classpath entry that may contain *.java and/or *.class files
   */
  MIXED
}
