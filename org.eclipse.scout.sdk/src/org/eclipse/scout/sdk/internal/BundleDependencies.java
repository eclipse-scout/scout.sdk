/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;

/**
 *
 */
public class BundleDependencies {
  private Set<String> m_reqired = new HashSet<String>();
  private Set<String> m_dependent = new HashSet<String>();

  public BundleDependencies(BundleDescription desc) {
    if (desc != null) {
      for (BundleDescription dep : desc.getDependents()) {
        m_dependent.add(dep.getName());
      }
      for (BundleSpecification req : desc.getRequiredBundles()) {
        m_reqired.add(req.getName());
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BundleDependencies) {
      BundleDependencies ref = (BundleDependencies) obj;
      return equals(ref.m_dependent, m_dependent) && equals(ref.m_reqired, m_reqired);
    }
    return false;
  }

  private boolean equals(Set<String> s1, Set<String> s2) {
    Set<String> comp1 = new HashSet<String>(s1);
    Set<String> comp2 = new HashSet<String>(s2);
    for (String s : comp1) {
      if (!comp2.remove(s)) {
        return false;
      }
    }
    return comp2.isEmpty();
  }

}
