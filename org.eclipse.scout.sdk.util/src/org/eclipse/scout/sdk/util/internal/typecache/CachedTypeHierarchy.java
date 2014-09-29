/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.util.internal.typecache;

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;

/**
 * <h3>{@link CachedTypeHierarchy}</h3>
 *
 * @author Michael Schaufelberger
 * @since 4.1.0 03.06.2014
 */
public final class CachedTypeHierarchy extends AbstractCachedTypeHierarchy {

  CachedTypeHierarchy(IType type) {
    super(type);
  }

  @Override
  public boolean isTypeAccepted(IType candidate, Set<IType> candidateSuperTypes) {
    return candidateSuperTypes.contains(getBaseType());
  }

  @Override
  protected void revalidate() {
    String msg = "Unable to create type hierarchy for type ";
    try {
      revalidateImpl();
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logError(msg + getBaseType().getFullyQualifiedName(), e);
    }
    catch (IllegalArgumentException e) {
      // [mvi] workaround:
      // may happen after a java project has been opened, that affects the existing hierarchies.
      // not ready yet: try a second time after waiting for the indexes (don't wait for the indexes every time. only in error case).
      try {
        JdtUtility.waitForIndexesReady();
        revalidateImpl();
      }
      catch (JavaModelException e1) {
        SdkUtilActivator.logError(msg + getBaseType().getFullyQualifiedName(), e);
      }
    }
  }

  private void revalidateImpl() throws JavaModelException {
    setJdtHierarchy(getBaseType().newTypeHierarchy(null));
  }
}
