/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.util;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;

/**
 * <h3>{@link ScoutTier}</h3> Helper class to detect and convert scout tiers.
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
@SuppressWarnings("squid:S00115")
public enum ScoutTier implements Predicate<IJavaElement> {

  /**
   * Scout Client Tier
   */
  Client,

  /**
   * Scout Shared Tier
   */
  Shared,

  /**
   * Scout Server Tier
   */
  Server,

  /**
   * Scout HTML UI Tier
   */
  HtmlUi;

  /**
   * Tests if the tier of this instance is included in the tier of the given {@link IJavaElement}.
   *
   * @return <code>true</code> if the tier of the given {@link IJavaElement} includes all elements of this tier.
   *         <code>false</code> otherwise or if the given {@link IJavaElement} does not belong to a Scout tier.
   * @see #isIncludedIn(ScoutTier)
   */
  @Override
  public boolean test(IJavaElement element) {
    return this.isIncludedIn(valueOfInternal(element));
  }

  /**
   * Tests if the tier of this instance is included in the given tier.<br>
   * <br>
   * <b>Examples:</b><br>
   * <code>ScoutTier.Shared.isIncludedIn(ScoutTier.Client) == true</code><br>
   * <code>ScoutTier.Client.isIncludedIn(ScoutTier.HtmlUi) == true</code><br>
   * <code>ScoutTier.Server.isIncludedIn(ScoutTier.Server) == true</code><br>
   * <code>ScoutTier.Server.isIncludedIn(ScoutTier.Shared) == false</code><br>
   * <code>ScoutTier.HtmlUi.isIncludedIn(ScoutTier.Client) == false</code><br>
   * <code>ScoutTier.Shared.isIncludedIn(null) == false</code>
   *
   * @param tierOfOtherElement
   *          The other tier to test against or <code>null</code>.
   * @return <code>true</code> if the given tier includes all elements of this tier. <code>false</code> otherwise or if
   *         the given tier is <code>null</code>.
   */
  public boolean isIncludedIn(ScoutTier tierOfOtherElement) {
    if (tierOfOtherElement == null) {
      return false;
    }
    if (Shared.equals(this)) {
      return true; // shared is always available if the other is not null
    }
    if (Client.equals(this)) {
      return Client.equals(tierOfOtherElement) || HtmlUi.equals(tierOfOtherElement);
    }
    return equals(tierOfOtherElement);
  }

  private static ScoutTier valueOfInternal(IJavaElement element) {
    try {
      return valueOf(element);
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }

  /**
   * Gets the {@link ScoutTier} the given {@link IJavaElement} belongs to.
   *
   * @param element
   *          The {@link IJavaElement} for which the {@link ScoutTier} should be calculated.
   * @return The {@link ScoutTier} the given element belongs to or <code>null</code> if it does not belong to a
   *         {@link ScoutTier}.
   * @throws JavaModelException
   */
  public static ScoutTier valueOf(IJavaElement element) throws JavaModelException {
    if (!S2eUtils.exists(element)) {
      return null;
    }
    IJavaProject javaProject = element.getJavaProject();
    if (!S2eUtils.exists(javaProject)) {
      return null;
    }

    boolean uiAvailable = S2eUtils.exists(javaProject.findType(IScoutRuntimeTypes.UiServlet));
    if (uiAvailable) {
      return HtmlUi;
    }

    boolean clientAvailable = S2eUtils.exists(javaProject.findType(IScoutRuntimeTypes.IClientSession));
    if (clientAvailable) {
      return Client;
    }

    boolean serverAvailable = S2eUtils.exists(javaProject.findType(IScoutRuntimeTypes.IServerSession));
    if (serverAvailable) {
      return Server;
    }

    boolean sharedAvailable = S2eUtils.exists(javaProject.findType(IScoutRuntimeTypes.ISession));
    if (sharedAvailable) {
      return Shared;
    }

    return null;
  }

  /**
   * Gets the name of this {@link ScoutTier}.
   *
   * @return E.g. "client" or "ui.html"
   */
  public String tierName() {
    switch (this) {
      case Client:
        return "client";
      case Shared:
        return "shared";
      case HtmlUi:
        return "ui.html";
      default:
        return "server";
    }
  }

  /**
   * Converts the given {@link String} from the {@link #tierName()} of this {@link ScoutTier} to the {@link #tierName()}
   * of the given {@link ScoutTier}.<br>
   * <br>
   * <b>Example :</b><br>
   * <code>ScoutTier.Client.convert(ScoutTier.Server, "org.eclipse.scout.client.test.app")</code> ->
   * "org.eclipse.scout.server.test.app"
   *
   * @param to
   *          The {@link ScoutTier} to which the given name should be converted.
   * @param name
   *          The name to convert.
   * @return The converted {@link String}.
   */
  public String convert(ScoutTier to, String name) {
    if (StringUtils.isBlank(name)) {
      return name;
    }
    return StringUtils.replace(name, '.' + tierName(), '.' + to.tierName());
  }

  /**
   * Converts the given {@link IJavaProject} to its corresponding {@link IJavaProject} with given {@link ScoutTier}.<br>
   * <br>
   * <b>Example :</b><br>
   * <code>ScoutTier.Shared.convert(ScoutTier.Client, sharedJavaProject)</code> -> ClientJavaProject or
   * <code>null</code>.
   *
   * @param to
   *          The {@link ScoutTier} to which the {@link IJavaProject} should be converted.
   * @param origin
   *          The original {@link IJavaProject}
   * @return The {@link IJavaProject} of the given {@link ScoutTier} type that belongs to the given input
   *         {@link IJavaProject} or <code>null</code> if no such {@link IJavaProject} could be found in the workspace.
   * @throws JavaModelException
   */
  public IJavaProject convert(ScoutTier to, IJavaProject origin) throws JavaModelException {
    if (!S2eUtils.exists(origin)) {
      return null;
    }

    String originJavaProjectName = origin.getJavaProject().getElementName();
    IJavaProject project = origin.getJavaModel().getJavaProject(convert(to, originJavaProjectName));
    if (S2eUtils.exists(project) && to.equals(valueOf(project))) {
      return project;
    }
    return null;
  }

  /**
   * Converts the given {@link IPackageFragment} to the given {@link ScoutTier}.<br>
   * <br>
   * <b>Example: </b><br>
   * <code>ScoutTier.Client.convert(ScoutTier.Server, clientPackage, false)</code>
   *
   * @param to
   *          Specifies the target {@link ScoutTier} to which the given {@link IPackageFragment} should be converted.
   * @param origin
   *          The original {@link IPackageFragment} to convert.
   * @return The converted {@link IPackageFragment} or <code>null</code> if no matching {@link IPackageFragment} could
   *         be found.
   * @throws JavaModelException
   */
  public IPackageFragment convert(ScoutTier to, IPackageFragment origin) throws JavaModelException {
    if (!S2eUtils.exists(origin)) {
      return null;
    }

    IPackageFragmentRoot targetSrcFolder = convert(to, (IPackageFragmentRoot) origin.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT));
    if (targetSrcFolder == null) {
      return null;
    }

    String name = convert(to, origin.getElementName());
    IPackageFragment packageFragment = targetSrcFolder.getPackageFragment(name);
    if (!S2eUtils.exists(packageFragment)) {
      return null;
    }
    return packageFragment;
  }

  /**
   * Converts the given {@link IPackageFragmentRoot} to the given {@link ScoutTier}.<br>
   * <br>
   * <b>Example: </b><br>
   * <code>ScoutTier.Client.convert(ScoutTier.Shared, clientSourceFolder, true)</code>
   *
   * @param to
   *          Specifies the target {@link ScoutTier} to which the given {@link IPackageFragmentRoot} should be
   *          converted.
   * @param origin
   *          The original {@link IPackageFragmentRoot} to convert.
   * @return The converted {@link IPackageFragmentRoot} or <code>null</code> if no matching {@link IPackageFragmentRoot}
   *         could be found.
   * @throws JavaModelException
   */
  public IPackageFragmentRoot convert(ScoutTier to, IPackageFragmentRoot origin) throws JavaModelException {
    if (!S2eUtils.exists(origin)) {
      return null;
    }

    IJavaProject targetProject = convert(to, origin.getJavaProject());
    if (targetProject == null) {
      return null;
    }

    String projectRelResourcePath = origin.getPath().removeFirstSegments(1).toString();
    IFolder folder = targetProject.getProject().getFolder(projectRelResourcePath);
    if (folder != null && folder.exists()) {
      IJavaElement element = JavaCore.create(folder);
      if (S2eUtils.exists(element) && element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
        return (IPackageFragmentRoot) element;
      }
    }

    try {
      return S2eUtils.getPrimarySourceFolder(targetProject);
    }
    catch (JavaModelException e) {
      SdkLog.info("Unable to calculate default source folder.", e);
    }
    return null;
  }
}
