/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.generator.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.testing.context.UsernameExtension;
import org.eclipse.scout.sdk.core.s.java.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutFullJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link ScoutAnnotationGeneratorTest}</h3>
 *
 * @since 13.0
 */
@ExtendWith(UsernameExtension.class)
@ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class)
public class ScoutAnnotationGeneratorTest {

  @Test
  public void testGenerated(IJavaEnvironment env) {
    assertEquals("@Generated(value = \"Generator\", comments = \"This class is auto generated. No manual modifications recommended.\")", ScoutAnnotationGenerator.createGenerated("Generator").toJavaSource(env).toString());
    assertEquals("@Generated(value = \"Generator\", comments = \"Test\\\"Comment\")", ScoutAnnotationGenerator.createGenerated("Generator", "Test\"Comment").toJavaSource(env).toString());
  }
}
