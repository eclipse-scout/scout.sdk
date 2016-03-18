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
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;

/**
 * <h3>{@link ClasspathEntry}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ClasspathEntry {
  private final Classpath m_classpath;
  private final String m_encoding;

  public ClasspathEntry(Classpath classpath, String encoding) {
    super();
    m_classpath = classpath;
    m_encoding = encoding;
  }

  public Classpath getClasspath() {
    return m_classpath;
  }

  public String getEncoding() {
    return m_encoding;
  }

  public static Collection<Classpath> toClassPaths(Collection<ClasspathEntry> entries) {
    if (entries == null || entries.isEmpty()) {
      return Collections.emptyList();
    }

    Collection<Classpath> result = new ArrayList<>(entries.size());
    for (ClasspathEntry entry : entries) {
      result.add(entry.getClasspath());
    }
    return result;
  }
}
