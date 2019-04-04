/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.ecj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link JavaEnvironmentWithEcjTest}</h3>
 *
 * @since 9.0.0
 */
public class JavaEnvironmentWithEcjTest {

  /**
   * Tests that registerCompilationUnitOverride can be called on closed environments and has no effect (the environment
   * stays closed)
   */
  @Test
  public void testRegisterAfterCloseKeepsEnvironmentUntouched() throws ReflectiveOperationException {
    String pck = "xx.yy";
    String className = "Test";
    @SuppressWarnings("resource")
    JavaEnvironmentWithEcj closedEnv = createClosedJavaEnvironment();
    assertEnvironmentClosed(closedEnv);
    closedEnv.registerCompilationUnitOverride(pck, className + JavaTypes.JAVA_FILE_SUFFIX, ("package " + pck + "; public class " + className + " {}").toCharArray());
    assertEnvironmentClosed(closedEnv);
    closedEnv.registerCompilationUnitOverride(pck, className + JavaTypes.JAVA_FILE_SUFFIX, ("package " + pck + "; public class " + className + " {}").toCharArray());

    String fqn = pck + '.' + className;
    assertThrows(IllegalArgumentException.class, () -> closedEnv.findType(fqn));
    closedEnv.reload();
    assertNotNull(closedEnv.findType(fqn));
    assertTrue(closedEnv.isInitialized());
  }

  private JavaEnvironmentWithEcj createClosedJavaEnvironment() {
    AtomicReference<IJavaEnvironment> holder = new AtomicReference<>();
    new CoreJavaEnvironmentBinaryOnlyFactory().accept(holder::set);
    return (JavaEnvironmentWithEcj) holder.get().unwrap();
  }

  private void assertEnvironmentClosed(JavaEnvironmentWithEcj candidate) throws ReflectiveOperationException {
    assertNotNull(candidate);
    assertEquals(0, fieldValue(candidate, "m_elements", Map.class).size());
    assertEquals(0, fieldValue(candidate, "m_evpCache", Map.class).size());
    assertEquals(0, fieldValue(candidate, "m_mvpCache", Map.class).size());
    assertEquals(0, fieldValue(candidate, "m_sourceCache", Map.class).size());
    assertFalse(fieldValue(candidate, "m_rawClassPath", Collection.class).isEmpty()); // classpath is preserved so that the environment can be reinitialized
    assertFalse(fieldValue(candidate, "m_compiler", FinalValue.class).isSet());
    assertFalse(fieldValue(candidate, "m_classpath", FinalValue.class).isSet());
  }

  @SuppressWarnings("unchecked")
  private <T> T fieldValue(Object instance, String fieldName, Class<T> type) throws ReflectiveOperationException {
    Field field = instance.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return (T) field.get(instance);
  }
}
