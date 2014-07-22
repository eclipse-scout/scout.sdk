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
 * <h3>{@link AbstractSdkTestWithJdtTestProject}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public abstract class AbstractSdkTestWithJdtTestProject extends AbstractScoutSdkTest {

  public static final String CLIENT_NAME = "jdt.test.client";
  public static final String SHARED_NAME = "jdt.test.shared";
  public static final String SERVER_NAME = "jdt.test.server";

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/operation/jdt", CLIENT_NAME, SHARED_NAME, SERVER_NAME);
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
