/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

/**
 * <h3>{@link CompilationUnitOverrideSupport}</h3>
 *
 * @since 7.0.100
 */
public class CompilationUnitOverrideSupport {
  public static final char SEPARATOR = '/';
  private final Map<CharBuffer, ICompilationUnit> m_overrideCompilationUnits;
  private final Set<CharBuffer> m_additionalPackages;

  public CompilationUnitOverrideSupport() {
    m_overrideCompilationUnits = new HashMap<>();
    m_additionalPackages = new HashSet<>();
  }

  /**
   * adds a compilation unit override
   *
   * @param cu
   *          The override
   * @return {@code true} if there was a previous mapping which has been updated with this call. {@code false} if this
   *         is the first override for this compilation unit or there was no change.
   */
  public boolean addCompilationUnit(ICompilationUnit cu) {
    char[][] packageName0 = cu.getPackageName();
    CharBuffer key = CharBuffer.wrap(CharOperation.concatWith(packageName0, cu.getMainTypeName(), SEPARATOR));

    boolean updatedExistingEntry = false;
    ICompilationUnit existingIcu = m_overrideCompilationUnits.put(key, cu);
    if (existingIcu != null) {
      updatedExistingEntry = !Arrays.equals(existingIcu.getContents(), cu.getContents());
    }

    //register additional packages
    if (packageName0 != null && packageName0.length > 0) {
      for (int i = 1; i <= packageName0.length; i++) {
        m_additionalPackages.add(CharBuffer.wrap(CharOperation.concatWith(CharOperation.subarray(packageName0, 0, i), SEPARATOR)));
      }
    }
    return updatedExistingEntry;
  }

  public Collection<ICompilationUnit> getCompilationUnits() {
    return m_overrideCompilationUnits.values();
  }

  public boolean containsPackage(char[] pck) {
    return m_additionalPackages.contains(CharBuffer.wrap(pck));
  }

  public int size() {
    return m_overrideCompilationUnits.size();
  }

  public ICompilationUnit get(char[] fqn) {
    return m_overrideCompilationUnits.get(CharBuffer.wrap(fqn));
  }

  public void clear() {
    m_additionalPackages.clear();
    m_overrideCompilationUnits.clear();
  }
}
