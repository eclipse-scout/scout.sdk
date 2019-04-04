/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.util;

import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link S2eScoutTier}</h3>
 *
 * @since 7.0.0
 */
public final class S2eScoutTier implements Predicate<IJavaElement> {

  private final ScoutTier m_tier;

  private S2eScoutTier(ScoutTier t) {
    m_tier = t;
  }

  public ScoutTier unwrap() {
    return m_tier;
  }

  /**
   * Converts the given {@link IJavaProject} to its corresponding {@link IJavaProject} with given {@link ScoutTier}.<br>
   * <br>
   * <b>Example :</b><br>
   * {@code ScoutTier.Shared.convert(ScoutTier.Client, sharedJavaProject)} -> ClientJavaProject or {@code null}.
   *
   * @param to
   *          The {@link ScoutTier} to which the {@link IJavaProject} should be converted.
   * @param origin
   *          The original {@link IJavaProject}
   * @return The {@link IJavaProject} of the given {@link ScoutTier} type that belongs to the given input
   *         {@link IJavaProject} or {@code null} if no such {@link IJavaProject} could be found in the workspace.
   */
  public Optional<IJavaProject> convert(ScoutTier to, IJavaProject origin) {
    if (!JdtUtils.exists(origin) || to == null) {
      return Optional.empty();
    }

    String originJavaProjectName = origin.getElementName();
    IJavaProject project = origin.getJavaModel().getJavaProject(unwrap().convert(to, originJavaProjectName));
    if (!JdtUtils.exists(project)) {
      return Optional.empty();
    }

    Optional<ScoutTier> projectTier = valueOf(project).map(S2eScoutTier::unwrap);
    if (!projectTier.isPresent()) {
      return Optional.empty();
    }
    if (to == projectTier.get()) {
      return Optional.of(project);
    }
    return Optional.empty();
  }

  /**
   * Converts the given {@link IPackageFragment} to the given {@link ScoutTier}.<br>
   * <br>
   * <b>Example: </b><br>
   * {@code ScoutTier.Client.convert(ScoutTier.Server, clientPackage, false)}
   *
   * @param to
   *          Specifies the target {@link ScoutTier} to which the given {@link IPackageFragment} should be converted.
   * @param origin
   *          The original {@link IPackageFragment} to convert.
   * @return The converted {@link IPackageFragment} or {@code null} if no matching {@link IPackageFragment} could be
   *         found.
   */
  public Optional<IPackageFragment> convert(ScoutTier to, IPackageFragment origin) {
    if (!JdtUtils.exists(origin) || to == null) {
      return Optional.empty();
    }

    Optional<IPackageFragmentRoot> targetSrcFolder = convert(to, JdtUtils.getSourceFolder(origin));
    if (!targetSrcFolder.isPresent()) {
      return Optional.empty();
    }

    String name = unwrap().convert(to, origin.getElementName());
    IPackageFragment packageFragment = targetSrcFolder.get().getPackageFragment(name);
    if (JdtUtils.exists(packageFragment)) {
      return Optional.of(packageFragment);
    }
    return Optional.empty();
  }

  /**
   * Converts the given {@link IPackageFragmentRoot} to the given {@link ScoutTier}.<br>
   * <br>
   * <b>Example: </b><br>
   * {@code ScoutTier.Client.convert(ScoutTier.Shared, clientSourceFolder)}
   *
   * @param to
   *          Specifies the target {@link ScoutTier} to which the given {@link IPackageFragmentRoot} should be
   *          converted.
   * @param origin
   *          The original {@link IPackageFragmentRoot} to convert.
   * @return The converted {@link IPackageFragmentRoot} or {@code null} if no matching {@link IPackageFragmentRoot}
   *         could be found.
   */
  public Optional<IPackageFragmentRoot> convert(ScoutTier to, IPackageFragmentRoot origin) {
    if (!JdtUtils.exists(origin) || to == null) {
      return Optional.empty();
    }

    Optional<IJavaProject> targetProject = convert(to, origin.getJavaProject());
    if (!targetProject.isPresent()) {
      return Optional.empty();
    }

    String projectRelResourcePath = origin.getPath().removeFirstSegments(1).toString();
    IFolder folder = targetProject.get().getProject().getFolder(projectRelResourcePath);
    if (folder != null && folder.exists()) {
      IJavaElement element = JavaCore.create(folder);
      if (JdtUtils.exists(element) && element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
        return Optional.of((IPackageFragmentRoot) element);
      }
    }
    return S2eUtils.primarySourceFolder(targetProject.get());
  }

  public static S2eScoutTier wrap(ScoutTier t) {
    return new S2eScoutTier(Ensure.notNull(t));
  }

  public static Optional<S2eScoutTier> valueOf(IJavaElement t) {
    if (!JdtUtils.exists(t)) {
      return Optional.empty();
    }
    Optional<ScoutTier> tier = ScoutTier.valueOf(fqn -> JdtUtils.exists(lookupJdtType(t, fqn)));
    return tier.map(S2eScoutTier::wrap);
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
    Optional<S2eScoutTier> tier = valueOf(t);
    return tier
        .filter(s2eScoutTier -> unwrap().isIncludedIn(s2eScoutTier.unwrap()))
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
    S2eScoutTier other = (S2eScoutTier) obj;
    return m_tier == other.m_tier;
  }

  @Override
  public String toString() {
    return m_tier.toString();
  }
}
