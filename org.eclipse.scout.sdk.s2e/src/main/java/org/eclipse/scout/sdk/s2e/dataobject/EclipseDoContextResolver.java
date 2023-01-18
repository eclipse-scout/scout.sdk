/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.dataobject;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.dataobject.DoContextResolvers;
import org.eclipse.scout.sdk.core.s.java.apidef.IScout22DoApi;
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.environment.model.JavaEnvironmentWithJdt;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.JdtUtils.PublicPrimaryNonAbstractSourceTypeFilter;

/**
 * <h3>{@link EclipseDoContextResolver}</h3>
 *
 * @since 12.0.0
 */
public class EclipseDoContextResolver implements DoContextResolvers.IDoContextResolver {

  @Override
  public Stream<IType> resolveNamespaceCandidates(IJavaEnvironment environment) {
    if (environment == null) {
      return Stream.empty();
    }
    var project = ((JavaEnvironmentWithJdt) environment.unwrap()).javaProject();
    var filter = new PublicPrimaryNonAbstractSourceTypeFilter().and(EclipseDoContextResolver::isInteresting);
    return ScoutApi.allKnown()
        .map(a -> a.api(IScout22DoApi.class))
        .filter(Optional::isPresent)
        .flatMap(Optional::stream)
        .map(a -> a.INamespace().fqn())
        .distinct()
        .flatMap(iNamespace -> JdtUtils.findTypesInStrictHierarchy(project, iNamespace, null, filter).stream())
        .map(org.eclipse.jdt.core.IType::getFullyQualifiedName)
        .map(environment::findType)
        .filter(Optional::isPresent)
        .flatMap(Optional::stream);
  }

  protected static boolean isInteresting(org.eclipse.jdt.core.IType t) {
    try {
      return !t.isInterface() && !t.isAnnotation();
    }
    catch (JavaModelException e) {
      SdkLog.warning("Attempt to access flags of type '{}' failed. Type will be skipped.", t.getFullyQualifiedName(), e);
      return false;
    }
  }

  @Override
  public Stream<IType> resolvePrimaryTypesInPackageOf(IType namespace) {
    if (namespace == null) {
      return Stream.empty();
    }
    var env = namespace.javaEnvironment();
    var project = ((JavaEnvironmentWithJdt) namespace.javaEnvironment().unwrap()).javaProject();
    try {
      var type = project.findType(namespace.name());
      if (!JdtUtils.exists(type)) {
        return Stream.empty();
      }
      return Stream.of(type.getPackageFragment().getCompilationUnits())
          .map(ITypeRoot::findPrimaryType)
          .filter(Objects::nonNull)
          .map(org.eclipse.jdt.core.IType::getFullyQualifiedName)
          .map(env::findType)
          .filter(Optional::isPresent)
          .flatMap(Optional::stream);
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }
}
