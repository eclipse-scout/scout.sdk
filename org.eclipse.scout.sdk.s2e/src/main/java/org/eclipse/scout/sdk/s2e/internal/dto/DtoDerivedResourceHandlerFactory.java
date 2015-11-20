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
package org.eclipse.scout.sdk.s2e.internal.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.s2e.trigger.IDerivedResourceHandler;
import org.eclipse.scout.sdk.s2e.trigger.IDerivedResourceHandlerFactory;
import org.eclipse.scout.sdk.s2e.trigger.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 *
 */
public class DtoDerivedResourceHandlerFactory implements IDerivedResourceHandlerFactory {

  @Override
  public List<IDerivedResourceHandler> createHandlersFor(IType jdtType, IJavaEnvironmentProvider envProvider) throws CoreException {
    if (!acceptType(jdtType)) {
      return Collections.emptyList();
    }
    return Collections.<IDerivedResourceHandler> singletonList(new DtoDerivedResourceHandler(jdtType, envProvider));
  }

  @Override
  public List<IDerivedResourceHandler> createAllHandlersIn(IJavaSearchScope scope, IJavaEnvironmentProvider envProvider) throws CoreException {
    return Collections.<IDerivedResourceHandler> singletonList(new DtoDerivedResourceBatchHandler(findAllCandidates(scope), envProvider));
  }

  protected Collection<IType> findAllCandidates(IJavaSearchScope scope) throws CoreException {
    Set<IType> collector = new HashSet<>();
    for (org.eclipse.jdt.core.IType candidate : S2eUtils.findAllTypesAnnotatedWith(IScoutRuntimeTypes.Data, scope, new NullProgressMonitor())) {
      if (acceptType(candidate)) {
        collector.add(candidate);
      }
    }
    for (org.eclipse.jdt.core.IType candidate : S2eUtils.findAllTypesAnnotatedWith(IScoutRuntimeTypes.FormData, scope, new NullProgressMonitor())) {
      if (acceptType(candidate)) {
        collector.add(candidate);
      }
    }
    for (org.eclipse.jdt.core.IType candidate : S2eUtils.findAllTypesAnnotatedWith(IScoutRuntimeTypes.PageData, scope, new NullProgressMonitor())) {
      if (acceptType(candidate)) {
        collector.add(candidate);
      }
    }
    return collector;
  }

  protected boolean acceptType(IType jdtType) throws CoreException {
    //fast check before doing expensive source parsing
    return S2eUtils.exists(jdtType)
        && S2eUtils.exists(jdtType.getJavaProject()) // required!
        && !jdtType.isAnonymous()
        && !jdtType.isBinary()
        && jdtType.getDeclaringType() == null
        && Flags.isPublic(jdtType.getFlags())
        && S2eUtils.getFirstAnnotationInSupertypeHierarchy(jdtType, IScoutRuntimeTypes.Data, IScoutRuntimeTypes.FormData, IScoutRuntimeTypes.PageData) != null;
  }

  @Override
  public List<IDerivedResourceHandler> createCleanupHandlersIn(IJavaSearchScope scope, IJavaEnvironmentProvider envProvider) throws CoreException {
    return null;
  }

}
