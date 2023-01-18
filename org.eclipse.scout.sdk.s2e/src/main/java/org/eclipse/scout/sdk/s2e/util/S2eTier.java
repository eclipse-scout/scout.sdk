/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.util;

import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.OptApiFunction;
import org.eclipse.scout.sdk.core.s.util.ITier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link S2eTier}</h3>
 *
 * @since 7.0.0
 */
public final class S2eTier implements Predicate<IJavaElement> {

  private final ITier<?> m_tier;

  private S2eTier(ITier<?> t) {
    m_tier = t;
  }

  public ITier<?> unwrap() {
    return m_tier;
  }

  /**
   * Converts the given {@link IJavaProject} to its corresponding {@link IJavaProject} with given {@link ITier}.<br>
   * <br>
   * <b>Example :</b><br>
   * {@code ScoutTier.Shared.convert(ScoutTier.Client, sharedJavaProject)} -> ClientJavaProject or {@code null}.
   *
   * @param to
   *          The {@link ITier} to which the {@link IJavaProject} should be converted.
   * @param origin
   *          The original {@link IJavaProject}
   * @return The {@link IJavaProject} of the given {@link ITier} type that belongs to the given input
   *         {@link IJavaProject} or {@code null} if no such {@link IJavaProject} could be found in the workspace.
   */
  public Optional<IJavaProject> convert(ITier<?> to, @SuppressWarnings("TypeMayBeWeakened") IJavaProject origin) {
    if (!JdtUtils.exists(origin) || to == null) {
      return Optional.empty();
    }

    var originJavaProjectName = origin.getElementName();
    var project = origin.getJavaModel().getJavaProject(unwrap().convert(to, originJavaProjectName));
    if (!JdtUtils.exists(project)) {
      return Optional.empty();
    }

    var projectTier = of(project).map(S2eTier::unwrap);
    if (projectTier.isEmpty()) {
      return Optional.empty();
    }
    if (to == projectTier.orElseThrow()) {
      return Optional.of(project);
    }
    return Optional.empty();
  }

  /**
   * Converts the given {@link IPackageFragment} to the given {@link ITier}.<br>
   * <br>
   * <b>Example: </b><br>
   * {@code ScoutTier.Client.convert(ScoutTier.Server, clientPackage, false)}
   *
   * @param to
   *          Specifies the target {@link ITier} to which the given {@link IPackageFragment} should be converted.
   * @param origin
   *          The original {@link IPackageFragment} to convert.
   * @return The converted {@link IPackageFragment} or {@code null} if no matching {@link IPackageFragment} could be
   *         found.
   */
  public Optional<IPackageFragment> convert(ITier<?> to, @SuppressWarnings("TypeMayBeWeakened") IPackageFragment origin) {
    if (!JdtUtils.exists(origin) || to == null) {
      return Optional.empty();
    }

    var targetSrcFolder = convert(to, JdtUtils.getSourceFolder(origin));
    if (targetSrcFolder.isEmpty()) {
      return Optional.empty();
    }

    var name = unwrap().convert(to, origin.getElementName());
    var packageFragment = targetSrcFolder.orElseThrow().getPackageFragment(name);
    if (JdtUtils.exists(packageFragment)) {
      return Optional.of(packageFragment);
    }
    return Optional.empty();
  }

  /**
   * Converts the given {@link IPackageFragmentRoot} to the given {@link ITier}.<br>
   * <br>
   * <b>Example: </b><br>
   * {@code ScoutTier.Client.convert(ScoutTier.Shared, clientSourceFolder)}
   *
   * @param to
   *          Specifies the target {@link ITier} to which the given {@link IPackageFragmentRoot} should be converted.
   * @param origin
   *          The original {@link IPackageFragmentRoot} to convert.
   * @return The converted {@link IPackageFragmentRoot} or {@code null} if no matching {@link IPackageFragmentRoot}
   *         could be found.
   */
  public Optional<IPackageFragmentRoot> convert(ITier<?> to, @SuppressWarnings("TypeMayBeWeakened") IPackageFragmentRoot origin) {
    if (!JdtUtils.exists(origin) || to == null) {
      return Optional.empty();
    }

    var targetProject = convert(to, origin.getJavaProject());
    if (targetProject.isEmpty()) {
      return Optional.empty();
    }

    var projectRelResourcePath = origin.getPath().removeFirstSegments(1).toString();
    var folder = targetProject.orElseThrow().getProject().getFolder(projectRelResourcePath);
    if (folder != null && folder.exists()) {
      var element = JavaCore.create(folder);
      if (JdtUtils.exists(element) && element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
        return Optional.of((IPackageFragmentRoot) element);
      }
    }
    return S2eUtils.primarySourceFolder(targetProject.orElseThrow());
  }

  public static S2eTier wrap(ITier<?> t) {
    return new S2eTier(Ensure.notNull(t));
  }

  public static Optional<S2eTier> of(IJavaElement t) {
    if (!JdtUtils.exists(t)) {
      return Optional.empty();
    }

    return ITier.of(fqn -> JdtUtils.exists(lookupJdtType(t, fqn)), new OptApiFunction() {
      @Override
      public <T extends IApiSpecification> Optional<T> apply(Class<T> api) {
        return ApiHelper.apiFor(t.getJavaProject(), api);
      }
    }).map(S2eTier::wrap);
  }

  static IType lookupJdtType(IJavaElement t, String fqn) {
    try {
      return t.getJavaProject().findType(fqn);
    }
    catch (JavaModelException ex) {
      throw new SdkException(ex);
    }
  }

  @Override
  public boolean test(IJavaElement t) {
    var tier = of(t);
    return tier
        .filter(s2eTier -> unwrap().isIncludedIn(s2eTier.unwrap()))
        .isPresent();
  }

  @Override
  public int hashCode() {
    return m_tier.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    var other = (S2eTier) obj;
    return m_tier == other.m_tier;
  }

  @Override
  public String toString() {
    return m_tier.toString();
  }
}
