/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s;

import java.lang.reflect.Field;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutFullJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link ScoutRuntimeTypesTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class)
public class ScoutRuntimeTypesTest {

  @Test
  public void testApi(IJavaEnvironment env) throws IllegalAccessException {
    testFields(IScoutRuntimeTypes.class.getFields(), env);
    testFields(JavaTypes.class.getFields(), env);
  }

  private static void testFields(Field[] fields, IJavaEnvironment env) throws IllegalAccessException {
    for (Field f : fields) {
      Object val = f.get(null);
      if (val instanceof String) {
        String fqn = (String) val;
        if (fqn.indexOf(JavaTypes.C_DOT) > 0 && !fqn.endsWith(JavaTypes.JAVA_FILE_SUFFIX)) {
          env.requireType(fqn);
        }
      }
    }
  }
}
