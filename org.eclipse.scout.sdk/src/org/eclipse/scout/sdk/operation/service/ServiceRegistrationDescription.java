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
package org.eclipse.scout.sdk.operation.service;

import java.security.InvalidParameterException;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link ServiceRegistrationDescription}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 22.11.2013
 */
public class ServiceRegistrationDescription {
  public IJavaProject targetProject;
  public String session;
  public String serviceFactory;

  public ServiceRegistrationDescription(IJavaProject targetProj, IType sessClass, String factoryClass) {
    this(targetProj, getFqn(sessClass), factoryClass);
  }

  public ServiceRegistrationDescription(IJavaProject targetProj, String sessClass, String factoryClass) {
    targetProject = targetProj;
    session = sessClass;
    serviceFactory = factoryClass;
  }

  private static String getFqn(IType t) {
    if (t == null) {
      return null;
    }
    return t.getFullyQualifiedName();
  }

  public ServiceRegistrationDescription(IJavaProject targetProj, IType sessClass) {
    this(targetProj, getFqn(sessClass));
  }

  public ServiceRegistrationDescription(IJavaProject targetProj, String sessClass) {
    this(targetProj, sessClass, calcDefaultFactory(targetProj));
  }

  public ServiceRegistrationDescription(IJavaProject targetProj) {
    this(targetProj, calcDefaultSession(targetProj));
  }

  private static String calcDefaultSession(IJavaProject targetProj) {
    IType[] sessionTypes = ScoutTypeUtility.getSessionTypes(targetProj);
    if (sessionTypes != null && sessionTypes.length > 0) {
      IScoutBundle scoutBundle = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(targetProj);
      if (scoutBundle != null) {
        IType ret = ScoutUtility.getNearestType(sessionTypes, scoutBundle);
        if (TypeUtility.exists(ret)) {
          return ret.getFullyQualifiedName();
        }
      }
      return sessionTypes[0].getFullyQualifiedName();
    }
    return null;
  }

  private static String calcDefaultFactory(IJavaProject target) {
    IScoutBundle b = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(target);
    if (b == null) {
      throw new InvalidParameterException("could not find a scout bundle for service registration.");
    }
    if (IScoutBundle.TYPE_CLIENT.equals(b.getType())) {
      return RuntimeClasses.ClientServiceFactory;
    }
    else if (IScoutBundle.TYPE_SERVER.equals(b.getType())) {
      return RuntimeClasses.ServerServiceFactory;
    }
    else if (IScoutBundle.TYPE_SHARED.equals(b.getType())) {
      return RuntimeClasses.DefaultServiceFactory;
    }
    else {
      throw new UnsupportedOperationException("scout project type '" + b.getType() + "' is not supported yet.");
    }
  }
}
