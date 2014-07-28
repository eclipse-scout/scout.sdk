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
package org.eclipse.scout.sdk.util.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;

/**
 * <h3>{@link ResourceProxy}</h3>
 *
 * @author Matthias Villiger
 * @since 4.0.0 20.05.2014
 */
public class ResourceProxy implements IResourceProxy {
  private final IResource m_resource;

  public ResourceProxy(IResource r) {
    m_resource = r;
  }

  @Override
  public long getModificationStamp() {
    return m_resource.getModificationStamp();
  }

  @Override
  public boolean isAccessible() {
    return m_resource.isAccessible();
  }

  @Override
  public boolean isDerived() {
    return m_resource.isDerived();
  }

  @Override
  public boolean isLinked() {
    return m_resource.isLinked();
  }

  @Override
  public boolean isPhantom() {
    return m_resource.isPhantom();
  }

  @Override
  public boolean isHidden() {
    return m_resource.isHidden();
  }

  @Override
  public boolean isTeamPrivateMember() {
    return m_resource.isTeamPrivateMember();
  }

  @Override
  public String getName() {
    return m_resource.getName();
  }

  @Override
  public Object getSessionProperty(QualifiedName key) {
    try {
      return m_resource.getSessionProperty(key);
    }
    catch (CoreException e) {
      SdkUtilActivator.logError("unable to retrieve session property '" + key.toString() + "' from resource '" + m_resource.getFullPath().toOSString() + "'.", e);
      return null;
    }
  }

  @Override
  public int getType() {
    return m_resource.getType();
  }

  @Override
  public IPath requestFullPath() {
    return m_resource.getFullPath();
  }

  @Override
  public IResource requestResource() {
    return m_resource;
  }
}
