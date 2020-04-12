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
package org.eclipse.scout.sdk.core.model.api;

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
