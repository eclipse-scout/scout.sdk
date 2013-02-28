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
package org.eclipse.scout.sdk.internal.test.bug.beforeopensource;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h1>Bug 87'400</h1>
 * <p>
 * <b>Symptom:</b> Sometimes NPE are thrown when expanding the forms node of a Scout client project or the services node
 * of a Scout server project.
 * <p>
 * <b>Reason:</b> Scout resolves the the hosting plug-in of a method by traversing the JDT's {@link IJavaElement}
 * hierarchy up to the root to find the project. After the java project has been found it is resolved to its appropriate
 * {@link IScoutBundle}. This works fine for methods declared in classes belonging to a Scout bundle (i.e. client,
 * server or shared plug-in, that is visible in the Scout perspective). However, it does not always work for methods
 * (scout configuration or exec methods) that are yet not part of a Scout project class.
 * <p>
 * The setup, in which it happens, is a workspace with additional non-scout plug-ins (i.e. they do not have the Scout
 * project nature). The additional plug-in is required to have a dependency on a Scout plug-in. Hence a yet not
 * implemented configuration or exec method resolves to the additional plug-in.
 */
public class Bug87400Test extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("bugsBeforeOpensource/87400", "a", "a.client");
  }

  @Test
  public void testGetScoutBundle_ScoutType() throws Exception {
    IType form = TypeUtility.getType("a.client.form.AForm");
    Assert.assertNotNull(form);

    IType mainBox = form.getType("MainBox");
    Assert.assertNotNull(mainBox);

    Assert.assertEquals("a.client", ScoutTypeUtility.getScoutBundle(form).getSymbolicName());
    Assert.assertEquals("a.client", ScoutTypeUtility.getScoutBundle(mainBox).getSymbolicName());
  }

  @Test
  public void testGetScoutBundle_ScoutMethod() throws Exception {
    IType form = TypeUtility.getType("a.client.form.AForm");
    Assert.assertNotNull(form);

    IType mainBox = form.getType("MainBox");
    Assert.assertNotNull(mainBox);

    checkScoutBundleForMethodOnField("foo", mainBox.getType("TextFieldWithConfiguration"));
    checkScoutBundleForMethodOnField("bar", mainBox.getType("TextFieldWithoutConfiguration"));
  }

  @Test
  public void testGetScoutBundle_ScoutExecMethod() throws Exception {
    IType form = TypeUtility.getType("a.client.form.AForm");
    Assert.assertNotNull(form);

    IType mainBox = form.getType("MainBox");
    Assert.assertNotNull(mainBox);

    checkScoutBundleForMethodOnField("execChangedValue", mainBox.getType("TextFieldWithConfiguration"));
  }

  @Test
  public void testGetScoutBundle_ScoutConfigPropertyMethod() throws Exception {
    IType form = TypeUtility.getType("a.client.form.AForm");
    Assert.assertTrue(TypeUtility.exists(form));

    IType mainBox = form.getType("MainBox");
    Assert.assertNotNull(mainBox);

    checkScoutBundleForMethodOnField("getConfiguredLabel", mainBox.getType("TextFieldWithConfiguration"));
  }

  private void checkScoutBundleForMethodOnField(String methodName, IType field) throws JavaModelException {
    Assert.assertNotNull(field);
    IMethod method = getScoutMethod(methodName, field);
    Assert.assertTrue(TypeUtility.exists(method));
    IScoutBundle scoutBundle = ScoutTypeUtility.getScoutBundle(method);
    Assert.assertNotNull(scoutBundle);
    Assert.assertEquals("a.client", scoutBundle.getSymbolicName());
  }

  private IMethod getScoutMethod(String methodName, IType type) throws JavaModelException {
    return TypeUtility.getMethod(type, methodName);
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    clearWorkspace();
  }
}
