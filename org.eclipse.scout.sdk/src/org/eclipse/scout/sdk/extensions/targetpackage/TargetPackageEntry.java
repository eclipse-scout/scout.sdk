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
package org.eclipse.scout.sdk.extensions.targetpackage;

/**
 * <h3>{@link TargetPackageEntry}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.9.0 25.02.2013
 */
public class TargetPackageEntry {
  private String m_id;
  private String m_defaultSuffix;
  private String m_bundleType;

  public TargetPackageEntry(String id, String defaultSuffix, String type) {
    m_id = id;
    m_defaultSuffix = defaultSuffix;
    m_bundleType = type;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TargetPackageEntry) {
      return m_id.equals(((TargetPackageEntry) obj).m_id);
    }
    return false;
  }

  @Override
  public String toString() {
    return m_id;
  }

  @Override
  public int hashCode() {
    return m_id.hashCode();
  }

  public String getId() {
    return m_id;
  }

  public void setId(String id) {
    m_id = id;
  }

  public String getDefaultSuffix() {
    return m_defaultSuffix;
  }

  public void setDefaultSuffix(String defaultSuffix) {
    m_defaultSuffix = defaultSuffix;
  }

  public String getBundleType() {
    return m_bundleType;
  }

  public void setBundleType(String bundleType) {
    m_bundleType = bundleType;
  }
}
