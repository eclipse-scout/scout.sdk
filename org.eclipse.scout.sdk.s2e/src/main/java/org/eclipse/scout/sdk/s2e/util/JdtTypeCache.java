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
package org.eclipse.scout.sdk.s2e.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.set.ListOrderedSet;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;

/**
 * <h3>{@link JdtTypeCache}</h3>
 * Helper class to cache {@link IType}s over several calls. This class ensures that each type is only resolved once.<br>
 * <b>Note: </b>Do not hold references of this class to long because the {@link IType} handles may become invalid over
 * time!<br>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public final class JdtTypeCache {
  private final Map<String /*fqn*/, ListOrderedSet/*<IType>*/> m_types;

  public JdtTypeCache() {
    m_types = new HashMap<>();
  }

  /**
   * Finds all types accessible in the current workspace that have the given fully qualified name.
   * 
   * @param fqn
   *          The fully qualified name to search.
   * @return A {@link ListOrderedSet} containing all the {@link IType}s with the given name.
   * @throws CoreException
   */
  public ListOrderedSet/*<IType>*/ getTypes(String fqn) throws CoreException {
    ListOrderedSet/*<IType>*/ result = m_types.get(fqn);
    if (result == null) {
      ListOrderedSet/*<IType>*/ candidates = JdtUtils.resolveJdtTypes(fqn);
      m_types.put(fqn, candidates);
      result = candidates;
    }
    return result;
  }
}
