/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.derived;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.model.api.Flags.isPublic;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.derived.DtoUpdateHandler;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

public class DtoDerivedResourceHandlerFactory implements IDerivedResourceHandlerFactory {

  @Override
  public List<BiFunction<IEnvironment, IProgress, Collection<? extends IFuture<?>>>> createHandlersFor(Set<IResource> resources, IJavaSearchScope searchScope) throws JavaModelException {
    Collection<IType> baseTypes = new HashSet<>();
    findScopeCandidates(searchScope, baseTypes);
    findResourceCandidates(resources, baseTypes);
    if (baseTypes.isEmpty()) {
      return emptyList();
    }

    return baseTypes.stream()
        .map(DerivedResourceInputWithJdt::new)
        .map(DtoUpdateHandler::new)
        .collect(toList());
  }

  protected static void findResourceCandidates(Iterable<IResource> resources, Collection<IType> collector) throws JavaModelException {
    for (IResource r : resources) {
      IJavaElement javaElement = JavaCore.create(r);
      if (JdtUtils.exists(javaElement) && javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
        ICompilationUnit icu = (ICompilationUnit) javaElement;
        for (IType candidate : icu.getTypes()) {
          if (acceptType(candidate) && JdtUtils.exists(JdtUtils.getFirstAnnotationInSupertypeHierarchy(candidate, IScoutRuntimeTypes.Data, IScoutRuntimeTypes.FormData, IScoutRuntimeTypes.PageData))) {
            collector.add(candidate);
          }
        }
      }
    }
  }

  protected static void findScopeCandidates(IJavaSearchScope scope, Collection<IType> collector) throws JavaModelException {
    if (scope == null) {
      return;
    }

    for (IType candidate : JdtUtils.findAllTypesAnnotatedWith(IScoutRuntimeTypes.Data, scope, null)) {
      if (acceptType(candidate)) {
        collector.add(candidate);
      }
    }
    for (IType candidate : JdtUtils.findAllTypesAnnotatedWith(IScoutRuntimeTypes.FormData, scope, null)) {
      if (acceptType(candidate)) {
        collector.add(candidate);
      }
    }
    for (IType candidate : JdtUtils.findAllTypesAnnotatedWith(IScoutRuntimeTypes.PageData, scope, null)) {
      if (acceptType(candidate)) {
        collector.add(candidate);
      }
    }
  }

  @SuppressWarnings("squid:S1067")
  protected static boolean acceptType(IType jdtType) throws JavaModelException {
    //fast check before doing expensive source parsing
    return JdtUtils.exists(jdtType)
        && JdtUtils.exists(jdtType.getJavaProject()) // required!
        && !jdtType.isAnonymous()
        && !jdtType.isBinary()
        && jdtType.getDeclaringType() == null
        && isPublic(jdtType.getFlags());
  }

}
