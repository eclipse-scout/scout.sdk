/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.BeforeClass;

/**
 * <h3>{@link AbstractSdkTestWithSampleProject}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public abstract class AbstractSdkTestWithSampleProject extends AbstractScoutSdkTest {

  public static final String UI_SWING_NAME = "sample.ui.swing";
  public static final String UI_SWT_NAME = "sample.ui.swt";
  public static final String CLIENT_NAME = "sample.client";
  public static final String SHARED_NAME = "sample.shared";
  public static final String SERVER_NAME = "sample.server";

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/operation/sampleProject/", UI_SWING_NAME, UI_SWT_NAME, CLIENT_NAME, SHARED_NAME, SERVER_NAME);
  }

  protected IProject getUiSwingProject() {
    return getProject(UI_SWING_NAME);
  }

  protected IProject getUiSwtProject() {
    return getProject(UI_SWT_NAME);
  }

  protected IProject getClientProject() {
    return getProject(CLIENT_NAME);
  }

  protected IProject getSharedProject() {
    return getProject(SHARED_NAME);
  }

  protected IProject getServerProject() {
    return getProject(SERVER_NAME);
  }

  protected IJavaProject getUiSwingJavaProject() {
    return JavaCore.create(getProject(UI_SWING_NAME));
  }

  protected IJavaProject getUiSwtJavaProject() {
    return JavaCore.create(getProject(UI_SWT_NAME));
  }

  protected IJavaProject getClientJavaProject() {
    return JavaCore.create(getProject(CLIENT_NAME));
  }

  protected IJavaProject getSharedJavaProject() {
    return JavaCore.create(getProject(SHARED_NAME));
  }

  protected IJavaProject getServerJavaProject() {
    return JavaCore.create(getProject(SERVER_NAME));
  }

}
