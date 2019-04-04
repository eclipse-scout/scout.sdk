/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.generator;

import org.eclipse.scout.sdk.core.fixture.ClassWithMembers;
import org.eclipse.scout.sdk.core.fixture.InterfaceWithDefaultMethods;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.SdkAssertions;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link SourceModelRoundtripTest}</h3>
 *
 * @since 5.1.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class SourceModelRoundtripTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/generator/";

  @Test
  public void testMembersOfSourceClass(IJavaEnvironment env) {
    IType type = env.requireType(ClassWithMembers.class.getName());
    assertEqualsRefFile("ClassWithMembers_source.txt", type, env);
  }

  @Test
  @ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentBinaryOnlyFactory.class)
  public void testMembersOfBinaryClass(IJavaEnvironment env) {
    IType type = env.requireType(ClassWithMembers.class.getName());
    assertEqualsRefFile("ClassWithMembers_binary.txt", type, env);
  }

  @Test
  public void testInterfaceWithDefaultMethods(IJavaEnvironment env) {
    IType type = env.requireType(InterfaceWithDefaultMethods.class.getName());
    assertEqualsRefFile("InterfaceWithDefaultMethods_source.txt", type, env);
  }

  protected static void assertEqualsRefFile(String refFileName, IType type, IJavaEnvironment env) {
    SdkAssertions.assertEqualsRefFile(env, REF_FILE_FOLDER + refFileName, type.requireCompilationUnit().toWorkingCopy());
  }
}
