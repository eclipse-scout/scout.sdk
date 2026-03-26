/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.generator.field;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.apidef.ApiRequirement;
import org.eclipse.scout.sdk.core.java.testing.apidef.EnabledFor;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.testing.context.UsernameExtension;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutFullJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link ScoutFieldGeneratorTest}</h3>
 */
@ExtendWith(UsernameExtension.class)
@ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class)
public class ScoutFieldGeneratorTest {

  @Test
  @EnabledFor(api = IScoutApi.class, require = ApiRequirement.MIN, version = {26, 2})
  public void testSerialVersionUid(IJavaEnvironment env) {
    assertEquals("@Serial\nprivate static final long serialVersionUID = 1L;", FieldGenerator.createSerialVersionUid().toJavaSource(env).toString());
    assertEquals("@Serial\nprivate static final long serialVersionUID = 1234L;", FieldGenerator.createSerialVersionUid(1234).toJavaSource(env).toString());
  }

  @Test
  @EnabledFor(api = IScoutApi.class, require = ApiRequirement.BEFORE, version = {26, 2})
  public void testSerialVersionUidWithoutSerialAnnotation(IJavaEnvironment env) {
    assertEquals("private static final long serialVersionUID = 1L;", FieldGenerator.createSerialVersionUid().toJavaSource(env).toString());
    assertEquals("private static final long serialVersionUID = 1234L;", FieldGenerator.createSerialVersionUid(1234).toJavaSource(env).toString());
  }
}
