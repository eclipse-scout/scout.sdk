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
    try {
      setJdtHierarchy(getBaseType().newTypeHierarchy(null));
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logError("Unable to create type hierarchy for type " + getBaseType().getFullyQualifiedName(), e);
    }
  }

}
