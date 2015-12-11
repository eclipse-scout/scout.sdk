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
package org.eclipse.scout.sdk.core.s;

import java.lang.reflect.Field;

import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link ScoutRuntimeTypesTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ScoutRuntimeTypesTest {
  @Test
  public void testApi() throws IllegalArgumentException, IllegalAccessException {
    IJavaEnvironment env = CoreScoutTestingUtils.createClientJavaEnvironment();
    testFields(IScoutRuntimeTypes.class.getFields(), env);
    testFields(IJavaRuntimeTypes.class.getFields(), env);
  }

  private static void testFields(Field[] fields, IJavaEnvironment env) throws IllegalArgumentException, IllegalAccessException {
    for (Field f : fields) {
      Object val = f.get(null);
      if (val instanceof String) {
        String fqn = (String) val;
        if (fqn.indexOf('.') > 0) {
          IType type = env.findType(fqn);
          Assert.assertNotNull("type '" + fqn + "' not found.", type);
        }
      }
    }
  }
}
