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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.trigger.IDerivedResourceHandler;
import org.eclipse.scout.sdk.s2e.trigger.IDerivedResourceHandlerFactory;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 *
 */
public class DtoDerivedResourceHandlerFactory implements IDerivedResourceHandlerFactory {

  @Override
  public List<IDerivedResourceHandler> createHandlersFor(Set<IResource> resources, IJavaEnvironmentProvider envProvider, IJavaSearchScope searchScope) throws CoreException {
    Set<IType> baseTypes = new HashSet<>();
    findScopeCandidates(searchScope, baseTypes);
    findResourceCandidates(resources, baseTypes);
    if (baseTypes.isEmpty()) {
      return Collections.emptyList();
    }

    List<IDerivedResourceHandler> handlers = new ArrayList<>(baseTypes.size());
    for (IType t : baseTypes) {
      handlers.add(new DtoDerivedResourceHandler(t, envProvider));
    }
    return handlers;
  }

  protected void findResourceCandidates(Set<IResource> resources, Collection<IType> collector) throws CoreException {
    for (IResource r : resources) {
      IJavaElement javaElement = JavaCore.create(r);
      if (S2eUtils.exists(javaElement) && javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
        ICompilationUnit icu = (ICompilationUnit) javaElement;
        for (IType candidate : icu.getTypes()) {
          if (acceptType(candidate) && S2eUtils.exists(S2eUtils.getFirstAnnotationInSupertypeHierarchy(candidate, IScoutRuntimeTypes.Data, IScoutRuntimeTypes.FormData, IScoutRuntimeTypes.PageData))) {
            collector.add(candidate);
          }
        }
      }
    }
  }

  protected void findScopeCandidates(IJavaSearchScope scope, Collection<IType> collector) throws CoreException {
    if (scope == null) {
      return;
    }

    for (org.eclipse.jdt.core.IType candidate : S2eUtils.findAllTypesAnnotatedWith(IScoutRuntimeTypes.Data, scope, null)) {
      if (acceptType(candidate)) {
        collector.add(candidate);
      }
    }
    for (org.eclipse.jdt.core.IType candidate : S2eUtils.findAllTypesAnnotatedWith(IScoutRuntimeTypes.FormData, scope, null)) {
      if (acceptType(candidate)) {
        collector.add(candidate);
      }
    }
    for (org.eclipse.jdt.core.IType candidate : S2eUtils.findAllTypesAnnotatedWith(IScoutRuntimeTypes.PageData, scope, null)) {
      if (acceptType(candidate)) {
        collector.add(candidate);
      }
    }
  }

  @SuppressWarnings("squid:S1067")
  protected boolean acceptType(IType jdtType) throws CoreException {
    //fast check before doing expensive source parsing
    return S2eUtils.exists(jdtType)
        && S2eUtils.exists(jdtType.getJavaProject()) // required!
        && !jdtType.isAnonymous()
        && !jdtType.isBinary()
        && jdtType.getDeclaringType() == null
        && Flags.isPublic(jdtType.getFlags());
  }

}
