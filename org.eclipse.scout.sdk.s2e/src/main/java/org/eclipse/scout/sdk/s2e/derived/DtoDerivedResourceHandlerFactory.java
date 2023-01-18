/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.derived;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.java.model.api.Flags.isPublic;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.scout.sdk.core.s.derived.DtoUpdateHandler;
import org.eclipse.scout.sdk.core.s.derived.IDerivedResourceHandler;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutAnnotationApi;
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.util.ApiHelper;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

public class DtoDerivedResourceHandlerFactory implements IDerivedResourceHandlerFactory {

  @Override
  public List<IDerivedResourceHandler> createHandlersFor(Set<IResource> resources, IJavaSearchScope searchScope) throws JavaModelException {
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
    for (var r : resources) {
      var javaElement = JavaCore.create(r);
      if (JdtUtils.exists(javaElement) && javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
        var icu = (ICompilationUnit) javaElement;
        var api = ApiHelper.scoutApiFor(icu.getJavaProject());
        if (api.isPresent()) {
          var dtoNames = dtoMarkerAnnotationNames(api.orElseThrow());
          for (var candidate : icu.getTypes()) {
            if (acceptType(candidate) && JdtUtils.exists(JdtUtils.getFirstAnnotationInSupertypeHierarchy(candidate, dtoNames))) {
              collector.add(candidate);
            }
          }
        }
      }
    }
  }

  protected static String[] dtoMarkerAnnotationNames(IScoutAnnotationApi scoutApi) {
    return new String[]{scoutApi.FormData().fqn(), scoutApi.PageData().fqn(), scoutApi.Data().fqn()};
  }

  protected static void findScopeCandidates(IJavaSearchScope scope, Collection<IType> collector) {
    if (scope == null) {
      return;
    }

    ScoutApi.allKnown()
        .map(DtoDerivedResourceHandlerFactory::dtoMarkerAnnotationNames)
        .flatMap(Stream::of)
        .distinct()
        .flatMap(fqn -> JdtUtils.findAllTypesAnnotatedWith(fqn, scope, null).stream())
        .filter(DtoDerivedResourceHandlerFactory::acceptType)
        .forEach(collector::add);
  }

  @SuppressWarnings("squid:S1067")
  protected static boolean acceptType(IType jdtType) {
    // fast check before doing expensive source parsing
    if (!JdtUtils.exists(jdtType)
        || !JdtUtils.exists(jdtType.getJavaProject()) // required!
        || jdtType.isBinary()
        || jdtType.getDeclaringType() != null) {
      return false;
    }

    try {
      return !jdtType.isAnonymous() && isPublic(jdtType.getFlags());
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }
}
