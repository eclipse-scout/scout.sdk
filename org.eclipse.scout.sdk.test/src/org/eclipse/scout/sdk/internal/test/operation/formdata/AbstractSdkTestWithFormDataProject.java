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
package org.eclipse.scout.sdk.internal.test.operation.formdata;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.junit.BeforeClass;

/**
 * <h3>{@link AbstractSdkTestWithFormDataProject}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class AbstractSdkTestWithFormDataProject extends AbstractScoutSdkTest {

  public static final String CLIENT_NAME = "formdata.client";
  public static final String SHARED_NAME = "formdata.shared";

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/operation/formData/", CLIENT_NAME, SHARED_NAME);
  }

  protected static IProject getClientProject() {
    return getProject(CLIENT_NAME);
  }

  protected static IProject getSharedProject() {
    return getProject(SHARED_NAME);
  }

  protected static IJavaProject getClientJavaProject() {
    return JavaCore.create(getProject(CLIENT_NAME));
  }

  protected static IJavaProject getSharedJavaProject() {
    return JavaCore.create(getProject(SHARED_NAME));
  }
}
